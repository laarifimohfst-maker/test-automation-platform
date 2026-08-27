package com.testplatform.test_automation_platform.security;

import com.testplatform.test_automation_platform.entity.AnalyseQualite;
import com.testplatform.test_automation_platform.entity.ConfigurationTest;
import com.testplatform.test_automation_platform.entity.Notification;
import com.testplatform.test_automation_platform.entity.Projet;
import com.testplatform.test_automation_platform.entity.ResultatTest;
import com.testplatform.test_automation_platform.entity.Utilisateur;
import com.testplatform.test_automation_platform.repository.AnalyseQualiteRepository;
import com.testplatform.test_automation_platform.repository.ConfigurationTestRepository;
import com.testplatform.test_automation_platform.repository.ExecutionRepository;
import com.testplatform.test_automation_platform.repository.NotificationRepository;
import com.testplatform.test_automation_platform.repository.ProjetRepository;
import com.testplatform.test_automation_platform.repository.RapportRepository;
import com.testplatform.test_automation_platform.repository.ResultatTestRepository;
import com.testplatform.test_automation_platform.repository.UtilisateurRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("authorizationService")
@Transactional(readOnly = true)
public class AuthorizationService {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final UtilisateurRepository utilisateurRepository;
    private final ProjetRepository projetRepository;
    private final ExecutionRepository executionRepository;
    private final RapportRepository rapportRepository;
    private final NotificationRepository notificationRepository;
    private final ConfigurationTestRepository configurationTestRepository;
    private final ResultatTestRepository resultatTestRepository;
    private final AnalyseQualiteRepository analyseQualiteRepository;

    public AuthorizationService(
            UtilisateurRepository utilisateurRepository,
            ProjetRepository projetRepository,
            ExecutionRepository executionRepository,
            RapportRepository rapportRepository,
            NotificationRepository notificationRepository,
            ConfigurationTestRepository configurationTestRepository,
            ResultatTestRepository resultatTestRepository,
            AnalyseQualiteRepository analyseQualiteRepository) {
        this.utilisateurRepository = utilisateurRepository;
        this.projetRepository = projetRepository;
        this.executionRepository = executionRepository;
        this.rapportRepository = rapportRepository;
        this.notificationRepository = notificationRepository;
        this.configurationTestRepository = configurationTestRepository;
        this.resultatTestRepository = resultatTestRepository;
        this.analyseQualiteRepository = analyseQualiteRepository;
    }

    public boolean estAdmin(Authentication authentication) {
        return authentication != null
                && authentication.getAuthorities().stream()
                .anyMatch(authority -> ROLE_ADMIN.equals(authority.getAuthority()));
    }

    public boolean peutAccederUtilisateur(
            Long utilisateurId,
            Authentication authentication) {
        if (estAdmin(authentication)) {
            return true;
        }
        return utilisateurRepository.findById(utilisateurId)
                .map(utilisateur -> estUtilisateurCourant(utilisateur, authentication))
                .orElse(false);
    }

    public boolean peutAccederEmail(
            String email,
            Authentication authentication) {
        return estAdmin(authentication)
                || email != null
                && authentication != null
                && email.equalsIgnoreCase(authentication.getName());
    }

    public boolean peutAccederProjet(
            Long projetId,
            Authentication authentication) {
        if (estAdmin(authentication)) {
            return true;
        }
        return projetRepository.findById(projetId)
                .map(Projet::getUtilisateur)
                .map(utilisateur -> estUtilisateurCourant(utilisateur, authentication))
                .orElse(false);
    }

    public boolean peutCreerProjet(
            Projet projet,
            Authentication authentication) {
        if (estAdmin(authentication)) {
            return true;
        }
        if (projet == null || projet.getUtilisateur() == null) {
            return false;
        }
        if (projet.getUtilisateur().getId() != null) {
            return peutAccederUtilisateur(
                    projet.getUtilisateur().getId(),
                    authentication
            );
        }
        return estUtilisateurCourant(projet.getUtilisateur(), authentication);
    }

    public boolean peutAccederExecution(
            Long executionId,
            Authentication authentication) {
        if (estAdmin(authentication)) {
            return true;
        }
        return executionRepository.findById(executionId)
                .map(execution -> execution.getProjet().getUtilisateur())
                .map(utilisateur -> estUtilisateurCourant(utilisateur, authentication))
                .orElse(false);
    }

    public boolean peutAccederRapport(
            Long rapportId,
            Authentication authentication) {
        if (estAdmin(authentication)) {
            return true;
        }
        return rapportRepository.findById(rapportId)
                .map(rapport -> rapport.getExecution().getProjet().getUtilisateur())
                .map(utilisateur -> estUtilisateurCourant(utilisateur, authentication))
                .orElse(false);
    }

    public boolean peutAccederNotification(
            Long notificationId,
            Authentication authentication) {
        if (estAdmin(authentication)) {
            return true;
        }
        return notificationRepository.findById(notificationId)
                .map(Notification::getUtilisateur)
                .map(utilisateur -> estUtilisateurCourant(utilisateur, authentication))
                .orElse(false);
    }

    public boolean peutAccederConfiguration(
            Long configurationId,
            Authentication authentication) {
        if (estAdmin(authentication)) {
            return true;
        }
        return configurationTestRepository.findById(configurationId)
                .map(ConfigurationTest::getProjet)
                .map(Projet::getUtilisateur)
                .map(utilisateur -> estUtilisateurCourant(utilisateur, authentication))
                .orElse(false);
    }

    public boolean peutCreerConfiguration(
            ConfigurationTest configuration,
            Authentication authentication) {
        return configuration != null
                && configuration.getProjet() != null
                && configuration.getProjet().getId() != null
                && peutAccederProjet(
                        configuration.getProjet().getId(),
                        authentication
                );
    }

    public boolean peutAccederResultat(
            Long resultatId,
            Authentication authentication) {
        if (estAdmin(authentication)) {
            return true;
        }
        return resultatTestRepository.findById(resultatId)
                .map(resultat -> resultat.getExecutionTest()
                        .getProjet().getUtilisateur())
                .map(utilisateur -> estUtilisateurCourant(utilisateur, authentication))
                .orElse(false);
    }

    public boolean peutCreerResultat(
            ResultatTest resultat,
            Authentication authentication) {
        return resultat != null
                && resultat.getExecutionTest() != null
                && resultat.getExecutionTest().getId() != null
                && peutAccederExecution(
                        resultat.getExecutionTest().getId(),
                        authentication
                );
    }

    public boolean peutAccederAnalyse(
            Long analyseId,
            Authentication authentication) {
        if (estAdmin(authentication)) {
            return true;
        }
        return analyseQualiteRepository.findById(analyseId)
                .map(AnalyseQualite::getExecutionAnalyseQualite)
                .map(execution -> execution.getProjet().getUtilisateur())
                .map(utilisateur -> estUtilisateurCourant(utilisateur, authentication))
                .orElse(false);
    }

    public boolean peutCreerAnalyse(
            AnalyseQualite analyse,
            Authentication authentication) {
        return analyse != null
                && analyse.getExecutionAnalyseQualite() != null
                && analyse.getExecutionAnalyseQualite().getId() != null
                && peutAccederExecution(
                        analyse.getExecutionAnalyseQualite().getId(),
                        authentication
                );
    }

    private boolean estUtilisateurCourant(
            Utilisateur utilisateur,
            Authentication authentication) {
        return utilisateur != null
                && utilisateur.getEmail() != null
                && authentication != null
                && utilisateur.getEmail().equalsIgnoreCase(authentication.getName());
    }
}
