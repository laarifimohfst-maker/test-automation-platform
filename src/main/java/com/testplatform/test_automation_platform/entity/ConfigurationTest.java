package com.testplatform.test_automation_platform.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "configurations_tests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfigurationTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean testsUnitaires;

    private boolean testsIntegration;

    private boolean testsApi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projet_id", nullable = false)
    private Projet projet;
}