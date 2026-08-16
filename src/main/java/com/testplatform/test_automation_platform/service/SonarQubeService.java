package com.testplatform.test_automation_platform.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Service
public class SonarQubeService {

    @Value("${sonar.host.url}")
    private String sonarHostUrl;

    @Value("${sonar.login}")
    private String sonarToken;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class IssueSonar {

        private String fichier;
        private Integer ligne;
        private String message;
        private String severite;
        private String type;
        private String regle;
    }

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public SonarQubeService() {
        this.restClient = RestClient.builder().build();
        this.objectMapper = new ObjectMapper();
    }

    public boolean projetExiste(String projectKey) {

        String url = sonarHostUrl
        + "/api/components/search?qualifiers=TRK&q="
        + projectKey;

        System.out.println("SONAR URL = " + url);
        System.out.println("SONAR TOKEN PRESENT = "
                + (sonarToken != null && !sonarToken.isBlank()));

        String response = restClient.get()
                .uri(url)
                .headers(headers -> headers.setBasicAuth(sonarToken, ""))
                .retrieve()
                .body(String.class);

        System.out.println("SONAR RESPONSE = " + response);

        return response != null
                && response.contains("\"key\":\"" + projectKey + "\"");
    }

    public void creerProjet(String projectKey, String projectName) {

        restClient.post()
                .uri(sonarHostUrl + "/api/projects/create")
                .headers(headers -> headers.setBasicAuth(sonarToken, ""))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(
                        "project=" + projectKey
                                + "&name=" + projectName
                )
                .retrieve()
                .toBodilessEntity();
    }

    public void affecterQualityGate(String projectKey) {
        try {
            restClient.post()
                    .uri(
                            sonarHostUrl
                                    + "/api/qualitygates/select"
                                    + "?projectKey={projectKey}&gateName={gateName}",
                            projectKey,
                            "Test Automation Quality Gate"
                    )
                    .headers(headers -> headers.setBasicAuth(sonarToken, ""))
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.Forbidden e) {
            // Un jeton global d'analyse (sqa_) ne peut pas administrer les
            // Quality Gates. SonarQube utilisera alors le gate par defaut.
            System.out.println(
                    "SONAR: affectation du Quality Gate non autorisee; "
                            + "utilisation du Quality Gate par defaut."
            );
        }
    }

    public String obtenirStatutQualityGate(String projectKey) {

        String url = sonarHostUrl
                + "/api/qualitygates/project_status?projectKey="
                + projectKey;

        String response = restClient.get()
                .uri(url)
                .headers(headers -> headers.setBasicAuth(sonarToken, ""))
                .retrieve()
                .body(String.class);

        if (response == null) {
            throw new IllegalStateException(
                    "Impossible de récupérer le statut du Quality Gate."
            );
        }

        if (response.contains("\"status\":\"OK\"")) {
            return "OK";
        }

        if (response.contains("\"status\":\"ERROR\"")) {
            return "ERROR";
        }

        return "NONE";
    }

