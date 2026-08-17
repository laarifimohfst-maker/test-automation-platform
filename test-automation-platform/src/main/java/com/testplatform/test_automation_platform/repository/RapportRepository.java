package com.testplatform.test_automation_platform.repository;

import com.testplatform.test_automation_platform.entity.Rapport;
import com.testplatform.test_automation_platform.enums.TypeRapport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RapportRepository extends JpaRepository<Rapport, Long> {

    List<Rapport> findByType(TypeRapport type);
}