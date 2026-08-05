package com.testplatform.test_automation_platform.service;

import com.testplatform.test_automation_platform.entity.AnalyseQualite;
import com.testplatform.test_automation_platform.entity.ExecutionAnalyseQualite;
import com.testplatform.test_automation_platform.repository.AnalyseQualiteRepository;
import org.springframework.stereotype.Service;

@Service
public class AnalyseQualiteService {

    private final AnalyseQualiteRepository analyseQualiteRepository;

    public AnalyseQualiteService(
            AnalyseQualiteRepository analyseQualiteRepository) {
        this.analyseQualiteRepository = analyseQualiteRepository;
    }

    public AnalyseQualite enregistrerAnalyse(
            AnalyseQualite analyseQualite) {

        return analyseQualiteRepository.save(analyseQualite);
    }

    public AnalyseQualite obtenirAnalyseParId(Long id) {

        return analyseQualiteRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Analyse qualité introuvable."));
    }

    public AnalyseQualite obtenirAnalyseParExecution(
            ExecutionAnalyseQualite execution) {

        return analyseQualiteRepository
                .findByExecutionAnalyseQualite(execution)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Aucune analyse qualité trouvée pour cette exécution."));
    }

    public AnalyseQualite modifierAnalyse(
            Long id,
            AnalyseQualite analyseModifiee) {

        AnalyseQualite analyse = obtenirAnalyseParId(id);

        analyse.setScore(analyseModifiee.getScore());
        analyse.setBugs(analyseModifiee.getBugs());
        analyse.setVulnerabilites(
                analyseModifiee.getVulnerabilites());
        analyse.setCodeSmells(
                analyseModifiee.getCodeSmells());
        analyse.setDuplication(
                analyseModifiee.getDuplication());
        analyse.setCoverage(
                analyseModifiee.getCoverage());
        analyse.setQualityGateStatus(
                analyseModifiee.getQualityGateStatus());
        analyse.setDateAnalyse(
                analyseModifiee.getDateAnalyse());

        return analyseQualiteRepository.save(analyse);
    }

    public void supprimerAnalyse(Long id) {

        if (!analyseQualiteRepository.existsById(id)) {
            throw new IllegalArgumentException(
                    "Analyse qualité introuvable.");
        }

        analyseQualiteRepository.deleteById(id);
    }
}