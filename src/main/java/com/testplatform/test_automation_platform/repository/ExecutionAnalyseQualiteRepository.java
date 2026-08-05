package com.testplatform.test_automation_platform.repository;

import com.testplatform.test_automation_platform.entity.ExecutionAnalyseQualite;
import com.testplatform.test_automation_platform.entity.Projet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExecutionAnalyseQualiteRepository
        extends JpaRepository<ExecutionAnalyseQualite, Long> {

    List<ExecutionAnalyseQualite> findByProjet(Projet projet);
}