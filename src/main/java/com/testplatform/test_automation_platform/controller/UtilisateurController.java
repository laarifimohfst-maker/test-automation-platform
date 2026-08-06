package com.testplatform.test_automation_platform.controller;

import com.testplatform.test_automation_platform.entity.Utilisateur;
import com.testplatform.test_automation_platform.service.UtilisateurService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/utilisateurs")
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    public UtilisateurController(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    @PostMapping
    public ResponseEntity<Utilisateur> creerUtilisateur(
            @RequestBody Utilisateur utilisateur) {

        return ResponseEntity.ok(
                utilisateurService.creerUtilisateur(utilisateur)
        );
    }

    @GetMapping
    public ResponseEntity<List<Utilisateur>> obtenirTousLesUtilisateurs() {

        return ResponseEntity.ok(
                utilisateurService.obtenirTousLesUtilisateurs()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Utilisateur> obtenirUtilisateurParId(
            @PathVariable Long id) {

        return utilisateurService.obtenirUtilisateurParId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<Utilisateur> obtenirUtilisateurParEmail(
            @PathVariable String email) {

        return utilisateurService.obtenirUtilisateurParEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Utilisateur> modifierUtilisateur(
            @PathVariable Long id,
            @RequestBody Utilisateur utilisateur) {

        return ResponseEntity.ok(
                utilisateurService.modifierUtilisateur(id, utilisateur)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerUtilisateur(
            @PathVariable Long id) {

        utilisateurService.supprimerUtilisateur(id);

        return ResponseEntity.noContent().build();
    }
}