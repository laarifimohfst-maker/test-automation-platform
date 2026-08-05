package com.testplatform.test_automation_platform.service;

import com.testplatform.test_automation_platform.entity.ConfigurationTest;
import com.testplatform.test_automation_platform.entity.Projet;
import com.testplatform.test_automation_platform.repository.ConfigurationTestRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConfigurationTestService {

    private final ConfigurationTestRepository configurationTestRepository;

    public ConfigurationTestService(
            ConfigurationTestRepository configurationTestRepository) {
        this.configurationTestRepository = configurationTestRepository;
    }

    public ConfigurationTest creerConfiguration(ConfigurationTest configurationTest) {
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
}