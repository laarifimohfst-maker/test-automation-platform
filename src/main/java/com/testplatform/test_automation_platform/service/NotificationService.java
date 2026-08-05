package com.testplatform.test_automation_platform.service;

import com.testplatform.test_automation_platform.entity.Notification;
import com.testplatform.test_automation_platform.entity.Utilisateur;
import com.testplatform.test_automation_platform.enums.TypeNotification;
import com.testplatform.test_automation_platform.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(
            NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public Notification creerNotification(
            Notification notification) {

        if (notification.getDateEnvoi() == null) {
            notification.setDateEnvoi(LocalDateTime.now());
        }

        notification.setLue(false);

        return notificationRepository.save(notification);
    }

    public List<Notification> obtenirNotificationsUtilisateur(
            Utilisateur utilisateur) {

        return notificationRepository.findByUtilisateur(utilisateur);
    }

    public List<Notification> obtenirNotificationsNonLues(
            Utilisateur utilisateur) {

        return notificationRepository
                .findByUtilisateurAndLueFalse(utilisateur);
    }

    public Notification obtenirNotificationParId(Long id) {

        return notificationRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Notification introuvable."));
    }

    public Notification marquerCommeLue(Long id) {

        Notification notification =
                obtenirNotificationParId(id);

        notification.setLue(true);

        return notificationRepository.save(notification);
    }

    public Notification modifierNotification(
            Long id,
            Notification notificationModifiee) {

        Notification notification =
                obtenirNotificationParId(id);

        notification.setMessage(notificationModifiee.getMessage());
        notification.setType(notificationModifiee.getType());

        return notificationRepository.save(notification);
    }

    public void supprimerNotification(Long id) {

        if (!notificationRepository.existsById(id)) {
            throw new IllegalArgumentException(
                    "Notification introuvable.");
        }

        notificationRepository.deleteById(id);
    }
}