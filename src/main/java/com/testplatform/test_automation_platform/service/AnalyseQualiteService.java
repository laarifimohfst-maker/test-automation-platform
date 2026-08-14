package com.testplatform.test_automation_platform.service;

import com.testplatform.test_automation_platform.entity.AnalyseQualite;
import com.testplatform.test_automation_platform.entity.ExecutionAnalyseQualite;
import com.testplatform.test_automation_platform.enums.StatutQualityGate;
import com.testplatform.test_automation_platform.repository.AnalyseQualiteRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Base64;

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

    public AnalyseQualite enregistrerResultatsSonar(
            ExecutionAnalyseQualite execution,
            SonarQubeService.MetriquesSonar metriques,
            String statutQualityGate) {

        AnalyseQualite analyse = analyseQualiteRepository
                .findByExecutionAnalyseQualite(execution)
                .orElseGet(AnalyseQualite::new);

        analyse.setBugs(metriques.getBugs());
        analyse.setVulnerabilites(metriques.getVulnerabilites());
        analyse.setCodeSmells(metriques.getCodeSmells());
        analyse.setDuplication(metriques.getDuplication());
        analyse.setCoverage(metriques.getCoverage());
        analyse.setQualityGateStatus(
                "OK".equals(statutQualityGate)
                        ? StatutQualityGate.REUSSI
                        : StatutQualityGate.ECHOUE
        );
        analyse.setDateAnalyse(LocalDateTime.now());
        analyse.setExecutionAnalyseQualite(execution);

        return analyseQualiteRepository.save(analyse);
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
