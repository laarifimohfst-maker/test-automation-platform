package com.testplatform.test_automation_platform.entity;

import com.testplatform.test_automation_platform.enums.StatutQualityGate;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "analyses_qualite")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyseQualite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer bugs;

    private Integer vulnerabilites;

    private Integer codeSmells;

    private Double duplication;

    private Double coverage;

    @Enumerated(EnumType.STRING)
    private StatutQualityGate qualityGateStatus;

    private LocalDateTime dateAnalyse;

    @Column(columnDefinition = "TEXT")
    private String issuesJson;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "execution_analyse_qualite_id", nullable = false, unique = true)
    private ExecutionAnalyseQualite executionAnalyseQualite;
}