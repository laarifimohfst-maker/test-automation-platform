package com.testplatform.test_automation_platform.service;

import com.testplatform.test_automation_platform.entity.Execution;
import com.testplatform.test_automation_platform.entity.ExecutionTest;
import com.testplatform.test_automation_platform.entity.Projet;
import com.testplatform.test_automation_platform.entity.Rapport;
import com.testplatform.test_automation_platform.entity.Utilisateur;
import com.testplatform.test_automation_platform.enums.Role;
import com.testplatform.test_automation_platform.enums.StatutExecution;
import com.testplatform.test_automation_platform.enums.StatutProjet;
import com.testplatform.test_automation_platform.enums.StatutTest;
import com.testplatform.test_automation_platform.enums.TypeRapport;
import com.testplatform.test_automation_platform.repository.ExecutionRepository;
import com.testplatform.test_automation_platform.repository.ProjetRepository;
import com.testplatform.test_automation_platform.repository.RapportRepository;
import com.testplatform.test_automation_platform.repository.ResultatTestRepository;
import com.testplatform.test_automation_platform.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminDashboardService {

    private final UtilisateurRepository utilisateurRepository;
    private final ProjetRepository projetRepository;
    private final ExecutionRepository executionRepository;
    private final ResultatTestRepository resultatTestRepository;
    private final RapportRepository rapportRepository;

    public AdminDashboardService(
            UtilisateurRepository utilisateurRepository,
            ProjetRepository projetRepository,
            ExecutionRepository executionRepository,
            ResultatTestRepository resultatTestRepository,
            RapportRepository rapportRepository) {
        this.utilisateurRepository = utilisateurRepository;
        this.projetRepository = projetRepository;
        this.executionRepository = executionRepository;
        this.resultatTestRepository = resultatTestRepository;
        this.rapportRepository = rapportRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> obtenirDashboard() {
        Map<String, Object> dashboard = new LinkedHashMap<>();
        dashboard.put("utilisateurs", statistiquesUtilisateurs());
        dashboard.put("projets", statistiquesProjets());
        dashboard.put("executions", statistiquesExecutions());
        dashboard.put("tests", statistiquesTests());
        dashboard.put("rapports", statistiquesRapports());
        dashboard.put("activiteRecente", activiteRecente());
        return dashboard;
    }

    private Map<String, Object> statistiquesUtilisateurs() {
        Map<String, Object> statistiques = new LinkedHashMap<>();
        statistiques.put("total", utilisateurRepository.count());
        statistiques.put("administrateurs", utilisateurRepository.countByRole(Role.ADMIN));
        statistiques.put("developpeurs", utilisateurRepository.countByRole(Role.DEVELOPPEUR));
        statistiques.put("actifs", utilisateurRepository.countByActifTrue());
        statistiques.put("inactifs", utilisateurRepository.countByActifFalse());
        return statistiques;
    }

    private Map<String, Object> statistiquesProjets() {
        Map<String, Object> statistiques = new LinkedHashMap<>();
        statistiques.put("total", projetRepository.count());
        statistiques.put("importes", projetRepository.countByStatut(StatutProjet.IMPORTE));
        statistiques.put("enErreur", projetRepository.countByStatut(StatutProjet.EN_ERREUR));
        return statistiques;
    }

    private Map<String, Object> statistiquesExecutions() {
        Map<String, Object> statistiques = new LinkedHashMap<>();
        statistiques.put("total", executionRepository.count());
        statistiques.put("enAttente", executionRepository.countByStatut(StatutExecution.EN_ATTENTE));
        statistiques.put("enCours", executionRepository.countByStatut(StatutExecution.EN_COURS));
        statistiques.put("terminees", executionRepository.countByStatut(StatutExecution.TERMINEE));
        statistiques.put("echouees", executionRepository.countByStatut(StatutExecution.ECHOUEE));
        statistiques.put("annulees", executionRepository.countByStatut(StatutExecution.ANNULEE));
        return statistiques;
    }

    private Map<String, Object> statistiquesTests() {
        Map<String, Object> statistiques = new LinkedHashMap<>();
        statistiques.put("total", resultatTestRepository.count());
        statistiques.put("reussis", resultatTestRepository.countByStatut(StatutTest.REUSSI));
        statistiques.put("echoues", resultatTestRepository.countByStatut(StatutTest.ECHOUE));
        statistiques.put("ignores", resultatTestRepository.countByStatut(StatutTest.IGNORED));
        return statistiques;
    }

    private Map<String, Object> statistiquesRapports() {
        Map<String, Object> statistiques = new LinkedHashMap<>();
        statistiques.put("total", rapportRepository.count());
        statistiques.put("tests", rapportRepository.countByType(TypeRapport.TESTS));
        statistiques.put("analysesQualite", rapportRepository.countByType(TypeRapport.ANALYSE_QUALITE));
        return statistiques;
    }

    private Map<String, Object> activiteRecente() {
        Map<String, Object> activite = new LinkedHashMap<>();
        activite.put("derniersProjets", projetRepository
                .findTop5ByOrderByDateImportDesc()
                .stream()
                .map(this::mapperProjet)
                .toList());
        activite.put("dernieresExecutions", executionRepository
                .findTop5ByOrderByDateDebutDesc()
                .stream()
                .map(this::mapperExecution)
                .toList());
        activite.put("derniersRapports", rapportRepository
                .findTop5ByOrderByDateGenerationDesc()
                .stream()
                .map(this::mapperRapport)
                .toList());
        return activite;
    }

    private Map<String, Object> mapperProjet(Projet projet) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", projet.getId());
        item.put("nom", projet.getNom());
        item.put("typeSource", projet.getTypeSource());
        item.put("statut", projet.getStatut());
        item.put("dateImport", projet.getDateImport());
        item.put("utilisateur", mapperUtilisateur(projet.getUtilisateur()));
        return item;
    }

    private Map<String, Object> mapperExecution(Execution execution) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", execution.getId());
        item.put("type", execution instanceof ExecutionTest ? "TESTS" : "ANALYSE_QUALITE");
        item.put("statut", execution.getStatut());
        item.put("dateDebut", execution.getDateDebut());
        item.put("dateFin", execution.getDateFin());
        item.put("projet", mapperProjetResume(execution.getProjet()));
        item.put("utilisateur", mapperUtilisateur(execution.getProjet().getUtilisateur()));
        return item;
    }

    private Map<String, Object> mapperRapport(Rapport rapport) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", rapport.getId());
        item.put("nom", rapport.getNom());
        item.put("type", rapport.getType());
        item.put("dateGeneration", rapport.getDateGeneration());
        item.put("taille", rapport.getTaille());
        item.put("executionId", rapport.getExecution().getId());
        item.put("projet", mapperProjetResume(rapport.getExecution().getProjet()));
        item.put("utilisateur", mapperUtilisateur(
                rapport.getExecution().getProjet().getUtilisateur()
        ));
        return item;
    }

    private Map<String, Object> mapperProjetResume(Projet projet) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", projet.getId());
        item.put("nom", projet.getNom());
        return item;
    }

    private Map<String, Object> mapperUtilisateur(Utilisateur utilisateur) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", utilisateur.getId());
        item.put("nom", utilisateur.getNom());
        item.put("email", utilisateur.getEmail());
        return item;
    }
}
