package com.testplatform.test_automation_platform.repository;

import com.testplatform.test_automation_platform.entity.Execution;
import com.testplatform.test_automation_platform.entity.Projet;
import com.testplatform.test_automation_platform.enums.StatutExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;

public interface ExecutionRepository extends JpaRepository<Execution, Long> {

    List<Execution> findByProjet(Projet projet);

    @Query(value = "SELECT DATE(e.date_debut) as jour, COUNT(*) as total " +
            "FROM executions e " +
            "JOIN projets p ON e.projet_id = p.id " +
            "WHERE p.utilisateur_id = :utilisateurId " +
            "AND e.date_debut >= :dateDebut " +
            "GROUP BY DATE(e.date_debut) " +
            "ORDER BY jour", nativeQuery = true)
    List<Object[]> compterExecutionsParJour(@Param("utilisateurId") Long utilisateurId, @Param("dateDebut") LocalDateTime dateDebut);

    @Query("SELECT COUNT(e) FROM Execution e WHERE e.projet.utilisateur.id = :utilisateurId")
    long countByProjet_Utilisateur_Id(@Param("utilisateurId") Long utilisateurId);

    @Query("SELECT e FROM Execution e WHERE e.projet.utilisateur.id = :utilisateurId ORDER BY e.dateDebut DESC")
    List<Execution> findDernieresExecutionsParUtilisateur(@Param("utilisateurId") Long utilisateurId, Pageable pageable);

    long countByStatut(StatutExecution statut);

    List<Execution> findTop5ByOrderByDateDebutDesc();
}
