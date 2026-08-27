package com.testplatform.test_automation_platform.controller;

import com.testplatform.test_automation_platform.entity.Utilisateur;
import com.testplatform.test_automation_platform.security.AuthorizationService;
import com.testplatform.test_automation_platform.service.UtilisateurService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/utilisateurs")
public class UtilisateurController {

    private final UtilisateurService utilisateurService;
    private final AuthorizationService authorizationService;

    public UtilisateurController(
            UtilisateurService utilisateurService,
            AuthorizationService authorizationService) {
        this.utilisateurService = utilisateurService;
        this.authorizationService = authorizationService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Utilisateur> creerUtilisateur(
            @RequestBody Utilisateur utilisateur) {

        return ResponseEntity.ok(
                utilisateurService.creerUtilisateur(utilisateur)
        );
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Utilisateur>> obtenirTousLesUtilisateurs() {

        return ResponseEntity.ok(
                utilisateurService.obtenirTousLesUtilisateurs()
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("@authorizationService.peutAccederUtilisateur(#p0, authentication)")
    public ResponseEntity<Utilisateur> obtenirUtilisateurParId(
            @PathVariable Long id) {

        return utilisateurService.obtenirUtilisateurParId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/email/{email}")
    @PreAuthorize("@authorizationService.peutAccederEmail(#p0, authentication)")
    public ResponseEntity<Utilisateur> obtenirUtilisateurParEmail(
            @PathVariable String email) {

        return utilisateurService.obtenirUtilisateurParEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("@authorizationService.peutAccederUtilisateur(#p0, authentication)")
    public ResponseEntity<Utilisateur> modifierUtilisateur(
            @PathVariable Long id,
            @RequestBody Utilisateur utilisateur,
            Authentication authentication) {

        return ResponseEntity.ok(
                utilisateurService.modifierUtilisateur(
                        id,
                        utilisateur,
                        authorizationService.estAdmin(authentication),
                        authentication.getName()
                )
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> supprimerUtilisateur(
            @PathVariable Long id,
            Authentication authentication) {

        utilisateurService.supprimerUtilisateur(id, authentication.getName());

        return ResponseEntity.noContent().build();
    }
}
