package com.testplatform.test_automation_platform.repository;

import com.testplatform.test_automation_platform.entity.ConfigurationTest;
import com.testplatform.test_automation_platform.entity.Projet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConfigurationTestRepository extends JpaRepository<ConfigurationTest, Long> {

    List<ConfigurationTest> findByProjet(Projet projet);
}