package com.testplatform.test_automation_platform.service;

import com.testplatform.test_automation_platform.entity.ExecutionTest;
import com.testplatform.test_automation_platform.entity.ResultatTest;
import com.testplatform.test_automation_platform.enums.StatutTest;
import com.testplatform.test_automation_platform.enums.TypeTest;
import com.testplatform.test_automation_platform.repository.ResultatTestRepository;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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
                        Paths.get(
                                cheminProjet,
                                "target",
                                "surefire-reports"
                        ),
                        executionTest
                )
        );

        resultats.addAll(
                lireDossierRapports(
                        Paths.get(
                                cheminProjet,
                                "target",
                                "failsafe-reports"
                        ),
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
                (dir, nom) -> nom.startsWith("TEST-")
                        && nom.endsWith(".xml")
        );

        if (fichiersXml == null) {
            return resultats;
        }

        for (File fichier : fichiersXml) {
            resultats.addAll(
                    lireFichierXml(fichier, executionTest)
            );
        }

        return resultats;
    }

    private List<ResultatTest> lireFichierXml(
            File fichier,
            ExecutionTest executionTest) {

        List<ResultatTest> resultats = new ArrayList<>();

        try {
            DocumentBuilderFactory factory =
                    DocumentBuilderFactory.newInstance();
            factory.setFeature(
                    "http://apache.org/xml/features/disallow-doctype-decl",
                    true
            );

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(fichier);
            NodeList testcases =
                    document.getElementsByTagName("testcase");

            for (int i = 0; i < testcases.getLength(); i++) {
                Element testcase = (Element) testcases.item(i);

                String nomTest = testcase.getAttribute("name");
                String classname = testcase.getAttribute("classname");
                long duree = convertirDureeEnMillisecondes(
                        testcase.getAttribute("time")
                );

                NodeList failures =
                        testcase.getElementsByTagName("failure");
                NodeList errors =
                        testcase.getElementsByTagName("error");
                NodeList skipped =
                        testcase.getElementsByTagName("skipped");

                StatutTest statut;
                String message = null;

                if (failures.getLength() > 0) {
                    statut = StatutTest.ECHOUE;
                    message = extraireMessage(failures);
                } else if (errors.getLength() > 0) {
                    statut = StatutTest.ECHOUE;
                    message = extraireMessage(errors);
                } else if (skipped.getLength() > 0) {
                    statut = StatutTest.IGNORED;
                    message = extraireMessage(skipped);
                } else {
                    statut = StatutTest.REUSSI;
                }

                ResultatTest resultat = ResultatTest.builder()
                        .executionTest(executionTest)
                        .nomTest(nomTest)
                        .type(determinerType(classname))
                        .statut(statut)
                        .duree(duree)
                        .message(message)
                        .build();

                resultats.add(enregistrerResultat(resultat));
            }
        } catch (Exception e) {
            System.out.println(
                    "Erreur lors de la lecture du fichier "
                            + fichier.getName()
                            + " : "
                            + e.getMessage()
            );
        }

        return resultats;
    }

    private long convertirDureeEnMillisecondes(String valeur) {
        if (valeur == null || valeur.isBlank()) {
            return 0L;
        }

        return Math.round(Double.parseDouble(valeur) * 1000);
    }

    private String extraireMessage(NodeList noeuds) {
        Element element = (Element) noeuds.item(0);
        String message = element.getAttribute("message");

        if (message == null || message.isBlank()) {
            message = element.getTextContent();
        }

        return message;
    }

    private TypeTest determinerType(String classname) {
        if (classname.endsWith("ApiIT")) {
            return TypeTest.API;
        }

        if (classname.endsWith("IT")) {
            return TypeTest.INTEGRATION;
        }

        return TypeTest.UNITAIRE;
    }
}
