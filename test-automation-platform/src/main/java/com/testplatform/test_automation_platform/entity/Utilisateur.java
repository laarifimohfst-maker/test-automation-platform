package com.testplatform.test_automation_platform.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.testplatform.test_automation_platform.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "utilisateurs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String motDePasse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Builder.Default
    @Getter(AccessLevel.NONE)
    @Column(nullable = false, columnDefinition = "boolean default true")
    private Boolean actif = true;

    private LocalDateTime dateCreation;

    public boolean isActif() {
        return Boolean.TRUE.equals(actif);
    }
}
