package com.testplatform.test_automation_platform.controller;

import com.testplatform.test_automation_platform.service.FileStorageService;
import com.testplatform.test_automation_platform.entity.Projet;
import com.testplatform.test_automation_platform.entity.Utilisateur;
import com.testplatform.test_automation_platform.service.ProjetService;
import com.testplatform.test_automation_platform.service.UtilisateurService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.io.IOException;

@RestController
@RequestMapping("/api/projets")
public class ProjetController {

    private final ProjetService projetService;
    private final UtilisateurService utilisateurService;
    private final FileStorageService fileStorageService;

    public ProjetController(
            ProjetService projetService,
            UtilisateurService utilisateurService,
            FileStorageService fileStorageService) {

        this.projetService = projetService;
        this.utilisateurService = utilisateurService;
        this.fileStorageService = fileStorageService;
    }

    @PostMapping
    @PreAuthorize("@authorizationService.peutCreerProjet(#p0, authentication)")
    public ResponseEntity<Projet> creerProjet(
            @RequestBody Projet projet) {

        return ResponseEntity.ok(
                projetService.creerProjet(projet)
        );
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Projet>> obtenirTousLesProjets() {

        return ResponseEntity.ok(
                projetService.obtenirTousLesProjets()
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("@authorizationService.peutAccederProjet(#p0, authentication)")
    public ResponseEntity<Projet> obtenirProjetParId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                projetService.obtenirProjetParId(id)
        );
    }

    @GetMapping("/utilisateur/{utilisateurId}")
    @PreAuthorize("@authorizationService.peutAccederUtilisateur(#p0, authentication)")
    public ResponseEntity<List<Projet>> obtenirProjetsUtilisateur(
            @PathVariable Long utilisateurId) {

        Utilisateur utilisateur =
                utilisateurService.obtenirUtilisateurParId(utilisateurId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Utilisateur introuvable."
                                ));

        return ResponseEntity.ok(
                projetService.obtenirProjetsUtilisateur(utilisateur)
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("@authorizationService.peutAccederProjet(#p0, authentication)")
    public ResponseEntity<Projet> modifierProjet(
            @PathVariable Long id,
            @RequestBody Projet projet) {

        return ResponseEntity.ok(
                projetService.modifierProjet(id, projet)
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> supprimerProjet(
            @PathVariable Long id) {

        projetService.supprimerProjet(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/import")
    @PreAuthorize("@authorizationService.peutAccederUtilisateur(#p1, authentication)")
    public ResponseEntity<?> importerProjet(
            @RequestParam("fichier") MultipartFile fichier,
            @RequestParam Long utilisateurId) {

        try {

            Utilisateur utilisateur = utilisateurService
                    .obtenirUtilisateurParId(utilisateurId)
                    .orElseThrow(() ->
                            new IllegalArgumentException("Utilisateur introuvable."));

            Projet projet = projetService.importerProjetZip(
                    fichier,
                    utilisateur
            );

            return ResponseEntity.ok(projet);

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (IOException e) {

            return ResponseEntity.internalServerError()
                    .body("Erreur lors de l'importation du projet.");
        }
    }

    @PostMapping("/import/github")
    @PreAuthorize("@authorizationService.peutAccederUtilisateur(#p1, authentication)")
    public ResponseEntity<?> importerDepuisGithub(
            @RequestParam String url,
            @RequestParam Long utilisateurId) {

        try {

            Utilisateur utilisateur = utilisateurService
                    .obtenirUtilisateurParId(utilisateurId)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Utilisateur introuvable."));

            Projet projet = projetService.importerProjetGithub(
                    url,
                    utilisateur);

            return ResponseEntity.ok(projet);

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest()
                    .body(e.getMessage());

        } catch (IOException | InterruptedException e) {

            return ResponseEntity.internalServerError()
                    .body("Erreur lors du clonage du dépôt GitHub.");
        }
    }

}
