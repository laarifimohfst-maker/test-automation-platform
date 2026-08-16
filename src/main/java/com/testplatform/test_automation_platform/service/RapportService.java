package com.testplatform.test_automation_platform.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.testplatform.test_automation_platform.entity.Execution;
import com.testplatform.test_automation_platform.entity.ExecutionAnalyseQualite;
import com.testplatform.test_automation_platform.enums.StatutQualityGate;
import com.testplatform.test_automation_platform.entity.ExecutionTest;
import com.testplatform.test_automation_platform.entity.Rapport;
import com.testplatform.test_automation_platform.entity.ResultatTest;
import com.testplatform.test_automation_platform.enums.StatutTest;
import com.testplatform.test_automation_platform.enums.TypeRapport;
import com.testplatform.test_automation_platform.enums.TypeTest;
import com.testplatform.test_automation_platform.repository.ExecutionRepository;
import com.testplatform.test_automation_platform.repository.RapportRepository;
import com.testplatform.test_automation_platform.repository.ResultatTestRepository;
import org.springframework.stereotype.Service;
import com.testplatform.test_automation_platform.entity.AnalyseQualite;
import com.testplatform.test_automation_platform.repository.AnalyseQualiteRepository;
import org.springframework.core.io.ClassPathResource;
import java.nio.charset.StandardCharsets;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class RapportService {

    private final RapportRepository rapportRepository;
    private final ExecutionRepository executionRepository;
    private final ResultatTestRepository resultatTestRepository;
    private final AnalyseQualiteRepository analyseQualiteRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PdfGeneratorService pdfGeneratorService;

    public RapportService(
            RapportRepository rapportRepository,
            ExecutionRepository executionRepository,
            ResultatTestRepository resultatTestRepository,
            AnalyseQualiteRepository analyseQualiteRepository,
            PdfGeneratorService pdfGeneratorService) {

        this.rapportRepository = rapportRepository;
        this.executionRepository = executionRepository;
        this.resultatTestRepository = resultatTestRepository;
        this.analyseQualiteRepository = analyseQualiteRepository;
        this.pdfGeneratorService = pdfGeneratorService;
    }

    public Rapport enregistrerRapport(Rapport rapport, Long executionId) {

        Execution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new IllegalArgumentException("Exécution introuvable."));

        rapport.setExecution(execution);

        if (execution instanceof ExecutionTest) {
            rapport.setType(TypeRapport.TESTS);
        } else if (execution instanceof ExecutionAnalyseQualite) {
            rapport.setType(TypeRapport.ANALYSE_QUALITE);
        } else {
            throw new IllegalArgumentException("Type d'exécution non pris en charge.");
        }

        if (rapport.getDateGeneration() == null) {
            rapport.setDateGeneration(LocalDateTime.now());
        }

        return rapportRepository.save(rapport);
    }

    public Rapport obtenirRapportParId(Long id) {
        return rapportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rapport introuvable."));
    }

    public List<Rapport> obtenirRapportsParType(TypeRapport type) {
        return rapportRepository.findByType(type);
    }

    public List<Rapport> obtenirTousLesRapports() {
        return rapportRepository.findAll();
    }

    public Rapport modifierRapport(Long id, Rapport rapportModifie) {
        Rapport rapport = obtenirRapportParId(id);

        rapport.setNom(rapportModifie.getNom());
        rapport.setCheminFichier(rapportModifie.getCheminFichier());
        rapport.setTaille(rapportModifie.getTaille());

        return rapportRepository.save(rapport);
    }

    public void supprimerRapport(Long id) {
        if (!rapportRepository.existsById(id)) {
            throw new IllegalArgumentException("Rapport introuvable.");
        }
        rapportRepository.deleteById(id);
    }

    public Rapport genererRapportTests(Long executionId) {

        Execution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new RuntimeException("Execution introuvable"));

        if (!(execution instanceof ExecutionTest)) {
            throw new RuntimeException("Cette exécution n'est pas une exécution de tests");
        }

        List<ResultatTest> resultats = resultatTestRepository.findByExecutionTest_Id(executionId);

        if (resultats.isEmpty()) {
            throw new RuntimeException("Aucun résultat de test pour cette exécution");
        }

        long total = resultats.size();
        long reussis = resultats.stream().filter(r -> r.getStatut() == StatutTest.REUSSI).count();
        long echoues = resultats.stream().filter(r -> r.getStatut() == StatutTest.ECHOUE).count();
        long ignores = resultats.stream().filter(r -> r.getStatut() == StatutTest.IGNORED).count();
        long dureeTotale = resultats.stream().mapToLong(ResultatTest::getDuree).sum();

        Map<TypeTest, Long> parType = resultats.stream()
                .collect(Collectors.groupingBy(ResultatTest::getType, Collectors.counting()));

        List<Map<String, Object>> testsEchoues = resultats.stream()
                .filter(r -> r.getStatut() == StatutTest.ECHOUE)
                .map(r -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("nomTest", r.getNomTest());
                    map.put("type", r.getType());
                    map.put("message", r.getMessage());
                    return map;
                })
                .collect(Collectors.toList());

        String dateExecutionStr = execution.getDateDebut() != null ? execution.getDateDebut().toString() : null;

        // 1. Construire le JSON (contenu)
        Map<String, Object> contenuMap = new LinkedHashMap<>();
        contenuMap.put("executionId", executionId);
        contenuMap.put("dateExecution", dateExecutionStr);
        contenuMap.put("total", total);
        contenuMap.put("reussis", reussis);
        contenuMap.put("echoues", echoues);
        contenuMap.put("ignores", ignores);
        contenuMap.put("dureeTotaleMs", dureeTotale);
        contenuMap.put("parType", parType);
        contenuMap.put("testsEchoues", testsEchoues);

        String contenuJson;
        try {
            contenuJson = objectMapper.writeValueAsString(contenuMap);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du JSON du rapport", e);
        }

        // 2. Construire le HTML rempli
        String html = construireHtmlRapportTests(
                executionId, dateExecutionStr, total, reussis, echoues, ignores,
                dureeTotale, parType, testsEchoues
        );

        // 3. Convertir en PDF
        byte[] pdfBytes = pdfGeneratorService.genererPdfDepuisHtml(html);

        // 4. Sauvegarder le PDF sur le disque
        String nomFichier = "rapport_tests_execution_" + executionId + "_" + System.currentTimeMillis() + ".pdf";
        String cheminFichier = sauvegarderPdf(pdfBytes, nomFichier);

        // 5. Créer et sauvegarder le Rapport complet
        Rapport rapport = Rapport.builder()
                .nom("Rapport tests - Execution #" + executionId)
                .type(TypeRapport.TESTS)
                .dateGeneration(LocalDateTime.now())
                .execution(execution)
                .contenu(contenuJson)
                .cheminFichier(cheminFichier)
                .taille((long) pdfBytes.length)
                .build();

        return rapportRepository.save(rapport);
    }

    public Rapport genererRapportAnalyseQualite(Long executionId) {

        Execution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new RuntimeException("Execution introuvable"));

        if (!(execution instanceof ExecutionAnalyseQualite)) {
            throw new RuntimeException("Cette exécution n'est pas une exécution d'analyse qualité");
        }

        AnalyseQualite analyse = analyseQualiteRepository.findByExecutionAnalyseQualite_Id(executionId)
                .orElseThrow(() -> new RuntimeException("Aucune analyse qualité pour cette exécution"));

        String dateExecutionStr = execution.getDateDebut() != null ? execution.getDateDebut().toString() : null;

        // Parser les issues stockées en JSON pour les remettre en objets Java
        List<Map<String, Object>> issuesList;
        try {
            if (analyse.getIssuesJson() != null && !analyse.getIssuesJson().isBlank()) {
                issuesList = objectMapper.readValue(
                        analyse.getIssuesJson(),
                        new TypeReference<List<Map<String, Object>>>() {}
                );
            } else {
                issuesList = List.of();
            }
        } catch (Exception e) {
            issuesList = List.of();
        }

        // 1. Construire le JSON (contenu)
        Map<String, Object> contenuMap = new LinkedHashMap<>();
        contenuMap.put("executionId", executionId);
        contenuMap.put("dateExecution", dateExecutionStr);
        contenuMap.put("bugs", analyse.getBugs());
        contenuMap.put("vulnerabilites", analyse.getVulnerabilites());
        contenuMap.put("codeSmells", analyse.getCodeSmells());
        contenuMap.put("duplication", analyse.getDuplication());
        contenuMap.put("coverage", analyse.getCoverage());
        contenuMap.put("qualityGateStatus", analyse.getQualityGateStatus());
        contenuMap.put("dateAnalyse", analyse.getDateAnalyse() != null ? analyse.getDateAnalyse().toString() : null);
        contenuMap.put("issues", issuesList);   // <-- nouveau

        String contenuJson;
        try {
            contenuJson = objectMapper.writeValueAsString(contenuMap);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du JSON du rapport", e);
        }

        // 2. Construire le HTML rempli
        String html = construireHtmlRapportAnalyseQualite(executionId, dateExecutionStr, analyse, issuesList);

        // 3. Convertir en PDF
        byte[] pdfBytes = pdfGeneratorService.genererPdfDepuisHtml(html);

        // 4. Sauvegarder le PDF sur le disque
        String nomFichier = "rapport_qualite_execution_" + executionId + "_" + System.currentTimeMillis() + ".pdf";
        String cheminFichier = sauvegarderPdf(pdfBytes, nomFichier);

        // 5. Créer et sauvegarder le Rapport complet
        Rapport rapport = Rapport.builder()
                .nom("Rapport qualité - Execution #" + executionId)
                .type(TypeRapport.ANALYSE_QUALITE)
                .dateGeneration(LocalDateTime.now())
                .execution(execution)
                .contenu(contenuJson)
                .cheminFichier(cheminFichier)
                .taille((long) pdfBytes.length)
                .build();

        return rapportRepository.save(rapport);
    }

    private String lireTemplate(String nomFichier) {
        try {
            ClassPathResource resource = new ClassPathResource("templates-pdf/" + nomFichier);
            return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Impossible de lire le template " + nomFichier, e);
        }
    }

    private String construireHtmlRapportTests(
            Long executionId,
            String dateExecution,
            long total,
            long reussis,
            long echoues,
            long ignores,
            long dureeTotale,
            Map<TypeTest, Long> parType,
            List<Map<String, Object>> testsEchoues) {

        String html = lireTemplate("rapport-tests.html");

        String parTypeTexte = parType.entrySet().stream()
                .map(e -> e.getKey() + " : " + e.getValue())
                .collect(Collectors.joining(", "));

        String blocEchecs;
        if (testsEchoues.isEmpty()) {
            blocEchecs = "<p class=\"aucun-echec\">Aucun test échoué.</p>";
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("<table class=\"echecs\"><tr><th>Nom du test</th><th>Type</th><th>Message</th></tr>");
            for (Map<String, Object> t : testsEchoues) {
                sb.append("<tr><td>").append(t.get("nomTest")).append("</td>")
                        .append("<td>").append(t.get("type")).append("</td>")
                        .append("<td>").append(t.get("message")).append("</td></tr>");
            }
            sb.append("</table>");
            blocEchecs = sb.toString();
        }

        return html
                .replace("${executionId}", String.valueOf(executionId))
                .replace("${dateExecution}", dateExecution != null ? dateExecution : "")
                .replace("${dateGeneration}", LocalDateTime.now().toString())
                .replace("${total}", String.valueOf(total))
                .replace("${reussis}", String.valueOf(reussis))
                .replace("${echoues}", String.valueOf(echoues))
                .replace("${ignores}", String.valueOf(ignores))
                .replace("${dureeTotaleMs}", String.valueOf(dureeTotale))
                .replace("${parTypeTexte}", parTypeTexte)
                .replace("${blocEchecs}", blocEchecs);
    }

    private String sauvegarderPdf(byte[] pdfBytes, String nomFichier) {
        try {
            Path dossier = Paths.get("reports");
            if (!Files.exists(dossier)) {
                Files.createDirectories(dossier);
            }

            Path chemin = dossier.resolve(nomFichier);
            Files.write(chemin, pdfBytes);

            return chemin.toString();

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la sauvegarde du PDF", e);
        }
    }

    private String construireHtmlRapportAnalyseQualite(
            Long executionId,
            String dateExecution,
            AnalyseQualite analyse,
            List<Map<String, Object>> issuesList) {

        String html = lireTemplate("rapport-analyse-qualite.html");

        String qualityGateClass = analyse.getQualityGateStatus() == StatutQualityGate.REUSSI
                ? "gate-reussi" : "gate-echoue";

        String blocIssues;
        if (issuesList.isEmpty()) {
            blocIssues = "<p class=\"aucun-echec\">Aucune issue détectée.</p>";
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("<table class=\"echecs\"><tr><th>Fichier</th><th>Ligne</th><th>Type</th><th>Sévérité</th><th>Message</th></tr>");
            for (Map<String, Object> issue : issuesList) {
                sb.append("<tr><td>").append(issue.get("fichier")).append("</td>")
                        .append("<td>").append(issue.get("ligne")).append("</td>")
                        .append("<td>").append(issue.get("type")).append("</td>")
                        .append("<td>").append(issue.get("severite")).append("</td>")
                        .append("<td>").append(issue.get("message")).append("</td></tr>");
            }
            sb.append("</table>");
            blocIssues = sb.toString();
        }

        return html
                .replace("${executionId}", String.valueOf(executionId))
                .replace("${dateExecution}", dateExecution != null ? dateExecution : "")
                .replace("${dateAnalyse}", analyse.getDateAnalyse() != null ? analyse.getDateAnalyse().toString() : "")
                .replace("${dateGeneration}", LocalDateTime.now().toString())
                .replace("${bugs}", String.valueOf(analyse.getBugs()))
                .replace("${vulnerabilites}", String.valueOf(analyse.getVulnerabilites()))
                .replace("${codeSmells}", String.valueOf(analyse.getCodeSmells()))
                .replace("${duplication}", String.valueOf(analyse.getDuplication()))
                .replace("${coverage}", String.valueOf(analyse.getCoverage()))
                .replace("${qualityGateStatus}", String.valueOf(analyse.getQualityGateStatus()))
                .replace("${qualityGateClass}", qualityGateClass)
                .replace("${blocIssues}", blocIssues);
    }
}