    public String obtenirStatutQualityGateParAnalyse(String analysisId) {

        String response = restClient.get()
                .uri(
                        sonarHostUrl
                                + "/api/qualitygates/project_status"
                                + "?analysisId={analysisId}",
                        analysisId
                )
                .headers(headers -> headers.setBasicAuth(sonarToken, ""))
                .retrieve()
                .body(String.class);

        if (response == null) {
            throw new IllegalStateException(
                    "Impossible de recuperer le Quality Gate de l'analyse."
            );
        }

        try {
            return objectMapper.readTree(response)
                    .path("projectStatus")
                    .path("status")
                    .asText("NONE");
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Reponse Quality Gate SonarQube invalide.",
                    e
            );
        }
    }

    public String attendreQualityGate(String projectKey)
            throws InterruptedException {

        int nombreTentatives = 10;

        for (int i = 0; i < nombreTentatives; i++) {

            String statut =
                    obtenirStatutQualityGate(projectKey);

            if (!"NONE".equals(statut)
                    && !"UNKNOWN".equals(statut)) {

                return statut;
            }

            Thread.sleep(3000);
        }

        return "UNKNOWN";
    }

    public String obtenirCleDerniereAnalyse(String projectKey) {

        String response = restClient.get()
                .uri(
                        sonarHostUrl
                                + "/api/project_analyses/search"
                                + "?project={projectKey}&ps=1",
                        projectKey
                )
                .headers(headers -> headers.setBasicAuth(sonarToken, ""))
                .retrieve()
                .body(String.class);

        if (response == null || response.isBlank()) {
            return null;
        }

        try {
            JsonNode analyses = objectMapper.readTree(response).path("analyses");

            if (!analyses.isArray() || analyses.isEmpty()) {
                return null;
            }

            return analyses.get(0).path("key").asText(null);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Impossible de lire la derniere analyse SonarQube.",
                    e
            );
        }
    }

    public String attendreNouvelleAnalyseEtQualityGate(
            String projectKey,
            String ancienneCleAnalyse) throws InterruptedException {

        int nombreTentatives = 30;

        for (int i = 0; i < nombreTentatives; i++) {
            String nouvelleCleAnalyse =
                    obtenirCleDerniereAnalyse(projectKey);

            if (nouvelleCleAnalyse != null
                    && !nouvelleCleAnalyse.equals(ancienneCleAnalyse)) {
                return obtenirStatutQualityGateParAnalyse(
                        nouvelleCleAnalyse
                );
            }

            Thread.sleep(2000);
        }

        return "UNKNOWN";
    }

    public MetriquesSonar obtenirMetriques(String projectKey) {

        String response = restClient.get()
                .uri(
                        sonarHostUrl
                                + "/api/measures/component"
                                + "?component={projectKey}"
                                + "&metricKeys=bugs,vulnerabilities,code_smells,"
                                + "duplicated_lines_density,coverage",
                        projectKey
                )
                .headers(headers -> headers.setBasicAuth(sonarToken, ""))
                .retrieve()
                .body(String.class);

        if (response == null || response.isBlank()) {
            throw new IllegalStateException(
                    "Impossible de recuperer les metriques SonarQube."
            );
        }

        try {
            JsonNode measures = objectMapper.readTree(response)
                    .path("component")
                    .path("measures");

            MetriquesSonar metriques = new MetriquesSonar();

            for (JsonNode measure : measures) {
                String metric = measure.path("metric").asText();
                String value = measure.path("value").asText("0");

                switch (metric) {
                    case "bugs" -> metriques.setBugs(parseInteger(value));
                    case "vulnerabilities" ->
                            metriques.setVulnerabilites(parseInteger(value));
                    case "code_smells" ->
                            metriques.setCodeSmells(parseInteger(value));
                    case "duplicated_lines_density" ->
                            metriques.setDuplication(parseDouble(value));
                    case "coverage" ->
                            metriques.setCoverage(parseDouble(value));
                    default -> {
                        // Les autres metriques ne sont pas stockees.
                    }
                }
            }

            return metriques;
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Reponse SonarQube invalide : " + e.getMessage(),
                    e
            );
        }
    }

    private Integer parseInteger(String value) {
        return (int) Math.round(Double.parseDouble(value));
    }

    private Double parseDouble(String value) {
        return Double.parseDouble(value);
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MetriquesSonar {

        private Integer bugs = 0;
        private Integer vulnerabilites = 0;
        private Integer codeSmells = 0;
        private Double duplication = 0.0;
        private Double coverage = 0.0;
    }

    public List<IssueSonar> obtenirIssues(String projectKey) {

        String response = restClient.get()
                .uri(
                        sonarHostUrl
                                + "/api/issues/search"
                                + "?componentKeys={projectKey}"
                                + "&statuses=OPEN,CONFIRMED,REOPENED"
                                + "&ps=500",
                        projectKey
                )
                .headers(headers -> headers.setBasicAuth(sonarToken, ""))
                .retrieve()
                .body(String.class);

        if (response == null || response.isBlank()) {
            return List.of();
        }

        try {
            JsonNode issuesNode = objectMapper.readTree(response).path("issues");
            List<IssueSonar> issues = new ArrayList<>();

            for (JsonNode issue : issuesNode) {
                String component = issue.path("component").asText("");
                String fichier = component.contains(":")
                        ? component.substring(component.indexOf(":") + 1)
                        : component;

                issues.add(IssueSonar.builder()
                        .fichier(fichier)
                        .ligne(issue.path("line").isMissingNode() ? null : issue.path("line").asInt())
                        .message(issue.path("message").asText(""))
                        .severite(issue.path("severity").asText(""))
                        .type(issue.path("type").asText(""))
                        .regle(issue.path("rule").asText(""))
                        .build());
            }

            return issues;

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Reponse SonarQube invalide (issues) : " + e.getMessage(), e
            );
        }
    }
}
