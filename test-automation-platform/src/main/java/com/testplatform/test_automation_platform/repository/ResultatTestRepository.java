package com.testplatform.test_automation_platform.repository;

import com.testplatform.test_automation_platform.entity.ExecutionTest;
import com.testplatform.test_automation_platform.entity.ResultatTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import com.testplatform.test_automation_platform.enums.StatutTest;
import java.util.List;

public interface ResultatTestRepository extends JpaRepository<ResultatTest, Long> {

    List<ResultatTest> findByExecutionTest(ExecutionTest executionTest);
    List<ResultatTest> findByExecutionTest_Id(Long executionId);

    @Modifying
    @Query(
        value = "DELETE FROM resultats_tests WHERE execution_test_id = :executionId",
        nativeQuery = true
    )
    void deleteByExecutionId(@Param("executionId") Long executionId);

    @Query("SELECT COUNT(r) FROM ResultatTest r WHERE r.executionTest.projet.utilisateur.id = :utilisateurId AND r.statut = :statut")
    long countByUtilisateurEtStatut(@Param("utilisateurId") Long utilisateurId, @Param("statut") StatutTest statut);

}
