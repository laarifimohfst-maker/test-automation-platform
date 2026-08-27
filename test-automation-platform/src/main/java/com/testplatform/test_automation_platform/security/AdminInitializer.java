package com.testplatform.test_automation_platform.security;

import com.testplatform.test_automation_platform.entity.Utilisateur;
import com.testplatform.test_automation_platform.enums.Role;
import com.testplatform.test_automation_platform.repository.UtilisateurRepository;
import com.testplatform.test_automation_platform.service.UtilisateurService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements ApplicationRunner {

    private final UtilisateurRepository utilisateurRepository;
    private final UtilisateurService utilisateurService;
    private final String email;
    private final String password;
    private final String name;

    public AdminInitializer(
            UtilisateurRepository utilisateurRepository,
            UtilisateurService utilisateurService,
            @Value("${security.bootstrap-admin.email:}") String email,
            @Value("${security.bootstrap-admin.password:}") String password,
            @Value("${security.bootstrap-admin.name:Administrateur}") String name) {
        this.utilisateurRepository = utilisateurRepository;
        this.utilisateurService = utilisateurService;
        this.email = email;
        this.password = password;
        this.name = name;
    }

    @Override
    public void run(ApplicationArguments args) {
        boolean emailPresent = email != null && !email.isBlank();
        boolean passwordPresent = password != null && !password.isBlank();

        if (!emailPresent && !passwordPresent) {
            return;
        }

        if (!emailPresent || !passwordPresent) {
            throw new IllegalStateException(
                    "ADMIN_EMAIL et ADMIN_PASSWORD doivent être définis ensemble."
            );
        }

        if (utilisateurRepository.existsByEmailIgnoreCase(email.trim())) {
            return;
        }

        Utilisateur administrateur = Utilisateur.builder()
                .nom(name == null || name.isBlank() ? "Administrateur" : name.trim())
                .email(email)
                .motDePasse(password)
                .role(Role.ADMIN)
                .build();

        utilisateurService.creerUtilisateur(administrateur);
    }
}
