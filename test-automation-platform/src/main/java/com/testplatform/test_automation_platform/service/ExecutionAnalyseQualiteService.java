package com.testplatform.test_automation_platform.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.testplatform.test_automation_platform.entity.ExecutionAnalyseQualite;
import com.testplatform.test_automation_platform.entity.Projet;
import com.testplatform.test_automation_platform.enums.StatutExecution;
import com.testplatform.test_automation_platform.repository.ExecutionAnalyseQualiteRepository;
import com.testplatform.test_automation_platform.repository.NotificationRepository;

@Service
public class ExecutionAnalyseQualiteService {

    private final ExecutionAnalyseQualiteRepository executionRepository;
    private final NotificationRepository notificationRepository;

    private final SonarQubeService sonarQubeService;
    private final MavenExecutionService mavenExecutionService;
    private final AnalyseQualiteService analyseQualiteService;
    private final NotificationService notificationService;

    public ExecutionAnalyseQualiteService(
            ExecutionAnalyseQualiteRepository executionRepository,
            NotificationRepository notificationRepository,
            SonarQubeService sonarQubeService,
            MavenExecutionService mavenExecutionService,
            AnalyseQualiteService analyseQualiteService,
            NotificationService notificationService) {

        this.executionRepository = executionRepository;
        this.notificationRepository = notificationRepository;

        this.sonarQubeService = sonarQubeService;
        this.mavenExecutionService = mavenExecutionService;
        this.analyseQualiteService = analyseQualiteService;
        this.notificationService = notificationService;
    }

    public ExecutionAnalyseQualite creerExecution(
            Projet projet) {

        ExecutionAnalyseQualite execution =
                new ExecutionAnalyseQualite();

        execution.setProjet(projet);
        execution.setStatut(
                StatutExecution.EN_ATTENTE);

        return executionRepository.save(
                execution);
    }

    public ExecutionAnalyseQualite obtenirExecutionParId(
            Long id) {

        return executionRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Exécution d'analyse qualité introuvable."));
    }

    public List<ExecutionAnalyseQualite>
    obtenirExecutionsParProjet(
            Projet projet) {

        return executionRepository.findByProjet(
                projet);
    }

    public ExecutionAnalyseQualite demarrerExecution(
            Long id) {

        ExecutionAnalyseQualite execution =
                obtenirExecutionParId(id);

        execution.setStatut(
                StatutExecution.EN_COURS);

        execution.setDateDebut(
                LocalDateTime.now());

        return executionRepository.save(
                execution);
    }

    public ExecutionAnalyseQualite terminerExecution(
            Long id,
            StatutExecution statut,
            String message) {

        ExecutionAnalyseQualite execution =
                obtenirExecutionParId(id);

        execution.setStatut(statut);
        execution.setMessage(message);

        execution.setDateFin(
                LocalDateTime.now());

        return executionRepository.save(
                execution);
    }

    /*
     * ====================================
     * SUPPRESSION
     * ====================================
     */

    @Transactional
    public void supprimerExecution(Long id) {

        if (!executionRepository.existsById(id)) {

            throw new IllegalArgumentException(
                    "Exécution d'analyse qualité introuvable.");
        }

        /*
         * IMPORTANT :
         * les notifications possèdent une clé
         * étrangère execution_id.
         *
         * Elles doivent donc être supprimées
         * avant l'exécution.
         */
        notificationRepository
                .deleteByExecutionId(id);

        /*
         * Puis suppression de
         * ExecutionAnalyseQualite.
         */
        executionRepository.deleteById(id);
    }

    /*
     * ====================================
     * EXÉCUTION ANALYSE SONARQUBE
     * ====================================
     */

    public ExecutionAnalyseQualite executerAnalyse(
            Long id) {

        ExecutionAnalyseQualite execution =
                obtenirExecutionParId(id);

        Projet projet =
                execution.getProjet();

        try {

            demarrerExecution(id);

            String projectKey =
                    projet.getProjectKey();

            // Vérifier si le projet existe
            // dans SonarQube
            if (!sonarQubeService
                    .projetExiste(projectKey)) {

                sonarQubeService.creerProjet(
                        projectKey,
                        projet.getNom());
            }

            // Affecter le Quality Gate
            sonarQubeService.affecterQualityGate(
                    projectKey);

            // Mémoriser la dernière analyse
            String ancienneCleAnalyse =
                    sonarQubeService
                            .obtenirCleDerniereAnalyse(
                                    projectKey);

            // Lancer Maven + SonarQube
            int codeRetour =
                    mavenExecutionService
                            .executerAnalyseQualite(
                                    projet.getCheminLocal(),
                                    projectKey);

            if (codeRetour == 0) {

                String statutQualityGate =
                        sonarQubeService
                                .attendreNouvelleAnalyseEtQualityGate(
                                        projectKey,
                                        ancienneCleAnalyse);

                SonarQubeService.MetriquesSonar metriques =
                        sonarQubeService
                                .obtenirMetriques(
                                        projectKey);

                List<SonarQubeService.IssueSonar> issues =
                        sonarQubeService
                                .obtenirIssues(
                                        projectKey);

                analyseQualiteService
                        .enregistrerResultatsSonar(
                                execution,
                                metriques,
                                issues,
                                statutQualityGate);

                if ("OK".equals(
                        statutQualityGate)) {

                    ExecutionAnalyseQualite resultat =
                            terminerExecution(
                                    id,
                                    StatutExecution.TERMINEE,
                                    "Analyse terminée. Quality Gate réussi.");

                    notificationService
                            .notifierFinExecution(
                                    resultat,
                                    true,
                                    resultat.getMessage());

                    return resultat;

                } else {

                    ExecutionAnalyseQualite resultat =
                            terminerExecution(
                                    id,
                                    StatutExecution.ECHOUEE,
                                    "Analyse terminée. Quality Gate échoué.");

                    notificationService
                            .notifierFinExecution(
                                    resultat,
                                    false,
                                    resultat.getMessage());

                    return resultat;
                }

            } else {

                ExecutionAnalyseQualite resultat =
                        terminerExecution(
                                id,
                                StatutExecution.ECHOUEE,
                                "L'analyse de qualité a échoué. Code retour Maven : "
                                        + codeRetour);

                notificationService
                        .notifierFinExecution(
                                resultat,
                                false,
                                resultat.getMessage());

                return resultat;
            }

        } catch (Exception e) {

            ExecutionAnalyseQualite resultat =
                    terminerExecution(
                            id,
                            StatutExecution.ECHOUEE,
                            "Erreur lors de l'analyse : "
                                    + e.getMessage());

            notificationService
                    .notifierFinExecution(
                            resultat,
                            false,
                            resultat.getMessage());

            return resultat;
        }
    }
}