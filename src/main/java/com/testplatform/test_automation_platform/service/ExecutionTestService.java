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

    public ExecutionTestService(
            ExecutionTestRepository executionTestRepository) {
        this.executionTestRepository = executionTestRepository;
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
}