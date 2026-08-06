package com.testplatform.test_automation_platform.controller;

import com.testplatform.test_automation_platform.entity.Projet;
import com.testplatform.test_automation_platform.entity.Utilisateur;
import com.testplatform.test_automation_platform.service.ProjetService;
import com.testplatform.test_automation_platform.service.UtilisateurService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projets")
public class ProjetController {

    private final ProjetService projetService;
    private final UtilisateurService utilisateurService;

    public ProjetController(
            ProjetService projetService,
            UtilisateurService utilisateurService) {

        this.projetService = projetService;
        this.utilisateurService = utilisateurService;
    }

    @PostMapping
    public ResponseEntity<Projet> creerProjet(
            @RequestBody Projet projet) {

        return ResponseEntity.ok(
                projetService.creerProjet(projet)
        );
    }

    @GetMapping
    public ResponseEntity<List<Projet>> obtenirTousLesProjets() {

        return ResponseEntity.ok(
                projetService.obtenirTousLesProjets()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Projet> obtenirProjetParId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                projetService.obtenirProjetParId(id)
        );
    }

    @GetMapping("/utilisateur/{utilisateurId}")
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
    public ResponseEntity<Projet> modifierProjet(
            @PathVariable Long id,
            @RequestBody Projet projet) {

        return ResponseEntity.ok(
                projetService.modifierProjet(id, projet)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerProjet(
            @PathVariable Long id) {

        projetService.supprimerProjet(id);

        return ResponseEntity.noContent().build();
    }
}