package com.testplatform.test_automation_platform.repository;

import com.testplatform.test_automation_platform.entity.Notification;
import com.testplatform.test_automation_platform.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUtilisateur(Utilisateur utilisateur);

    List<Notification> findByUtilisateurAndLueFalse(Utilisateur utilisateur);
}