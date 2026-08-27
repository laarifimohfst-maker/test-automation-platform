package com.testplatform.test_automation_platform;

import com.testplatform.test_automation_platform.entity.Utilisateur;
import com.testplatform.test_automation_platform.enums.Role;
import com.testplatform.test_automation_platform.repository.UtilisateurRepository;
import com.testplatform.test_automation_platform.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    private Utilisateur proprietaire;
    private Utilisateur autreUtilisateur;

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

    private RequestPostProcessor jwtAdministrateur() {
        return jwt()
                .jwt(token -> token
                        .subject("admin@example.invalid")
                        .claim("roles", List.of("ROLE_ADMIN")))
                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }
}
