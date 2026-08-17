package com.testplatform.test_automation_platform.repository;

import com.testplatform.test_automation_platform.entity.AnalyseQualite;
import com.testplatform.test_automation_platform.entity.ExecutionAnalyseQualite;
import com.testplatform.test_automation_platform.enums.StatutQualityGate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AnalyseQualiteRepository
        extends JpaRepository<AnalyseQualite, Long> {

    Optional<AnalyseQualite> findByExecutionAnalyseQualite(
            ExecutionAnalyseQualite executionAnalyseQualite
    );

    Optional<AnalyseQualite> findByExecutionAnalyseQualite_Id(Long executionId);

    @Query("SELECT COUNT(a) FROM AnalyseQualite a WHERE a.executionAnalyseQualite.projet.utilisateur.id = :utilisateurId AND a.qualityGateStatus = :statut")
    long countByUtilisateurEtQualityGateStatus(@Param("utilisateurId") Long utilisateurId, @Param("statut") StatutQualityGate statut);
}