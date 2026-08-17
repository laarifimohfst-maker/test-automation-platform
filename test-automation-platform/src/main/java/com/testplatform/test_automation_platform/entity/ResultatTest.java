package com.testplatform.test_automation_platform.entity;

import com.testplatform.test_automation_platform.enums.StatutTest;
import com.testplatform.test_automation_platform.enums.TypeTest;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "resultats_tests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResultatTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeTest type;

    private String nomTest;

    @Enumerated(EnumType.STRING)
    private StatutTest statut;

    private Long duree;

    @Column(columnDefinition = "TEXT")
    private String message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "execution_test_id", nullable = false)
    private ExecutionTest executionTest;
}