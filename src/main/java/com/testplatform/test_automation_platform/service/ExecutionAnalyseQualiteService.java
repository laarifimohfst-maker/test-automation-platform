package com.testplatform.test_automation_platform.service;

import com.testplatform.test_automation_platform.entity.ExecutionAnalyseQualite;
import com.testplatform.test_automation_platform.entity.Projet;
import com.testplatform.test_automation_platform.enums.StatutExecution;
import com.testplatform.test_automation_platform.repository.ExecutionAnalyseQualiteRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ExecutionAnalyseQualiteService {

    private final ExecutionAnalyseQualiteRepository executionRepository;
    private final SonarQubeService sonarQubeService;
    private final MavenExecutionService mavenExecutionService;
    private final AnalyseQualiteService analyseQualiteService;

    public ExecutionAnalyseQualiteService(
            ExecutionAnalyseQualiteRepository executionRepository,
            SonarQubeService sonarQubeService,
            MavenExecutionService mavenExecutionService,
            AnalyseQualiteService analyseQualiteService) {

        this.executionRepository = executionRepository;
        this.sonarQubeService = sonarQubeService;
        this.mavenExecutionService = mavenExecutionService;
        this.analyseQualiteService = analyseQualiteService;
    }

    public ExecutionAnalyseQualite creerExecution(Projet projet) {

        ExecutionAnalyseQualite execution = new ExecutionAnalyseQualite();

        execution.setProjet(projet);
        execution.setStatut(StatutExecution.EN_ATTENTE);

        return executionRepository.save(execution);
    }

    public ExecutionAnalyseQualite obtenirExecutionParId(Long id) {

        return executionRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Exécution d'analyse qualité introuvable."));
    }

    public List<ExecutionAnalyseQualite> obtenirExecutionsParProjet(
            Projet projet) {

        return executionRepository.findByProjet(projet);
    }

    public ExecutionAnalyseQualite demarrerExecution(Long id) {

        ExecutionAnalyseQualite execution =
                obtenirExecutionParId(id);

        execution.setStatut(StatutExecution.EN_COURS);
        execution.setDateDebut(LocalDateTime.now());

        return executionRepository.save(execution);
    }

    public ExecutionAnalyseQualite terminerExecution(
            Long id,
            StatutExecution statut,
            String message) {

        ExecutionAnalyseQualite execution =
                obtenirExecutionParId(id);

        execution.setStatut(statut);
        execution.setMessage(message);
        execution.setDateFin(LocalDateTime.now());

        return executionRepository.save(execution);
    }

    public void supprimerExecution(Long id) {

        if (!executionRepository.existsById(id)) {
            throw new IllegalArgumentException(
                    "Exécution d'analyse qualité introuvable.");
        }

        executionRepository.deleteById(id);
    }

    public ExecutionAnalyseQualite executerAnalyse(Long id) {

        ExecutionAnalyseQualite execution =
                obtenirExecutionParId(id);

        Projet projet = execution.getProjet();

        try {

            demarrerExecution(id);

            String projectKey = projet.getProjectKey();

            // Vérifier si le projet existe dans SonarQube
            if (!sonarQubeService.projetExiste(projectKey)) {

                // Créer le projet dans SonarQube
                sonarQubeService.creerProjet(
                        projectKey,
                        projet.getNom()
                );

            }

            // Affecter le Quality Gate, y compris lorsque le projet existe deja.
            sonarQubeService.affecterQualityGate(projectKey);

            // Memoriser la derniere analyse afin de ne pas reutiliser son statut.
            String ancienneCleAnalyse =
                    sonarQubeService.obtenirCleDerniereAnalyse(projectKey);

            // Lancer l'analyse Maven + SonarQube
            int codeRetour =
                    mavenExecutionService.executerAnalyseQualite(
                            projet.getCheminLocal(),
                            projectKey
                    );

            if (codeRetour == 0) {

                String statutQualityGate =
                        sonarQubeService.attendreNouvelleAnalyseEtQualityGate(
                                projectKey,
                                ancienneCleAnalyse
                        );

                SonarQubeService.MetriquesSonar metriques =
                        sonarQubeService.obtenirMetriques(projectKey);

                analyseQualiteService.enregistrerResultatsSonar(
                        execution,
                        metriques,
                        statutQualityGate
                );

                if ("OK".equals(statutQualityGate)) {

                    return terminerExecution(
                            id,
                            StatutExecution.TERMINEE,
                            "Analyse terminée. Quality Gate réussi."
                    );

                } else {

                    return terminerExecution(
                            id,
                            StatutExecution.ECHOUEE,
                            "Analyse terminée. Quality Gate échoué."
                    );
                }

            } else {

                return terminerExecution(
                        id,
                        StatutExecution.ECHOUEE,
                        "L'analyse de qualité a échoué. Code retour Maven : "
                                + codeRetour
                );
            }

        } catch (Exception e) {

            return terminerExecution(
                    id,
                    StatutExecution.ECHOUEE,
                    "Erreur lors de l'analyse : " + e.getMessage()
            );
        }
    }
}
