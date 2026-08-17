package com.testplatform.test_automation_platform.service;

import com.testplatform.test_automation_platform.entity.Execution;
import com.testplatform.test_automation_platform.entity.ExecutionTest;
import com.testplatform.test_automation_platform.entity.Projet;
import com.testplatform.test_automation_platform.entity.Utilisateur;
import com.testplatform.test_automation_platform.enums.StatutQualityGate;
import com.testplatform.test_automation_platform.enums.StatutTest;
import com.testplatform.test_automation_platform.repository.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private final ProjetRepository projetRepository;
    private final ExecutionRepository executionRepository;
    private final ResultatTestRepository resultatTestRepository;
    private final AnalyseQualiteRepository analyseQualiteRepository;
    private final NotificationRepository notificationRepository;

    public DashboardService(
            ProjetRepository projetRepository,
            ExecutionRepository executionRepository,
            ResultatTestRepository resultatTestRepository,
            AnalyseQualiteRepository analyseQualiteRepository,
            NotificationRepository notificationRepository) {

        this.projetRepository = projetRepository;
        this.executionRepository = executionRepository;
        this.resultatTestRepository = resultatTestRepository;
        this.analyseQualiteRepository = analyseQualiteRepository;
        this.notificationRepository = notificationRepository;
    }

    public Map<String, Object> obtenirDashboard(Utilisateur utilisateur) {

        Map<String, Object> dashboard = new LinkedHashMap<>();

        long nbProjets = projetRepository.findByUtilisateur(utilisateur).size();
        long nbExecutions = executionRepository.countByProjet_Utilisateur_Id(utilisateur.getId());

        long testsReussis = resultatTestRepository.countByUtilisateurEtStatut(utilisateur.getId(), StatutTest.REUSSI);
        long testsEchoues = resultatTestRepository.countByUtilisateurEtStatut(utilisateur.getId(), StatutTest.ECHOUE);
        long testsIgnores = resultatTestRepository.countByUtilisateurEtStatut(utilisateur.getId(), StatutTest.IGNORED);

        long gatesOk = analyseQualiteRepository.countByUtilisateurEtQualityGateStatus(utilisateur.getId(), StatutQualityGate.REUSSI);
        long gatesKo = analyseQualiteRepository.countByUtilisateurEtQualityGateStatus(utilisateur.getId(), StatutQualityGate.ECHOUE);

        long notificationsNonLues = notificationRepository.findByUtilisateurAndLueFalse(utilisateur).size();

        LocalDateTime seJoursAvant = LocalDate.now().minusDays(6).atStartOfDay();
        List<Object[]> evolutionBrute = executionRepository.compterExecutionsParJour(utilisateur.getId(), seJoursAvant);

        List<Map<String, Object>> evolution = evolutionBrute.stream().map(row -> {
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("date", row[0].toString());
            point.put("total", ((Number) row[1]).longValue());
            return point;
        }).toList();

        List<Execution> dernieresExecutions = executionRepository.findDernieresExecutionsParUtilisateur(
                utilisateur.getId(), PageRequest.of(0, 5));

        List<Map<String, Object>> dernieresExecutionsListe = dernieresExecutions.stream().map(e -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("executionId", e.getId());
            item.put("projetNom", e.getProjet().getNom());
            item.put("type", e instanceof ExecutionTest ? "TESTS" : "ANALYSE_QUALITE");
            item.put("statut", e.getStatut());
            item.put("date", e.getDateDebut() != null ? e.getDateDebut().toString() : null);
            return item;
        }).toList();

        List<Projet> projets = projetRepository.findByUtilisateur(utilisateur);

        List<Map<String, Object>> projetsListe = projets.stream().map(p -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("projetId", p.getId());
            item.put("nom", p.getNom());
            item.put("typeSource", p.getTypeSource());
            item.put("statut", p.getStatut());

            List<Execution> executionsProjet = executionRepository.findByProjet(p);
            String derniereDate = executionsProjet.stream()
                    .filter(e -> e.getDateDebut() != null)
                    .max(Comparator.comparing(Execution::getDateDebut))
                    .map(e -> e.getDateDebut().toString())
                    .orElse(null);
            item.put("derniereExecution", derniereDate);

            return item;
        }).toList();

        dashboard.put("nbProjets", nbProjets);
        dashboard.put("nbExecutions", nbExecutions);
        dashboard.put("testsReussis", testsReussis);
        dashboard.put("testsEchoues", testsEchoues);
        dashboard.put("testsIgnores", testsIgnores);
        dashboard.put("gatesOk", gatesOk);
        dashboard.put("gatesKo", gatesKo);
        dashboard.put("notificationsNonLues", notificationsNonLues);
        dashboard.put("evolutionExecutions", evolution);
        dashboard.put("dernieresExecutions", dernieresExecutionsListe);
        dashboard.put("mesProjets", projetsListe);

        return dashboard;
    }
}