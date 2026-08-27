package com.testplatform.test_automation_platform.service;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.testplatform.test_automation_platform.entity.Execution;
import com.testplatform.test_automation_platform.entity.ExecutionAnalyseQualite;
import com.testplatform.test_automation_platform.entity.ExecutionTest;
import com.testplatform.test_automation_platform.entity.Projet;
import com.testplatform.test_automation_platform.entity.Utilisateur;
import com.testplatform.test_automation_platform.enums.StatutProjet;
import com.testplatform.test_automation_platform.enums.TypeSource;
import com.testplatform.test_automation_platform.repository.AnalyseQualiteRepository;
import com.testplatform.test_automation_platform.repository.ConfigurationTestRepository;
import com.testplatform.test_automation_platform.repository.ExecutionRepository;
import com.testplatform.test_automation_platform.repository.NotificationRepository;
import com.testplatform.test_automation_platform.repository.ProjetRepository;
import com.testplatform.test_automation_platform.repository.RapportRepository;
import com.testplatform.test_automation_platform.repository.ResultatTestRepository;

@Service
public class ProjetService {

    private final ProjetRepository projetRepository;
    private final FileStorageService fileStorageService;
    private final ExecutionRepository executionRepository;
    private final ConfigurationTestRepository configurationTestRepository;
    private final ResultatTestRepository resultatTestRepository;
    private final AnalyseQualiteRepository analyseQualiteRepository;
    private final RapportRepository rapportRepository;
    private final NotificationRepository notificationRepository;

    public ProjetService(
            ProjetRepository projetRepository,
            FileStorageService fileStorageService,
            ExecutionRepository executionRepository,
            ConfigurationTestRepository configurationTestRepository,
            ResultatTestRepository resultatTestRepository,
            AnalyseQualiteRepository analyseQualiteRepository,
            RapportRepository rapportRepository,
            NotificationRepository notificationRepository) {

        this.projetRepository = projetRepository;
        this.fileStorageService = fileStorageService;
        this.executionRepository = executionRepository;
        this.configurationTestRepository = configurationTestRepository;
        this.resultatTestRepository = resultatTestRepository;
        this.analyseQualiteRepository = analyseQualiteRepository;
        this.rapportRepository = rapportRepository;
        this.notificationRepository = notificationRepository;
    }

    public Projet creerProjet(Projet projet) {

        if (projet.getStatut() == null) {
            projet.setStatut(StatutProjet.IMPORTE);
        }

        return projetRepository.save(projet);
    }

    public Projet importerProjetZip(
            MultipartFile fichier,
            Utilisateur utilisateur)
            throws IOException {

        String cheminProjet =
                fileStorageService.sauvegarderProjet(fichier);

        Projet projet = new Projet();

        projet.setNom(fichier.getOriginalFilename().replace(".zip", ""));
        projet.setCheminLocal(cheminProjet);
        projet.setUtilisateur(utilisateur);
        projet.setDateImport(LocalDateTime.now());
        projet.setStatut(StatutProjet.IMPORTE);
        projet.setTypeSource(TypeSource.ARCHIVE_ZIP);

        projet.setProjectKey("project-" + UUID.randomUUID());

        Projet projetEnregistre = projetRepository.save(projet);

        return projetEnregistre;
    }

    public Projet importerProjetGithub(
            String urlGithub,
            Utilisateur utilisateur)
            throws IOException, InterruptedException {

        String cheminProjet =
                fileStorageService.clonerProjetGithub(urlGithub);

        Projet projet = new Projet();

        projet.setNom(Paths.get(cheminProjet).getFileName().toString());
        projet.setCheminLocal(cheminProjet);
        projet.setUtilisateur(utilisateur);
        projet.setDateImport(LocalDateTime.now());
        projet.setStatut(StatutProjet.IMPORTE);
        projet.setTypeSource(TypeSource.GITHUB);

        projet.setProjectKey("project-" + UUID.randomUUID());

        Projet projetEnregistre = projetRepository.save(projet);

        return projetEnregistre;
    }

    public List<Projet> obtenirTousLesProjets() {
        return projetRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Projet> rechercherProjetsAdministration(
            String recherche,
            Long utilisateurId,
            StatutProjet statut,
            TypeSource typeSource) {

        String texteRecherche = recherche == null
                ? ""
                : recherche.trim().toLowerCase(Locale.ROOT);

        return projetRepository
                .findAll(Sort.by(Sort.Direction.DESC, "dateImport"))
                .stream()
                .filter(projet -> utilisateurId == null
                        || projet.getUtilisateur().getId().equals(utilisateurId))
                .filter(projet -> statut == null || projet.getStatut() == statut)
                .filter(projet -> typeSource == null
                        || projet.getTypeSource() == typeSource)
                .filter(projet -> texteRecherche.isEmpty()
                        || contientIgnoreCase(projet.getNom(), texteRecherche)
                        || contientIgnoreCase(projet.getDescription(), texteRecherche)
                        || contientIgnoreCase(
                                projet.getUtilisateur().getNom(),
                                texteRecherche
                        )
                        || contientIgnoreCase(
                                projet.getUtilisateur().getEmail(),
                                texteRecherche
                        ))
                .toList();
    }

    public Projet obtenirProjetParId(Long id) {
        return projetRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Projet introuvable."));
    }

    public List<Projet> obtenirProjetsUtilisateur(Utilisateur utilisateur) {
        return projetRepository.findByUtilisateur(utilisateur);
    }

    public Projet modifierProjet(Long id, Projet projetModifie) {

        Projet projet = obtenirProjetParId(id);

        projet.setNom(projetModifie.getNom());
        projet.setDescription(projetModifie.getDescription());
        projet.setTypeSource(projetModifie.getTypeSource());
        projet.setStatut(projetModifie.getStatut());

        return projetRepository.save(projet);
    }

    @Transactional
    public void supprimerProjet(Long id) {

        Projet projet = obtenirProjetParId(id);
        List<Execution> executions = executionRepository.findByProjet(projet);

        for (Execution execution : executions) {
            Long executionId = execution.getId();

            notificationRepository.deleteByExecutionId(executionId);
            rapportRepository.deleteByExecutionId(executionId);

            if (execution instanceof ExecutionTest) {
                resultatTestRepository.deleteByExecutionId(executionId);
            }

            if (execution instanceof ExecutionAnalyseQualite executionAnalyse) {
                analyseQualiteRepository
                        .findByExecutionAnalyseQualite(executionAnalyse)
                        .ifPresent(analyseQualiteRepository::delete);
            }

            executionRepository.delete(execution);
        }

        executionRepository.flush();

        configurationTestRepository.deleteAll(
                configurationTestRepository.findByProjet(projet)
        );
        configurationTestRepository.flush();

        projetRepository.delete(projet);
        projetRepository.flush();

        if (fileStorageService.estCheminProjetAutorise(projet.getCheminLocal())) {
            fileStorageService.supprimerProjet(projet.getCheminLocal());
        }
    }

    private boolean contientIgnoreCase(String valeur, String recherche) {
        return valeur != null
                && valeur.toLowerCase(Locale.ROOT).contains(recherche);
    }
}
