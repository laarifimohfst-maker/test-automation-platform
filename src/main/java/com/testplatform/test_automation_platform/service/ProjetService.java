package com.testplatform.test_automation_platform.service;

import com.testplatform.test_automation_platform.entity.Projet;
import com.testplatform.test_automation_platform.entity.Utilisateur;
import com.testplatform.test_automation_platform.enums.StatutProjet;
import com.testplatform.test_automation_platform.repository.ProjetRepository;
import com.testplatform.test_automation_platform.enums.TypeSource;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import java.nio.file.Paths;
import java.io.IOException;

import java.util.List;

@Service
public class ProjetService {

    private final ProjetRepository projetRepository;
    private final FileStorageService fileStorageService;

    public ProjetService(
            ProjetRepository projetRepository,
            FileStorageService fileStorageService) {

        this.projetRepository = projetRepository;
        this.fileStorageService = fileStorageService;
    }

    public Projet creerProjet(Projet projet) {

        if (projet.getStatut() == null) {
            projet.setStatut(StatutProjet.IMPORTE);
        }

        return projetRepository.save(projet);
    }

    public Projet importerProjetZip(
            MultipartFile fichier,
            Utilisateur utilisateur)
            throws IOException {

        String cheminProjet =
                fileStorageService.sauvegarderProjet(fichier);

        Projet projet = new Projet();

        projet.setNom(fichier.getOriginalFilename().replace(".zip", ""));
        projet.setCheminLocal(cheminProjet);
        projet.setUtilisateur(utilisateur);
        projet.setDateImport(LocalDateTime.now());
        projet.setStatut(StatutProjet.IMPORTE);
        projet.setTypeSource(TypeSource.ARCHIVE_ZIP);

        return projetRepository.save(projet);
    }

    public Projet importerProjetGithub(
            String urlGithub,
            Utilisateur utilisateur)
            throws IOException, InterruptedException {

        String cheminProjet =
                fileStorageService.clonerProjetGithub(urlGithub);

        Projet projet = new Projet();

        projet.setNom(Paths.get(cheminProjet).getFileName().toString());
        projet.setCheminLocal(cheminProjet);
        projet.setUtilisateur(utilisateur);
        projet.setDateImport(LocalDateTime.now());
        projet.setStatut(StatutProjet.IMPORTE);
        projet.setTypeSource(TypeSource.GITHUB);

        return projetRepository.save(projet);
    }

    public List<Projet> obtenirTousLesProjets() {
        return projetRepository.findAll();
    }

    public Projet obtenirProjetParId(Long id) {
        return projetRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Projet introuvable."));
    }

    public List<Projet> obtenirProjetsUtilisateur(Utilisateur utilisateur) {
        return projetRepository.findByUtilisateur(utilisateur);
    }

    public Projet modifierProjet(Long id, Projet projetModifie) {

        Projet projet = obtenirProjetParId(id);

        projet.setNom(projetModifie.getNom());
        projet.setDescription(projetModifie.getDescription());
        projet.setTypeSource(projetModifie.getTypeSource());
        projet.setStatut(projetModifie.getStatut());

        return projetRepository.save(projet);
    }

    public void supprimerProjet(Long id) {

        if (!projetRepository.existsById(id)) {
            throw new IllegalArgumentException("Projet introuvable.");
        }

        projetRepository.deleteById(id);
    }
}