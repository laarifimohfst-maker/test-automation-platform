package com.testplatform.test_automation_platform.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.Comparator;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${upload.directory}")
    private String uploadDirectory;

    public String sauvegarderProjet(MultipartFile fichier)
            throws IOException {

        Path dossierUpload = Paths.get(uploadDirectory);

        if (!Files.exists(dossierUpload)) {
            Files.createDirectories(dossierUpload);
        }

        Path cheminFichier =
                dossierUpload.resolve(fichier.getOriginalFilename());

        Files.copy(
                fichier.getInputStream(),
                cheminFichier,
                StandardCopyOption.REPLACE_EXISTING
        );

        String cheminProjet = extraireZip(cheminFichier.toString());

        try {

            return verifierPom(cheminProjet);

        } catch (IllegalArgumentException e) {

            supprimerProjet(cheminProjet);

            Files.deleteIfExists(cheminFichier);

            throw e;
        }
    }

    public String extraireZip(String cheminZip) throws IOException {

        Path dossierExtraction = Paths.get(
                cheminZip.replace(".zip", "")
        );

        if (!Files.exists(dossierExtraction)) {
            Files.createDirectories(dossierExtraction);
        }

        try (ZipInputStream zipInputStream =
                     new ZipInputStream(new FileInputStream(cheminZip))) {

            ZipEntry entry;

            while ((entry = zipInputStream.getNextEntry()) != null) {

                Path cheminFichier =
                        dossierExtraction.resolve(entry.getName());

                if (entry.isDirectory()) {

                    Files.createDirectories(cheminFichier);

                } else {

                    Files.createDirectories(cheminFichier.getParent());

                    try (FileOutputStream outputStream =
                                 new FileOutputStream(
                                         cheminFichier.toFile())) {

                        zipInputStream.transferTo(outputStream);
                    }
                }

                zipInputStream.closeEntry();
            }
        }

        return dossierExtraction.toString();
    }

    public Path trouverPom(Path dossierProjet) throws IOException {

        try (var fichiers = Files.walk(dossierProjet)) {

            return fichiers
                    .filter(path -> path.getFileName().toString().equals("pom.xml"))
                    .findFirst()
                    .orElse(null);
        }
    }

    public String verifierPom(String cheminProjet) throws IOException {

        Path pom = trouverPom(Paths.get(cheminProjet));

        if (pom == null) {
            throw new IllegalArgumentException(
                    "Le projet importé n'est pas un projet Spring Boot.");
        }

        if (!estProjetSpringBoot(pom)) {
            throw new IllegalArgumentException(
                    "Le projet importé n'est pas un projet Spring Boot.");
        }

        return pom.getParent().toString();
    }

    public boolean estProjetSpringBoot(Path pom) {

        try {

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document document = builder.parse(pom.toFile());

            NodeList groupIds = document.getElementsByTagName("groupId");

            for (int i = 0; i < groupIds.getLength(); i++) {

                String valeur = groupIds.item(i).getTextContent().trim();

                if ("org.springframework.boot".equals(valeur)) {
                    return true;
                }
            }

            return false;

        } catch (Exception e) {
            return false;
        }
    }

    public void supprimerProjet(String chemin) {

        Path path = Paths.get(chemin);

        if (!Files.exists(path)) {
            return;
        }

        try (var stream = Files.walk(path)) {

            stream.sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                            System.out.println("Supprimé : " + p);
                        } catch (IOException e) {
                            System.out.println("Impossible : " + p);
                            e.printStackTrace();
                        }
                    });

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Le dossier existe encore ? " + Files.exists(path));
    }

    public String clonerProjetGithub(String urlGithub)
            throws IOException, InterruptedException {

        Path uploads = Paths.get("uploads");

        if (!Files.exists(uploads)) {
            Files.createDirectories(uploads);
        }

        String nomDossier = UUID.randomUUID().toString();
        Path dossierProjet = uploads.resolve(nomDossier);

        try {

            ProcessBuilder processBuilder = new ProcessBuilder(
                    "git",
                    "clone",
                    urlGithub,
                    dossierProjet.toString()
            );

            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );

            String ligne;

            while ((ligne = reader.readLine()) != null) {
                System.out.println(ligne);
            }

            int codeRetour = process.waitFor();

            System.out.println("Code retour Git : " + codeRetour);

            if (codeRetour != 0) {

                supprimerProjet(dossierProjet.toString());

                throw new IllegalArgumentException(
                        "Impossible de cloner le dépôt GitHub.");
            }

            System.out.println("Clonage terminé.");

            String cheminProjet = verifierPom(dossierProjet.toString());

            System.out.println("Projet Maven valide.");

            return cheminProjet;

        } catch (Exception e) {

            System.out.println("Erreur : " + e.getMessage());

            supprimerProjet(dossierProjet.toString());

            if (e instanceof IllegalArgumentException) {
                throw (IllegalArgumentException) e;
            }

            throw e;
        }
    }

}