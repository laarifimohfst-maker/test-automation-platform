package com.testplatform.test_automation_platform.controller;

import com.testplatform.test_automation_platform.entity.ConfigurationTest;
import com.testplatform.test_automation_platform.entity.Projet;
import com.testplatform.test_automation_platform.service.ConfigurationTestService;
import com.testplatform.test_automation_platform.service.ProjetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/configurations-tests")
public class ConfigurationTestController {

    private final ConfigurationTestService configurationTestService;
    private final ProjetService projetService;

    public ConfigurationTestController(
            ConfigurationTestService configurationTestService,
            ProjetService projetService) {

        this.configurationTestService = configurationTestService;
        this.projetService = projetService;
    }

    @PostMapping
    public ResponseEntity<ConfigurationTest> creerConfiguration(
            @RequestBody ConfigurationTest configurationTest) {

        return ResponseEntity.ok(
                configurationTestService.creerConfiguration(configurationTest)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConfigurationTest> obtenirConfigurationParId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                configurationTestService.obtenirConfigurationParId(id)
        );
    }

    @GetMapping("/projet/{projetId}")
    public ResponseEntity<List<ConfigurationTest>> obtenirConfigurationsParProjet(
            @PathVariable Long projetId) {

        Projet projet = projetService.obtenirProjetParId(projetId);

        return ResponseEntity.ok(
                configurationTestService.obtenirConfigurationsParProjet(projet)
        );
    }

    @PutMapping("/{id}")
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerConfiguration(
            @PathVariable Long id) {

        configurationTestService.supprimerConfiguration(id);

        return ResponseEntity.noContent().build();
    }
}