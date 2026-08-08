package com.testplatform.test_automation_platform.service;

import com.testplatform.test_automation_platform.entity.ConfigurationTest;
import com.testplatform.test_automation_platform.entity.Projet;
import com.testplatform.test_automation_platform.repository.ConfigurationTestRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.time.LocalDateTime;

@Service
public class ConfigurationTestService {

    private final ConfigurationTestRepository configurationTestRepository;
    private final ProjetService projetService;

    public ConfigurationTestService(
            ConfigurationTestRepository configurationTestRepository,
            ProjetService projetService) {
        this.configurationTestRepository = configurationTestRepository;
        this.projetService = projetService;
    }

    public ConfigurationTest creerConfiguration(ConfigurationTest configurationTest) {
        configurationTest.setDateConfiguration(LocalDateTime.now());
        return configurationTestRepository.save(configurationTest);
    }

    public ConfigurationTest obtenirConfigurationParId(Long id) {
        return configurationTestRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Configuration de test introuvable."));
    }

    public List<ConfigurationTest> obtenirConfigurationsParProjet(Projet projet) {
        return configurationTestRepository.findByProjet(projet);
    }

    public ConfigurationTest modifierConfiguration(
            Long id,
            ConfigurationTest configurationModifiee) {

        ConfigurationTest configuration =
                obtenirConfigurationParId(id);

        configuration.setTestsUnitaires(
                configurationModifiee.isTestsUnitaires());

        configuration.setTestsIntegration(
                configurationModifiee.isTestsIntegration());

        configuration.setTestsApi(
                configurationModifiee.isTestsApi());

        return configurationTestRepository.save(configuration);
    }

    public void supprimerConfiguration(Long id) {

        if (!configurationTestRepository.existsById(id)) {
            throw new IllegalArgumentException(
                    "Configuration de test introuvable.");
        }

        configurationTestRepository.deleteById(id);
    }

    public ConfigurationTest configurerTests(Long projetId, ConfigurationTest configurationDemandee) {

        boolean unitaires = configurationDemandee.isTestsUnitaires();
        boolean integration = configurationDemandee.isTestsIntegration();
        boolean api = configurationDemandee.isTestsApi();

        if (!unitaires && !integration && !api) {
            throw new IllegalArgumentException(
                    "Vous devez sélectionner au moins un type de test.");
        }

        Projet projet = projetService.obtenirProjetParId(projetId);

        return configurationTestRepository
                .findByProjetAndTestsUnitairesAndTestsIntegrationAndTestsApi(
                        projet, unitaires, integration, api)
                .orElseGet(() -> {
                    ConfigurationTest nouvelle = ConfigurationTest.builder()
                            .projet(projet)
                            .testsUnitaires(unitaires)
                            .testsIntegration(integration)
                            .testsApi(api)
                            .dateConfiguration(LocalDateTime.now())
                            .build();
                    return configurationTestRepository.save(nouvelle);
                });
    }
}