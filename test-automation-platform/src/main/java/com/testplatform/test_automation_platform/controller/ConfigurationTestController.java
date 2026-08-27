package com.testplatform.test_automation_platform.controller;

import com.testplatform.test_automation_platform.entity.ConfigurationTest;
import com.testplatform.test_automation_platform.entity.Projet;
import com.testplatform.test_automation_platform.service.ConfigurationTestService;
import com.testplatform.test_automation_platform.service.ProjetService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ConfigurationTestController {

    private final ConfigurationTestService configurationTestService;
    private final ProjetService projetService;

    public ConfigurationTestController(
            ConfigurationTestService configurationTestService,
            ProjetService projetService) {

        this.configurationTestService = configurationTestService;
        this.projetService = projetService;
    }

    @PostMapping("/api/configurations-tests")
    @PreAuthorize("@authorizationService.peutCreerConfiguration(#p0, authentication)")
    public ResponseEntity<ConfigurationTest> creerConfiguration(
            @RequestBody ConfigurationTest configurationTest) {

        return ResponseEntity.ok(
                configurationTestService.creerConfiguration(configurationTest)
        );
    }

    @GetMapping("/api/configurations-tests/{id}")
    @PreAuthorize("@authorizationService.peutAccederConfiguration(#p0, authentication)")
    public ResponseEntity<ConfigurationTest> obtenirConfigurationParId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                configurationTestService.obtenirConfigurationParId(id)
        );
    }

    @GetMapping("/api/configurations-tests/projet/{projetId}")
    @PreAuthorize("@authorizationService.peutAccederProjet(#p0, authentication)")
    public ResponseEntity<List<ConfigurationTest>> obtenirConfigurationsParProjet(
            @PathVariable Long projetId) {

        Projet projet = projetService.obtenirProjetParId(projetId);

        return ResponseEntity.ok(
                configurationTestService.obtenirConfigurationsParProjet(projet)
        );
    }

    @PutMapping("/api/configurations-tests/{id}")
    @PreAuthorize("@authorizationService.peutAccederConfiguration(#p0, authentication)")
    public ResponseEntity<ConfigurationTest> modifierConfiguration(
            @PathVariable Long id,
            @RequestBody ConfigurationTest configurationTest) {

        return ResponseEntity.ok(
                configurationTestService.modifierConfiguration(
                        id,
                        configurationTest
                )
        );
    }

    @DeleteMapping("/api/configurations-tests/{id}")
    @PreAuthorize("@authorizationService.peutAccederConfiguration(#p0, authentication)")
    public ResponseEntity<Void> supprimerConfiguration(
            @PathVariable Long id) {

        configurationTestService.supprimerConfiguration(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/projets/{projetId}/configuration-test")
    @PreAuthorize("@authorizationService.peutAccederProjet(#p0, authentication)")
    public ResponseEntity<ConfigurationTest> configurer(
            @PathVariable Long projetId,
            @RequestBody ConfigurationTest configurationTest) {

        return ResponseEntity.ok(
                configurationTestService.configurerTests(projetId, configurationTest)
        );
    }
}
