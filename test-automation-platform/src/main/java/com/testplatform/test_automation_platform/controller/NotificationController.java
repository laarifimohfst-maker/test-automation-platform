package com.testplatform.test_automation_platform.controller;

import com.testplatform.test_automation_platform.entity.Notification;
import com.testplatform.test_automation_platform.entity.Utilisateur;
import com.testplatform.test_automation_platform.service.NotificationService;
import com.testplatform.test_automation_platform.service.UtilisateurService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UtilisateurService utilisateurService;

    public NotificationController(
            NotificationService notificationService,
            UtilisateurService utilisateurService) {

        this.notificationService = notificationService;
        this.utilisateurService = utilisateurService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Notification> creerNotification(
            @RequestBody Notification notification) {

        return ResponseEntity.ok(
                notificationService.creerNotification(notification)
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("@authorizationService.peutAccederNotification(#p0, authentication)")
    public ResponseEntity<Notification> obtenirNotificationParId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                notificationService.obtenirNotificationParId(id)
        );
    }

    @GetMapping("/utilisateur/{utilisateurId}")
    @PreAuthorize("@authorizationService.peutAccederUtilisateur(#p0, authentication)")
    public ResponseEntity<List<Notification>> obtenirNotificationsUtilisateur(
            @PathVariable Long utilisateurId) {

        Utilisateur utilisateur =
                utilisateurService.obtenirUtilisateurParId(utilisateurId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Utilisateur introuvable."
                                ));

        return ResponseEntity.ok(
                notificationService.obtenirNotificationsUtilisateur(
                        utilisateur
                )
        );
    }

    @GetMapping("/utilisateur/{utilisateurId}/non-lues")
    @PreAuthorize("@authorizationService.peutAccederUtilisateur(#p0, authentication)")
    public ResponseEntity<List<Notification>> obtenirNotificationsNonLues(
            @PathVariable Long utilisateurId) {

        Utilisateur utilisateur =
                utilisateurService.obtenirUtilisateurParId(utilisateurId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Utilisateur introuvable."
                                ));

        return ResponseEntity.ok(
                notificationService.obtenirNotificationsNonLues(
                        utilisateur
                )
        );
    }

    @PutMapping("/{id}/lire")
    @PreAuthorize("@authorizationService.peutAccederNotification(#p0, authentication)")
    public ResponseEntity<Notification> marquerCommeLue(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                notificationService.marquerCommeLue(id)
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("@authorizationService.peutAccederNotification(#p0, authentication)")
    public ResponseEntity<Notification> modifierNotification(
            @PathVariable Long id,
            @RequestBody Notification notification) {

        return ResponseEntity.ok(
                notificationService.modifierNotification(
                        id,
                        notification
                )
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@authorizationService.peutAccederNotification(#p0, authentication)")
    public ResponseEntity<Void> supprimerNotification(
            @PathVariable Long id) {

        notificationService.supprimerNotification(id);

        return ResponseEntity.noContent().build();
    }
}
