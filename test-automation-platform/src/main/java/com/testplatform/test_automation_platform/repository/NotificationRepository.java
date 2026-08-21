package com.testplatform.test_automation_platform.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.testplatform.test_automation_platform.entity.Notification;
import com.testplatform.test_automation_platform.entity.Utilisateur;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    List<Notification> findByUtilisateur(
            Utilisateur utilisateur);

    List<Notification> findByUtilisateurAndLueFalse(
            Utilisateur utilisateur);

    /*
     * Supprime toutes les notifications
     * liées à une exécution.
     */
    @Modifying
    @Query(
        value = "DELETE FROM notifications WHERE execution_id = :executionId",
        nativeQuery = true
    )
    void deleteByExecutionId(
            @Param("executionId") Long executionId);
}