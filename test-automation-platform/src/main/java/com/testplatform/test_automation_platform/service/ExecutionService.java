package com.testplatform.test_automation_platform.service;

import com.testplatform.test_automation_platform.entity.Execution;
import com.testplatform.test_automation_platform.entity.Projet;
import com.testplatform.test_automation_platform.enums.StatutExecution;
import com.testplatform.test_automation_platform.repository.ExecutionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExecutionService {

    private final ExecutionRepository executionRepository;

    public ExecutionService(ExecutionRepository executionRepository) {
        this.executionRepository = executionRepository;
    }

    public Execution obtenirExecutionParId(Long id) {
        return executionRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Exécution introuvable."));
    }

    public List<Execution> obtenirExecutionsParProjet(Projet projet) {
        return executionRepository.findByProjet(projet);
    }

    public List<Execution> obtenirToutesLesExecutions() {
        return executionRepository.findAll();
    }

    public Execution enregistrerExecution(Execution execution) {
        return executionRepository.save(execution);
    }

    public void supprimerExecution(Long id) {

        if (!executionRepository.existsById(id)) {
            throw new IllegalArgumentException("Exécution introuvable.");
        }

        executionRepository.deleteById(id);
    }

    public Execution mettreAJourStatut(
            Long id,
            StatutExecution statut) {

        Execution execution = obtenirExecutionParId(id);

        execution.setStatut(statut);

        return executionRepository.save(execution);
    }
}