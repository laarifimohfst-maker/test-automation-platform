package com.testplatform.test_automation_platform.service;

import com.testplatform.test_automation_platform.entity.Execution;
import com.testplatform.test_automation_platform.entity.ExecutionAnalyseQualite;
import com.testplatform.test_automation_platform.entity.ExecutionTest;
import com.testplatform.test_automation_platform.entity.Projet;
import com.testplatform.test_automation_platform.enums.StatutExecution;
import com.testplatform.test_automation_platform.enums.TypeExecution;
import com.testplatform.test_automation_platform.repository.ExecutionRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class ExecutionService {

    private final ExecutionRepository executionRepository;
    private final ExecutionTestService executionTestService;
    private final ExecutionAnalyseQualiteService executionAnalyseQualiteService;

    public ExecutionService(
            ExecutionRepository executionRepository,
            ExecutionTestService executionTestService,
            ExecutionAnalyseQualiteService executionAnalyseQualiteService) {
        this.executionRepository = executionRepository;
        this.executionTestService = executionTestService;
        this.executionAnalyseQualiteService = executionAnalyseQualiteService;
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

    @Transactional(readOnly = true)
    public List<Execution> rechercherExecutionsAdministration(
            String recherche,
            Long utilisateurId,
            Long projetId,
            StatutExecution statut,
            TypeExecution type) {

        String texteRecherche = recherche == null
                ? ""
                : recherche.trim().toLowerCase(Locale.ROOT);

        return executionRepository
                .findAll(Sort.by(Sort.Direction.DESC, "dateDebut"))
                .stream()
                .filter(execution -> utilisateurId == null
                        || execution.getProjet()
                        .getUtilisateur()
                        .getId()
                        .equals(utilisateurId))
                .filter(execution -> projetId == null
                        || execution.getProjet().getId().equals(projetId))
                .filter(execution -> statut == null
                        || execution.getStatut() == statut)
                .filter(execution -> correspondType(execution, type))
                .filter(execution -> texteRecherche.isEmpty()
                        || contientIgnoreCase(
                                execution.getProjet().getNom(),
                                texteRecherche
                        )
                        || contientIgnoreCase(
                                execution.getProjet().getUtilisateur().getNom(),
                                texteRecherche
                        )
                        || contientIgnoreCase(
                                execution.getProjet().getUtilisateur().getEmail(),
                                texteRecherche
                        )
                        || contientIgnoreCase(
                                execution.getMessage(),
                                texteRecherche
                        )
                        || String.valueOf(execution.getId())
                        .contains(texteRecherche))
                .toList();
    }

    public Execution enregistrerExecution(Execution execution) {
        return executionRepository.save(execution);
    }

    public void supprimerExecution(Long id) {
        Execution execution = obtenirExecutionParId(id);

        if (execution.getStatut() == StatutExecution.EN_COURS) {
            throw new IllegalArgumentException(
                    "Une exécution en cours ne peut pas être supprimée."
            );
        }

        if (execution instanceof ExecutionTest) {
            executionTestService.supprimerExecution(id);
            return;
        }

        if (execution instanceof ExecutionAnalyseQualite) {
            executionAnalyseQualiteService.supprimerExecution(id);
            return;
        }

        throw new IllegalArgumentException(
                "Type d'exécution non pris en charge."
        );
    }

    public Execution mettreAJourStatut(
            Long id,
            StatutExecution statut) {

        Execution execution = obtenirExecutionParId(id);

        execution.setStatut(statut);

        return executionRepository.save(execution);
    }

    private boolean correspondType(
            Execution execution,
            TypeExecution type) {
        if (type == null) {
            return true;
        }

        return switch (type) {
            case TESTS -> execution instanceof ExecutionTest;
            case ANALYSE_QUALITE ->
                    execution instanceof ExecutionAnalyseQualite;
        };
    }

    private boolean contientIgnoreCase(String valeur, String recherche) {
        return valeur != null
                && valeur.toLowerCase(Locale.ROOT).contains(recherche);
    }
}
