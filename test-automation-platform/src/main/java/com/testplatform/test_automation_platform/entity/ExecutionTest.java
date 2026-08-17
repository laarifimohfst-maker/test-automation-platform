package com.testplatform.test_automation_platform.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "executions_tests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionTest extends Execution {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "configuration_test_id", nullable = false)
    private ConfigurationTest configurationTest;
}