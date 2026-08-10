package com.testplatform.test_automation_platform.service;

import com.testplatform.test_automation_platform.entity.ExecutionTest;
import com.testplatform.test_automation_platform.entity.ResultatTest;
import com.testplatform.test_automation_platform.repository.ResultatTestRepository;
import com.testplatform.test_automation_platform.enums.StatutTest;
import com.testplatform.test_automation_platform.enums.TypeTest;
import org.springframework.stereotype.Service;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.List;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

@Service
public class ResultatTestService {

    private final ResultatTestRepository resultatTestRepository;

    public ResultatTestService(
            ResultatTestRepository resultatTestRepository) {
        this.resultatTestRepository = resultatTestRepository;
    }

    public ResultatTest enregistrerResultat(ResultatTest resultatTest) {
        return resultatTestRepository.save(resultatTest);
    }

    public ResultatTest obtenirResultatParId(Long id) {
        return resultatTestRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Résultat de test introuvable."));
    }

    public List<ResultatTest> obtenirResultatsParExecution(
            ExecutionTest executionTest) {

        return resultatTestRepository.findByExecutionTest(executionTest);
    }

    public ResultatTest modifierResultat(
            Long id,
            ResultatTest resultatModifie) {

        ResultatTest resultat = obtenirResultatParId(id);

        resultat.setType(resultatModifie.getType());
        resultat.setNomTest(resultatModifie.getNomTest());
        resultat.setStatut(resultatModifie.getStatut());
        resultat.setDuree(resultatModifie.getDuree());
        resultat.setMessage(resultatModifie.getMessage());

        return resultatTestRepository.save(resultat);
    }

    public void supprimerResultat(Long id) {

        if (!resultatTestRepository.existsById(id)) {
            throw new IllegalArgumentException(
                    "Résultat de test introuvable.");
        }

        resultatTestRepository.deleteById(id);
    }

    public List<ResultatTest> lireEtEnregistrerResultats(
            String cheminProjet,
            ExecutionTest executionTest) {

        List<ResultatTest> resultats = new ArrayList<>();

        resultats.addAll(
                lireDossierRapports(
                        Paths.get(cheminProjet, "target", "surefire-reports"),
                        executionTest
                )
        );

        resultats.addAll(
                lireDossierRapports(
                        Paths.get(cheminProjet, "target", "failsafe-reports"),
                        executionTest
                )
        );

        return resultats;
    }

    private List<ResultatTest> lireDossierRapports(
            Path dossierRapports,
            ExecutionTest executionTest) {

        List<ResultatTest> resultats = new ArrayList<>();

        File dossier = dossierRapports.toFile();

        if (!dossier.exists() || !dossier.isDirectory()) {
            return resultats;
        }

        File[] fichiersXml = dossier.listFiles(
                (dir, nom) -> nom.endsWith(".xml")
        );

        if (fichiersXml == null) {
            return resultats;
        }

        for (File fichier : fichiersXml) {
            resultats.addAll(lireFichierXml(fichier, executionTest));
        }

        return resultats;
    }

    private List<ResultatTest> lireFichierXml(
            File fichier,
            ExecutionTest executionTest) {

        List<ResultatTest> resultats = new ArrayList<>();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(fichier);

            NodeList testcases = document.getElementsByTagName("testcase");

            for (int i = 0; i < testcases.getLength(); i++) {

                Element testcase = (Element) testcases.item(i);

                String nomTest = testcase.getAttribute("name");
                String classname = testcase.getAttribute("classname");
                double tempsEnSecondes = Double.parseDouble(
                        testcase.getAttribute("time")
                );
                long dureeEnMillisecondes = Math.round(tempsEnSecondes * 1000);

                NodeList failures = testcase.getElementsByTagName("failure");
                NodeList errors = testcase.getElementsByTagName("error");

                StatutTest statut;
                String message = null;

                if (failures.getLength() > 0) {
                    statut = StatutTest.ECHOUE;
                    message = ((Element) failures.item(0)).getAttribute("message");
                } else if (errors.getLength() > 0) {
                    statut = StatutTest.ECHOUE;
                    message = ((Element) errors.item(0)).getAttribute("message");
                } else {
                    statut = StatutTest.REUSSI;
                }

                TypeTest type = determinerType(classname);

                ResultatTest resultat = ResultatTest.builder()
                        .executionTest(executionTest)
                        .nomTest(nomTest)
                        .type(type)
                        .statut(statut)
                        .duree(dureeEnMillisecondes)
                        .message(message)
                        .build();

                resultats.add(enregistrerResultat(resultat));
            }

        } catch (Exception e) {
            System.out.println(
                    "Erreur lors de la lecture du fichier "
                            + fichier.getName() + " : " + e.getMessage()
            );
        }

        return resultats;
    }

    private TypeTest determinerType(String classname) {

        if (classname.endsWith("ApiIT")) {
            return TypeTest.API;
        } else if (classname.endsWith("IT")) {
            return TypeTest.INTEGRATION;
        } else {
            return TypeTest.UNITAIRE;
        }
    }
}