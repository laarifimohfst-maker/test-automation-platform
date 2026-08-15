package com.testplatform.test_automation_platform.repository;

import com.testplatform.test_automation_platform.entity.ExecutionTest;
import com.testplatform.test_automation_platform.entity.ResultatTest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResultatTestRepository extends JpaRepository<ResultatTest, Long> {

    List<ResultatTest> findByExecutionTest(ExecutionTest executionTest);
    List<ResultatTest> findByExecutionTest_Id(Long executionId);
}