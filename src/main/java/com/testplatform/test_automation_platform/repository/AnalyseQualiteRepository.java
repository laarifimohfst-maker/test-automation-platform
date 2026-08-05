package com.testplatform.test_automation_platform.repository;

import com.testplatform.test_automation_platform.entity.AnalyseQualite;
import com.testplatform.test_automation_platform.entity.ExecutionAnalyseQualite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AnalyseQualiteRepository
        extends JpaRepository<AnalyseQualite, Long> {

    Optional<AnalyseQualite> findByExecutionAnalyseQualite(
            ExecutionAnalyseQualite executionAnalyseQualite
    );
}