package com.testplatform.test_automation_platform.entity;

import com.testplatform.test_automation_platform.enums.TypeRapport;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "rapports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rapport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;

    private String cheminFichier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeRapport type;

    private LocalDateTime dateGeneration;

    private Long taille;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "execution_id", nullable = false, unique = true)
    private Execution execution;
}