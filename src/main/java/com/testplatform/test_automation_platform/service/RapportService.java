package com.testplatform.test_automation_platform.service;

import com.testplatform.test_automation_platform.entity.Execution;
import com.testplatform.test_automation_platform.entity.ExecutionAnalyseQualite;
import com.testplatform.test_automation_platform.entity.ExecutionTest;
import com.testplatform.test_automation_platform.entity.Rapport;
import com.testplatform.test_automation_platform.enums.TypeRapport;
import com.testplatform.test_automation_platform.repository.ExecutionRepository;
import com.testplatform.test_automation_platform.repository.RapportRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RapportService {

    private final RapportRepository rapportRepository;
    private final ExecutionRepository executionRepository;

    public RapportService(
            RapportRepository rapportRepository,
            ExecutionRepository executionRepository) {

        this.rapportRepository = rapportRepository;
        this.executionRepository = executionRepository;
    }

    public Rapport enregistrerRapport(
            Rapport rapport,
            Long executionId) {

        Execution execution = executionRepository.findById(executionId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Exécution introuvable."));

        rapport.setExecution(execution);

        // Détermination automatique du type du rapport
        if (execution instanceof ExecutionTest) {
            rapport.setType(TypeRapport.TESTS);
        } else if (execution instanceof ExecutionAnalyseQualite) {
            rapport.setType(TypeRapport.ANALYSE_QUALITE);
        } else {
            throw new IllegalArgumentException(
                    "Type d'exécution non pris en charge.");
        }

        // Date de génération automatique
        if (rapport.getDateGeneration() == null) {
            rapport.setDateGeneration(LocalDateTime.now());
        }

        return rapportRepository.save(rapport);
    }

    public Rapport obtenirRapportParId(Long id) {

        return rapportRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Rapport introuvable."));
    }

    public List<Rapport> obtenirRapportsParType(TypeRapport type) {
        return rapportRepository.findByType(type);
    }

    public List<Rapport> obtenirTousLesRapports() {
        return rapportRepository.findAll();
    }

    public Rapport modifierRapport(
            Long id,
            Rapport rapportModifie) {

        Rapport rapport = obtenirRapportParId(id);

        rapport.setNom(rapportModifie.getNom());
        rapport.setCheminFichier(rapportModifie.getCheminFichier());
        rapport.setTaille(rapportModifie.getTaille());

        // On ne modifie ni le type, ni l'exécution, ni la date de génération

        return rapportRepository.save(rapport);
    }

    public void supprimerRapport(Long id) {

        if (!rapportRepository.existsById(id)) {
            throw new IllegalArgumentException(
                    "Rapport introuvable.");
        }

        rapportRepository.deleteById(id);
    }
}