package com.testplatform.test_automation_platform;

import com.testplatform.test_automation_platform.entity.Utilisateur;
import com.testplatform.test_automation_platform.entity.ConfigurationTest;
import com.testplatform.test_automation_platform.entity.ExecutionTest;
import com.testplatform.test_automation_platform.entity.Notification;
import com.testplatform.test_automation_platform.entity.Projet;
import com.testplatform.test_automation_platform.entity.Rapport;
import com.testplatform.test_automation_platform.entity.ResultatTest;
import com.testplatform.test_automation_platform.enums.Role;
import com.testplatform.test_automation_platform.enums.StatutExecution;
import com.testplatform.test_automation_platform.enums.StatutProjet;
import com.testplatform.test_automation_platform.enums.StatutTest;
import com.testplatform.test_automation_platform.enums.TypeNotification;
import com.testplatform.test_automation_platform.enums.TypeRapport;
import com.testplatform.test_automation_platform.enums.TypeSource;
import com.testplatform.test_automation_platform.enums.TypeTest;
import com.testplatform.test_automation_platform.repository.ConfigurationTestRepository;
import com.testplatform.test_automation_platform.repository.ExecutionRepository;
import com.testplatform.test_automation_platform.repository.ExecutionTestRepository;
import com.testplatform.test_automation_platform.repository.NotificationRepository;
import com.testplatform.test_automation_platform.repository.ProjetRepository;
import com.testplatform.test_automation_platform.repository.RapportRepository;
import com.testplatform.test_automation_platform.repository.ResultatTestRepository;
import com.testplatform.test_automation_platform.repository.UtilisateurRepository;
import com.testplatform.test_automation_platform.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.containsString;

