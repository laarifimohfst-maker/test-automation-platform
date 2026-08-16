package com.testplatform.test_automation_platform.controller;

import com.testplatform.test_automation_platform.entity.Utilisateur;
import com.testplatform.test_automation_platform.repository.UtilisateurRepository;
import com.testplatform.test_automation_platform.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final UtilisateurRepository utilisateurRepository;

    public DashboardController(
            DashboardService dashboardService,
            UtilisateurRepository utilisateurRepository) {
        this.dashboardService = dashboardService;
        this.utilisateurRepository = utilisateurRepository;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> obtenirDashboard(@RequestParam Long utilisateurId) {

        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable."));

        return ResponseEntity.ok(dashboardService.obtenirDashboard(utilisateur));
    }
}