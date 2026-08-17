package com.testplatform.test_automation_platform.repository;

import com.testplatform.test_automation_platform.entity.ExecutionTest;
import com.testplatform.test_automation_platform.entity.Projet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExecutionTestRepository extends JpaRepository<ExecutionTest, Long> {

    List<ExecutionTest> findByProjet(Projet projet);
}