@SpringBootTest(properties = {
        "security.jwt.key-directory=target/test-jwt-keys",
        "security.bootstrap-admin.email=",
        "security.bootstrap-admin.password="
})
@AutoConfigureMockMvc
@Transactional
class SecurityIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private JwtAuthenticationConverter jwtAuthenticationConverter;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ProjetRepository projetRepository;

    @Autowired
    private ConfigurationTestRepository configurationTestRepository;

    @Autowired
    private ExecutionTestRepository executionTestRepository;

    @Autowired
    private ExecutionRepository executionRepository;

    @Autowired
    private ResultatTestRepository resultatTestRepository;

    @Autowired
    private RapportRepository rapportRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private EntityManager entityManager;

    private Utilisateur proprietaire;
    private Utilisateur autreUtilisateur;
    private Utilisateur administrateur;

    @BeforeEach
    void creerUtilisateursDeTest() {
        proprietaire = utilisateurRepository.save(Utilisateur.builder()
                .nom("Propriétaire sécurité")
                .email("security-owner@example.invalid")
                .motDePasse("hash-inutilise-dans-ce-test")
                .role(Role.DEVELOPPEUR)
                .dateCreation(LocalDateTime.now())
                .build());

        autreUtilisateur = utilisateurRepository.save(Utilisateur.builder()
                .nom("Autre utilisateur sécurité")
                .email("security-other@example.invalid")
                .motDePasse("hash-inutilise-dans-ce-test")
                .role(Role.DEVELOPPEUR)
                .dateCreation(LocalDateTime.now())
                .build());

        administrateur = utilisateurRepository.save(Utilisateur.builder()
                .nom("Administrateur sécurité")
                .email("admin@example.invalid")
                .motDePasse("hash-inutilise-dans-ce-test")
                .role(Role.ADMIN)
                .dateCreation(LocalDateTime.now())
                .build());
    }

    @Test
    void routeProtegeeSansJwtRetourne401() throws Exception {
        mockMvc.perform(get("/api/utilisateurs"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.message").value("Authentification requise."));
    }

    @Test
    void inscriptionPubliqueCreeToujoursUnDeveloppeur() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content("""
                                {
                                  "nom": "Nouveau compte",
                                  "email": "new-account@example.invalid",
                                  "motDePasse": "MotDePasse123!",
                                  "role": "ADMIN"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("DEVELOPPEUR"))
                .andExpect(jsonPath("$.motDePasse").doesNotExist());

        Utilisateur utilisateur = utilisateurRepository
                .findByEmailIgnoreCase("new-account@example.invalid")
                .orElseThrow();

        assertThat(utilisateur.getRole()).isEqualTo(Role.DEVELOPPEUR);
        assertThat(utilisateur.getMotDePasse()).startsWith("$2");
    }

    @Test
    void inscriptionRefuseUnMotDePasseTropCourt() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content("""
                                {
                                  "nom": "Compte invalide",
                                  "email": "invalid-account@example.invalid",
                                  "motDePasse": "court"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "Le mot de passe doit contenir au moins 8 caractères."
                ));
    }

    @Test
    void developpeurNePeutPasListerLesUtilisateurs() throws Exception {
        mockMvc.perform(get("/api/utilisateurs")
                        .with(jwtDeveloppeur(proprietaire.getEmail())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Accès interdit."));
    }

    @Test
    void administrateurPeutListerLesUtilisateursSansVoirLesMotsDePasse()
            throws Exception {
        mockMvc.perform(get("/api/utilisateurs").with(jwtAdministrateur()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].motDePasse").doesNotExist());
    }

    @Test
    void corsAutorisePatchDepuisLeFrontend() throws Exception {
        mockMvc.perform(options("/api/utilisateurs/{id}/desactiver", proprietaire.getId())
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "PATCH"))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        "Access-Control-Allow-Origin",
                        "http://localhost:5173"
                ))
                .andExpect(header().string(
                        "Access-Control-Allow-Methods",
                        containsString("PATCH")
                ));
    }

    @Test
    void administrateurPeutCreerPuisModifierUnUtilisateur()
            throws Exception {
        String email = "managed-user-" + UUID.randomUUID() + "@example.invalid";

        mockMvc.perform(post("/api/utilisateurs")
                        .with(jwtAdministrateur())
                        .contentType("application/json")
                        .content("""
                                {
                                  "nom": "Utilisateur géré",
                                  "email": "%s",
                                  "motDePasse": "MotDePasse123!",
                                  "role": "DEVELOPPEUR"
                                }
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.role").value("DEVELOPPEUR"))
                .andExpect(jsonPath("$.motDePasse").doesNotExist());

        Utilisateur utilisateur = utilisateurRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow();

        mockMvc.perform(put("/api/utilisateurs/{id}", utilisateur.getId())
                        .with(jwtAdministrateur())
                        .contentType("application/json")
                        .content("""
                                {
                                  "nom": "Utilisateur modifié",
                                  "email": "%s",
                                  "role": "ADMIN"
                                }
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value("Utilisateur modifié"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void suppressionUtilisateurNettoieSesDonneesAssociees()
            throws Exception {
        Utilisateur utilisateur = utilisateurRepository.save(Utilisateur.builder()
                .nom("Utilisateur à supprimer")
                .email("delete-user-" + UUID.randomUUID() + "@example.invalid")
                .motDePasse("hash-inutilise-dans-ce-test")
                .role(Role.DEVELOPPEUR)
                .dateCreation(LocalDateTime.now())
                .build());

        Projet projet = projetRepository.save(Projet.builder()
                .nom("Projet de l'utilisateur supprimé")
                .description("Projet avec dépendances")
                .typeSource(TypeSource.GITHUB)
                .dateImport(LocalDateTime.now())
                .statut(StatutProjet.IMPORTE)
                .cheminLocal("uploads/delete-user-test-" + UUID.randomUUID())
                .projectKey("delete-user-test-" + UUID.randomUUID())
                .utilisateur(utilisateur)
                .build());

        ConfigurationTest configuration = configurationTestRepository.save(
                ConfigurationTest.builder()
                        .projet(projet)
                        .testsUnitaires(true)
                        .dateConfiguration(LocalDateTime.now())
                        .build()
        );

        ExecutionTest execution = new ExecutionTest();
        execution.setProjet(projet);
        execution.setConfigurationTest(configuration);
        execution.setStatut(StatutExecution.TERMINEE);
        execution.setDateDebut(LocalDateTime.now());
        execution = executionTestRepository.save(execution);

        ResultatTest resultat = resultatTestRepository.save(ResultatTest.builder()
                .executionTest(execution)
                .type(TypeTest.UNITAIRE)
                .nomTest("testSuppressionUtilisateur")
                .statut(StatutTest.REUSSI)
                .build());

        Rapport rapport = rapportRepository.save(Rapport.builder()
                .nom("Rapport utilisateur supprimé")
                .type(TypeRapport.TESTS)
                .execution(execution)
                .dateGeneration(LocalDateTime.now())
                .build());

        Notification notificationExecution = notificationRepository.save(
                Notification.builder()
                        .message("Notification liée à l'exécution")
                        .type(TypeNotification.SUCCES)
                        .utilisateur(utilisateur)
                        .execution(execution)
                        .dateEnvoi(LocalDateTime.now())
                        .build()
        );
        Notification notificationSimple = notificationRepository.save(
                Notification.builder()
                        .message("Notification sans exécution")
                        .type(TypeNotification.INFORMATION)
                        .utilisateur(utilisateur)
                        .dateEnvoi(LocalDateTime.now())
                        .build()
        );

        Long utilisateurId = utilisateur.getId();
        Long projetId = projet.getId();
        Long configurationId = configuration.getId();
        Long executionId = execution.getId();
        Long resultatId = resultat.getId();
        Long rapportId = rapport.getId();
        Long notificationExecutionId = notificationExecution.getId();
        Long notificationSimpleId = notificationSimple.getId();

        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(delete("/api/utilisateurs/{id}", utilisateurId)
                        .with(jwtAdministrateur()))
                .andExpect(status().isNoContent());

        assertThat(utilisateurRepository.existsById(utilisateurId)).isFalse();
        assertThat(projetRepository.existsById(projetId)).isFalse();
        assertThat(configurationTestRepository.existsById(configurationId)).isFalse();
        assertThat(executionTestRepository.existsById(executionId)).isFalse();
        assertThat(resultatTestRepository.existsById(resultatId)).isFalse();
        assertThat(rapportRepository.existsById(rapportId)).isFalse();
        assertThat(notificationRepository.existsById(notificationExecutionId)).isFalse();
        assertThat(notificationRepository.existsById(notificationSimpleId)).isFalse();
    }

    @Test
    void administrateurPeutDesactiverEtReactiverUnDeveloppeur()
            throws Exception {
        mockMvc.perform(patch(
                        "/api/utilisateurs/{id}/desactiver",
                        proprietaire.getId()
                ).with(jwtAdministrateur()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.actif").value(false));

        mockMvc.perform(patch(
                        "/api/utilisateurs/{id}/reactiver",
                        proprietaire.getId()
                ).with(jwtAdministrateur()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.actif").value(true));
    }

    @Test
    void developpeurNePeutPasDesactiverUnCompte() throws Exception {
        mockMvc.perform(patch(
                        "/api/utilisateurs/{id}/desactiver",
                        autreUtilisateur.getId()
                ).with(jwtDeveloppeur(proprietaire.getEmail())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Accès interdit."));
    }

    @Test
    void administrateurNePeutPasDesactiverSonPropreCompte()
            throws Exception {
        mockMvc.perform(patch(
                        "/api/utilisateurs/{id}/desactiver",
                        administrateur.getId()
                ).with(jwtAdministrateur()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "Un administrateur ne peut pas désactiver son propre compte."
                ));
    }

    @Test
    void compteDesactiveNePeutPasSeConnecter() throws Exception {
        proprietaire.setMotDePasse(passwordEncoder.encode("MotDePasse123!"));
        proprietaire.setActif(false);
        utilisateurRepository.saveAndFlush(proprietaire);

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("""
                                {
                                  "email": "security-owner@example.invalid",
                                  "motDePasse": "MotDePasse123!"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(
                        "Email ou mot de passe incorrect."
                ));
    }

    @Test
    void ancienTokenDUnCompteDesactiveEstRefuse() throws Exception {
        var authentication = UsernamePasswordAuthenticationToken.authenticated(
                proprietaire.getEmail(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_DEVELOPPEUR"))
        );
        String token = jwtService.genererToken(authentication);

        proprietaire.setActif(false);
        utilisateurRepository.saveAndFlush(proprietaire);

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void developpeurPeutConsulterSonDashboard() throws Exception {
        mockMvc.perform(get("/api/dashboard")
                        .param("utilisateurId", proprietaire.getId().toString())
                        .with(jwtDeveloppeur(proprietaire.getEmail())))
                .andExpect(status().isOk());
    }

    @Test
    void developpeurNePeutPasConsulterLeDashboardDUnAutreUtilisateur()
            throws Exception {
        mockMvc.perform(get("/api/dashboard")
                        .param("utilisateurId", autreUtilisateur.getId().toString())
                        .with(jwtDeveloppeur(proprietaire.getEmail())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Accès interdit."));
    }

    @Test
    void developpeurNePeutPasSupprimerUnProjet() throws Exception {
        mockMvc.perform(delete("/api/projets/999999")
                        .with(jwtDeveloppeur(proprietaire.getEmail())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Accès interdit."));
    }

    @Test
    void administrateurPeutFiltrerLesProjetsAvecLeurProprietaire()
            throws Exception {
        Projet projet = creerProjetTest(
                "Projet administration recherché",
                TypeSource.GITHUB
        );

        mockMvc.perform(get("/api/projets")
                        .param("recherche", "administration")
                        .param("utilisateurId", proprietaire.getId().toString())
                        .param("statut", "IMPORTE")
                        .param("typeSource", "GITHUB")
                        .with(jwtAdministrateur()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(projet.getId()))
                .andExpect(jsonPath("$[0].utilisateur.id")
                        .value(proprietaire.getId()))
                .andExpect(jsonPath("$[0].utilisateur.motDePasse")
                        .doesNotExist());
    }

    @Test
    void developpeurNePeutPasConsulterLaListeGlobaleDesProjets()
            throws Exception {
        mockMvc.perform(get("/api/projets")
                        .with(jwtDeveloppeur(proprietaire.getEmail())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Accès interdit."));
    }

    @Test
    void administrateurSupprimeProjetEtToutesSesDonneesLiees()
            throws Exception {
        Projet projet = creerProjetTest(
                "Projet à supprimer",
                TypeSource.ARCHIVE_ZIP
        );
        ConfigurationTest configuration = configurationTestRepository.save(
                ConfigurationTest.builder()
                        .projet(projet)
                        .testsUnitaires(true)
                        .dateConfiguration(LocalDateTime.now())
                        .build()
        );

        ExecutionTest execution = new ExecutionTest();
        execution.setProjet(projet);
        execution.setConfigurationTest(configuration);
        execution.setStatut(StatutExecution.TERMINEE);
        execution.setDateDebut(LocalDateTime.now());
        execution = executionTestRepository.save(execution);

        ResultatTest resultat = resultatTestRepository.save(
                ResultatTest.builder()
                        .executionTest(execution)
                        .type(TypeTest.UNITAIRE)
                        .nomTest("testAdministration")
                        .statut(StatutTest.REUSSI)
                        .build()
        );
        Rapport rapport = rapportRepository.save(
                Rapport.builder()
                        .nom("Rapport administration")
                        .type(TypeRapport.TESTS)
                        .execution(execution)
                        .dateGeneration(LocalDateTime.now())
                        .build()
        );
        Notification notification = notificationRepository.save(
                Notification.builder()
                        .message("Exécution terminée")
                        .type(TypeNotification.SUCCES)
                        .utilisateur(proprietaire)
                        .execution(execution)
                        .dateEnvoi(LocalDateTime.now())
                        .build()
        );

        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(delete("/api/projets/{id}", projet.getId())
                        .with(jwtAdministrateur()))
                .andExpect(status().isNoContent());

        assertThat(projetRepository.existsById(projet.getId())).isFalse();
        assertThat(configurationTestRepository.existsById(configuration.getId()))
                .isFalse();
        assertThat(executionTestRepository.existsById(execution.getId())).isFalse();
        assertThat(resultatTestRepository.existsById(resultat.getId())).isFalse();
        assertThat(rapportRepository.existsById(rapport.getId())).isFalse();
        assertThat(notificationRepository.existsById(notification.getId())).isFalse();
    }

    @Test
    void administrateurPeutFiltrerLesRapportsParProjetEtUtilisateur()
            throws Exception {
        Projet projet = creerProjetTest(
                "Projet rapports administration",
                TypeSource.GITHUB
        );
        ExecutionTest execution = creerExecutionTest(projet);
        Rapport rapport = rapportRepository.save(Rapport.builder()
                .nom("Rapport recherché administration")
                .type(TypeRapport.TESTS)
                .execution(execution)
                .dateGeneration(LocalDateTime.now())
                .build());

        mockMvc.perform(get("/api/rapports")
                        .param("recherche", "recherché")
                        .param("utilisateurId", proprietaire.getId().toString())
                        .param("projetId", projet.getId().toString())
                        .param("type", "TESTS")
                        .with(jwtAdministrateur()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(rapport.getId()))
                .andExpect(jsonPath("$[0].execution.projet.id")
                        .value(projet.getId()));
    }

    @Test
    void developpeurNePeutPasConsulterLaListeGlobaleDesRapports()
            throws Exception {
        mockMvc.perform(get("/api/rapports")
                        .with(jwtDeveloppeur(proprietaire.getEmail())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Accès interdit."));
    }

    @Test
    void administrateurPeutTelechargerPuisSupprimerUnRapportEtSonPdf()
            throws Exception {
        Projet projet = creerProjetTest(
                "Projet téléchargement rapport",
                TypeSource.ARCHIVE_ZIP
        );
        ExecutionTest execution = creerExecutionTest(projet);
        Path dossierRapports = Paths.get("reports").toAbsolutePath().normalize();
        Files.createDirectories(dossierRapports);
        Path fichier = Files.createTempFile(
                dossierRapports,
                "rapport-admin-test-",
                ".pdf"
        );
        byte[] contenuPdf = new byte[]{37, 80, 68, 70, 45, 49, 46, 52};
        Files.write(fichier, contenuPdf);

        try {
            Rapport rapport = rapportRepository.save(Rapport.builder()
                    .nom("Rapport sécurisé")
                    .type(TypeRapport.TESTS)
                    .execution(execution)
                    .cheminFichier(fichier.toString())
                    .taille((long) contenuPdf.length)
                    .dateGeneration(LocalDateTime.now())
                    .build());

            mockMvc.perform(get("/api/rapports/{id}/download", rapport.getId())
                            .with(jwtAdministrateur()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                    .andExpect(content().bytes(contenuPdf));

            mockMvc.perform(delete("/api/rapports/{id}", rapport.getId())
                            .with(jwtAdministrateur()))
                    .andExpect(status().isNoContent());

            assertThat(rapportRepository.existsById(rapport.getId())).isFalse();
            assertThat(Files.exists(fichier)).isFalse();
        } finally {
            Files.deleteIfExists(fichier);
        }
    }

    @Test
    void telechargementRefuseUnFichierSitueHorsDuDossierRapports()
            throws Exception {
        Projet projet = creerProjetTest(
                "Projet rapport externe",
                TypeSource.GITHUB
        );
        ExecutionTest execution = creerExecutionTest(projet);
        Rapport rapport = rapportRepository.save(Rapport.builder()
                .nom("Rapport externe interdit")
                .type(TypeRapport.TESTS)
                .execution(execution)
                .cheminFichier("src/main/resources/application.properties")
                .dateGeneration(LocalDateTime.now())
                .build());

        mockMvc.perform(get("/api/rapports/{id}/download", rapport.getId())
                        .with(jwtAdministrateur()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "Le fichier du rapport se trouve hors du dossier autorisé."
                ));
    }

    @Test
    void administrateurPeutConsulterLeDashboardGlobal() throws Exception {
        LocalDateTime dateRecente = LocalDateTime.of(2099, 1, 1, 12, 0);
        Projet projet = creerProjetTest(
                "Projet dashboard administration",
                TypeSource.GITHUB
        );
        projet.setDateImport(dateRecente);
        projet = projetRepository.save(projet);

        ExecutionTest execution = creerExecutionTest(projet);
        execution.setDateDebut(dateRecente);
        execution = executionTestRepository.save(execution);

        resultatTestRepository.save(ResultatTest.builder()
                .executionTest(execution)
                .type(TypeTest.UNITAIRE)
                .nomTest("testDashboardAdministration")
                .statut(StatutTest.REUSSI)
                .build());

        Rapport rapport = rapportRepository.save(Rapport.builder()
                .nom("Rapport dashboard administration")
                .type(TypeRapport.TESTS)
                .execution(execution)
                .dateGeneration(dateRecente)
                .build());

        mockMvc.perform(get("/api/admin/dashboard")
                        .with(jwtAdministrateur()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.utilisateurs.total")
                        .value(utilisateurRepository.count()))
                .andExpect(jsonPath("$.projets.total")
                        .value(projetRepository.count()))
                .andExpect(jsonPath("$.executions.total")
                        .value(executionRepository.count()))
                .andExpect(jsonPath("$.tests.total")
                        .value(resultatTestRepository.count()))
                .andExpect(jsonPath("$.rapports.total")
                        .value(rapportRepository.count()))
                .andExpect(jsonPath("$.activiteRecente.derniersProjets[0].id")
                        .value(projet.getId()))
                .andExpect(jsonPath("$.activiteRecente.dernieresExecutions[0].id")
                        .value(execution.getId()))
                .andExpect(jsonPath("$.activiteRecente.derniersRapports[0].id")
                        .value(rapport.getId()))
                .andExpect(jsonPath(
                        "$.activiteRecente.derniersProjets[0].cheminLocal"
                ).doesNotExist())
                .andExpect(jsonPath(
                        "$.activiteRecente.derniersRapports[0].cheminFichier"
                ).doesNotExist())
                .andExpect(jsonPath(
                        "$.activiteRecente.derniersProjets[0].utilisateur.motDePasse"
                ).doesNotExist());
    }

    @Test
    void developpeurNePeutPasConsulterLeDashboardAdministrateur()
            throws Exception {
        mockMvc.perform(get("/api/admin/dashboard")
                        .with(jwtDeveloppeur(proprietaire.getEmail())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Accès interdit."));
    }

    @Test
    void convertisseurJwtLitLeClaimRoles() {
        Jwt jwt = Jwt.withTokenValue("token-test")
                .header("alg", "RS256")
                .subject(proprietaire.getEmail())
                .claim("roles", List.of("ROLE_DEVELOPPEUR"))
                .build();

        var authentication = jwtAuthenticationConverter.convert(jwt);

        assertThat(authentication).isNotNull();
        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .contains("ROLE_DEVELOPPEUR");
    }

    @Test
    void tokenGenereEstSigneEtValide() {
        var authentication = UsernamePasswordAuthenticationToken.authenticated(
                proprietaire.getEmail(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_DEVELOPPEUR"))
        );

        Jwt jwt = jwtDecoder.decode(jwtService.genererToken(authentication));

        assertThat(jwt.getSubject()).isEqualTo(proprietaire.getEmail());
        assertThat(jwt.getIssuer().toString())
                .isEqualTo("http://localhost:8080");
        assertThat(jwt.getAudience()).contains("test-automation-api");
        assertThat(jwt.getClaimAsStringList("roles"))
                .containsExactly("ROLE_DEVELOPPEUR");
        assertThat(jwt.getExpiresAt()).isAfter(jwt.getIssuedAt());
    }

    private RequestPostProcessor jwtDeveloppeur(String email) {
        return jwt()
                .jwt(token -> token
                        .subject(email)
                        .claim("roles", List.of("ROLE_DEVELOPPEUR")))
                .authorities(new SimpleGrantedAuthority("ROLE_DEVELOPPEUR"));
    }

    private Projet creerProjetTest(String nom, TypeSource typeSource) {
        return projetRepository.save(Projet.builder()
                .nom(nom)
                .description("Projet créé pour les tests administrateur")
                .typeSource(typeSource)
                .dateImport(LocalDateTime.now())
                .statut(StatutProjet.IMPORTE)
                .cheminLocal("uploads/projet-admin-inexistant-" + UUID.randomUUID())
                .projectKey("admin-test-" + UUID.randomUUID())
                .utilisateur(proprietaire)
                .build());
    }

    private ExecutionTest creerExecutionTest(Projet projet) {
        ConfigurationTest configuration = configurationTestRepository.save(
                ConfigurationTest.builder()
                        .projet(projet)
                        .testsUnitaires(true)
                        .dateConfiguration(LocalDateTime.now())
                        .build()
        );

        ExecutionTest execution = new ExecutionTest();
        execution.setProjet(projet);
        execution.setConfigurationTest(configuration);
        execution.setStatut(StatutExecution.TERMINEE);
        execution.setDateDebut(LocalDateTime.now());
        return executionTestRepository.save(execution);
    }

    private RequestPostProcessor jwtAdministrateur() {
        return jwt()
                .jwt(token -> token
                        .subject("admin@example.invalid")
                        .claim("roles", List.of("ROLE_ADMIN")))
                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }
}
