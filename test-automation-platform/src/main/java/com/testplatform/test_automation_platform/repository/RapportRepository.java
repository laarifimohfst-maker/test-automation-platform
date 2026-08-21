package com.testplatform.test_automation_platform.repository;

import com.testplatform.test_automation_platform.entity.Rapport;
import com.testplatform.test_automation_platform.enums.TypeRapport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RapportRepository extends JpaRepository<Rapport, Long> {

    List<Rapport> findByType(TypeRapport type);

    Optional<Rapport> findByExecution_Id(Long executionId);

    @Modifying
    @Query(
        value = "DELETE FROM rapports WHERE execution_id = :executionId",
        nativeQuery = true
    )
    void deleteByExecutionId(@Param("executionId") Long executionId);
}
