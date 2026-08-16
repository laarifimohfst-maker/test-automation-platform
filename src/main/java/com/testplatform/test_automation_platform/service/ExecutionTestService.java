package com.testplatform.test_automation_platform.service;

import com.testplatform.test_automation_platform.entity.ConfigurationTest;
import com.testplatform.test_automation_platform.entity.ExecutionTest;
import com.testplatform.test_automation_platform.entity.Projet;
import com.testplatform.test_automation_platform.enums.StatutExecution;
import com.testplatform.test_automation_platform.repository.ExecutionTestRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ExecutionTestService {

    private final ExecutionTestRepository executionTestRepository;
    private final MavenExecutionService mavenExecutionService;
    private final ResultatTestService resultatTestService;
    private final NotificationService notificationService;

    public ExecutionTestService(
            ExecutionTestRepository executionTestRepository,
            MavenExecutionService mavenExecutionService,
            ResultatTestService resultatTestService,
            NotificationService notificationService) {

        this.executionTestRepository = executionTestRepository;
        this.mavenExecutionService = mavenExecutionService;
        this.resultatTestService = resultatTestService;
        this.notificationService = notificationService;
    }

    public ExecutionTest creerExecution(
            Projet projet,
            ConfigurationTest configurationTest) {

        ExecutionTest execution = new ExecutionTest();

        execution.setProjet(projet);
        execution.setConfigurationTest(configurationTest);
        execution.setStatut(StatutExecution.EN_ATTENTE);
        execution.setDateDebut(LocalDateTime.now());

        return executionTestRepository.save(execution);
    }

    public ExecutionTest lancerTest(
            Projet projet,
            ConfigurationTest configurationTest) throws Exception {

        ExecutionTest execution = creerExecution(projet, configurationTest);

        return executerTests(execution.getId());
    }

    public ExecutionTest obtenirExecutionParId(Long id) {
        return executionTestRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Exécution des tests introuvable."));
    }

    public List<ExecutionTest> obtenirExecutionsParProjet(
            Projet projet) {

        return executionTestRepository.findByProjet(projet);
    }

    public ExecutionTest demarrerExecution(Long id) {

        ExecutionTest execution = obtenirExecutionParId(id);

        execution.setStatut(StatutExecution.EN_COURS);
        execution.setDateDebut(LocalDateTime.now());

        return executionTestRepository.save(execution);
    }

    public ExecutionTest terminerExecution(
            Long id,
            StatutExecution statut,
            String message) {

        ExecutionTest execution = obtenirExecutionParId(id);

        execution.setStatut(statut);
        execution.setMessage(message);
        execution.setDateFin(LocalDateTime.now());

        return executionTestRepository.save(execution);
    }

    public void supprimerExecution(Long id) {

        if (!executionTestRepository.existsById(id)) {
            throw new IllegalArgumentException(
                    "Exécution des tests introuvable.");
        }

        executionTestRepository.deleteById(id);
    }

    public ExecutionTest executerTests(Long id) throws Exception {

        ExecutionTest execution = obtenirExecutionParId(id);

        ConfigurationTest configuration = execution.getConfigurationTest();
        Projet projet = execution.getProjet();
        String cheminProjet = projet.getCheminLocal();

        execution.setStatut(StatutExecution.EN_COURS);
        execution.setDateDebut(LocalDateTime.now());
        executionTestRepository.save(execution);

        int codeRetour = mavenExecutionService.executerTests(cheminProjet, configuration);

        boolean succes = (codeRetour == 0);

        if (succes) {
            execution.setStatut(StatutExecution.TERMINEE);
            execution.setMessage("Les tests ont été exécutés avec succès.");
        } else {
            execution.setStatut(StatutExecution.ECHOUEE);
            execution.setMessage("Les tests ont échoué. Code retour Maven : " + codeRetour);
        }

        execution.setDateFin(LocalDateTime.now());
        executionTestRepository.save(execution);

        resultatTestService.lireEtEnregistrerResultats(cheminProjet, execution);

        // Notification automatique de fin d'exécution
        notificationService.notifierFinExecution(execution, succes, execution.getMessage());

        return execution;
    }

}
