package com.testplatform.test_automation_platform.service;

import com.testplatform.test_automation_platform.entity.ConfigurationTest;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class MavenExecutionService {

    @Value("${sonar.host.url}")
    private String sonarHostUrl;

    @Value("${sonar.login}")
    private String sonarToken;

    public int executerTests(
            String cheminProjet,
            ConfigurationTest configuration)
            throws IOException, InterruptedException {

        int codeRetour = 0;

        /*
         * Tests unitaires
         */
        if (configuration.isTestsUnitaires()) {

            System.out.println("=== Exécution des tests unitaires ===");

            codeRetour = executerCommande(
                    cheminProjet,
                    "mvn",
                    "test"
            );

            if (codeRetour != 0) {
                return codeRetour;
            }
        }

        /*
         * Tests d'intégration
         *
         * *IT      → tests d'intégration
         * !*ApiIT  → exclut les tests API
         */
        if (configuration.isTestsIntegration()) {

            System.out.println("=== Exécution des tests d'intégration ===");

            codeRetour = executerCommande(
                    cheminProjet,
                    "mvn",
                    "verify",
                    "-Dsurefire.skip=true",
                    "-Dit.test=*IT,!*ApiIT"
            );

            if (codeRetour != 0) {
                return codeRetour;
            }
        }

        /*
         * Tests API
         *
         * *ApiIT → uniquement les tests API
         */
        if (configuration.isTestsApi()) {

            System.out.println("=== Exécution des tests API ===");

            codeRetour = executerCommande(
                    cheminProjet,
                    "mvn",
                    "verify",
                    "-Dsurefire.skip=true",
                    "-Dit.test=*ApiIT"
            );

            if (codeRetour != 0) {
                return codeRetour;
            }
        }

        return codeRetour;
    }

    public int executerCommande(
            String cheminProjet,
            String... commande)
            throws IOException, InterruptedException {

        Path dossierProjet = Paths.get(cheminProjet);

        List<String> commandeComplete = new ArrayList<>();
        commandeComplete.add("cmd.exe");
        commandeComplete.add("/c");
        commandeComplete.addAll(Arrays.asList(commande));

        ProcessBuilder processBuilder =
                new ProcessBuilder(commandeComplete);

        processBuilder.directory(dossierProjet.toFile());

        // Utiliser les certificats approuves par Windows pour Maven Central.
        Map<String, String> environnement = processBuilder.environment();
        String mavenOpts = environnement.getOrDefault("MAVEN_OPTS", "");
        String windowsTrustStore =
                "-Djavax.net.ssl.trustStoreType=Windows-ROOT";

        if (!mavenOpts.contains("javax.net.ssl.trustStore")) {
            environnement.put(
                    "MAVEN_OPTS",
                    (mavenOpts + " " + windowsTrustStore).trim()
            );
        }

        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        process.getInputStream()
                )
        );

        String ligne;

        while ((ligne = reader.readLine()) != null) {
            System.out.println(ligne);
        }

        int codeRetour = process.waitFor();

        System.out.println(
                "Code retour Maven : " + codeRetour
        );

        return codeRetour;
    }

    public int executerAnalyseQualite(
            String cheminProjet,
            String projectKey)
            throws IOException, InterruptedException {

        System.out.println("=== Analyse de qualité SonarQube ===");

        return executerCommande(
                cheminProjet,
                "mvn",
                "-DskipTests",
                "compile",
                "org.sonarsource.scanner.maven:sonar-maven-plugin:5.6.0.6792:sonar",
                "-Dsonar.projectKey=" + projectKey,
                "-Dsonar.host.url=" + sonarHostUrl,
                "-Dsonar.login=" + sonarToken,
                // Les rapports de tests generiques sont optionnels. Certains
                // projets declarent un chemin vers un rapport qui n'existe pas,
                // ce qui fait echouer toute l'analyse SonarQube. La plateforme
                // analyse donc le code sans imposer ce rapport externe.
                "-Dsonar.testExecutionReportPaths="
        );
    }
}
