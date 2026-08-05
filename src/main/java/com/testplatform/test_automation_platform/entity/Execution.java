package com.testplatform.test_automation_platform.entity;

import com.testplatform.test_automation_platform.enums.StatutExecution;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "executions")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class Execution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    protected LocalDateTime dateDebut;

    protected LocalDateTime dateFin;

    @Enumerated(EnumType.STRING)
    protected StatutExecution statut;

    protected String branche;

    protected String message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projet_id", nullable = false)
    protected Projet projet;
}