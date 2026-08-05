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

    public ExecutionAnalyseQualiteService(
            ExecutionAnalyseQualiteRepository executionRepository) {
        this.executionRepository = executionRepository;
    }

    public ExecutionAnalyseQualite creerExecution(Projet projet) {

        ExecutionAnalyseQualite execution =
                new ExecutionAnalyseQualite();

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
}