package com.testplatform.test_automation_platform.controller;

import com.testplatform.test_automation_platform.entity.Utilisateur;
import com.testplatform.test_automation_platform.enums.Role;
import com.testplatform.test_automation_platform.repository.UtilisateurRepository;
import com.testplatform.test_automation_platform.security.JwtService;
import com.testplatform.test_automation_platform.service.UtilisateurService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UtilisateurRepository utilisateurRepository;
    private final UtilisateurService utilisateurService;

    public AuthController(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            UtilisateurRepository utilisateurRepository,
            UtilisateurService utilisateurService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.utilisateurRepository = utilisateurRepository;
        this.utilisateurService = utilisateurService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> inscrire(
            @RequestBody Map<String, String> informations) {

        if (informations == null) {
            return creerReponseErreur(
                    HttpStatus.BAD_REQUEST,
                    "Le nom, l'email et le mot de passe sont obligatoires."
            );
        }

        Utilisateur utilisateur = Utilisateur.builder()
                .nom(informations.get("nom"))
                .email(informations.get("email"))
                .motDePasse(informations.get("motDePasse"))
                .role(Role.DEVELOPPEUR)
                .build();

        try {
            Utilisateur utilisateurCree =
                    utilisateurService.creerUtilisateur(utilisateur);
            return ResponseEntity.status(HttpStatus.CREATED).body(utilisateurCree);
        } catch (IllegalArgumentException exception) {
            return creerReponseErreur(
                    HttpStatus.BAD_REQUEST,
                    exception.getMessage()
            );
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> connecter(
            @RequestBody Map<String, String> identifiants) {

        if (identifiants == null) {
            return creerReponseErreur(
                    HttpStatus.BAD_REQUEST,
                    "L'email et le mot de passe sont obligatoires."
            );
        }

        String email = identifiants.get("email");
        String motDePasse = identifiants.get("motDePasse");

        if (email == null || email.isBlank()
                || motDePasse == null || motDePasse.isBlank()) {
            return creerReponseErreur(
                    HttpStatus.BAD_REQUEST,
                    "L'email et le mot de passe sont obligatoires."
            );
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(
                            email.trim(),
                            motDePasse
                    )
            );

            Map<String, Object> reponse = new LinkedHashMap<>();
            reponse.put("accessToken", jwtService.genererToken(authentication));
            reponse.put("tokenType", "Bearer");
            reponse.put(
                    "expiresIn",
                    jwtService.obtenirDureeExpirationEnSecondes()
            );

            return ResponseEntity.ok(reponse);
        } catch (AuthenticationException exception) {
            return creerReponseErreur(
                    HttpStatus.UNAUTHORIZED,
                    "Email ou mot de passe incorrect."
            );
        }
    }

    @GetMapping("/me")
    public ResponseEntity<Utilisateur> obtenirUtilisateurCourant(
            Authentication authentication) {
        return utilisateurRepository
                .findByEmailIgnoreCase(authentication.getName())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private ResponseEntity<Map<String, Object>> creerReponseErreur(
            HttpStatus statut,
            String message) {
        Map<String, Object> reponse = new LinkedHashMap<>();
        reponse.put("message", message);
        return ResponseEntity.status(statut).body(reponse);
    }
}
