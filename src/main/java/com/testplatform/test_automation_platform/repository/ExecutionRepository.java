package com.testplatform.test_automation_platform.repository;

import com.testplatform.test_automation_platform.entity.Execution;
import com.testplatform.test_automation_platform.entity.Projet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExecutionRepository extends JpaRepository<Execution, Long> {

    List<Execution> findByProjet(Projet projet);
}