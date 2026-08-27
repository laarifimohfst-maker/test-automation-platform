package com.testplatform.test_automation_platform.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.testplatform.test_automation_platform.entity.Utilisateur;
import com.testplatform.test_automation_platform.enums.Role;
import com.testplatform.test_automation_platform.repository.UtilisateurRepository;

@Service
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    public UtilisateurService(
            UtilisateurRepository utilisateurRepository,
            PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Utilisateur creerUtilisateur(Utilisateur utilisateur) {

        validerNouvelUtilisateur(utilisateur);
        String emailNormalise = normaliserEmail(utilisateur.getEmail());

        if (utilisateurRepository.existsByEmailIgnoreCase(emailNormalise)) {
            throw new IllegalArgumentException("Cet email est déjà utilisé.");
        }

        utilisateur.setEmail(emailNormalise);
        utilisateur.setMotDePasse(
                passwordEncoder.encode(utilisateur.getMotDePasse())
        );
        utilisateur.setActif(true);
        if (utilisateur.getDateCreation() == null) {
            utilisateur.setDateCreation(LocalDateTime.now());
        }

        return utilisateurRepository.save(utilisateur);
    }

    public List<Utilisateur> obtenirTousLesUtilisateurs() {
        return utilisateurRepository.findAll();
    }

    public Optional<Utilisateur> obtenirUtilisateurParId(Long id) {
        return utilisateurRepository.findById(id);
    }

    public Optional<Utilisateur> obtenirUtilisateurParEmail(String email) {
        return utilisateurRepository.findByEmailIgnoreCase(normaliserEmail(email));
    }

    public Utilisateur modifierUtilisateur(
            Long id,
            Utilisateur utilisateurModifie,
            boolean modificationRoleAutorisee,
            String emailUtilisateurCourant) {

        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Utilisateur introuvable."));

        if (utilisateurModifie.getNom() != null) {
            utilisateur.setNom(utilisateurModifie.getNom().trim());
        }

        if (utilisateurModifie.getEmail() != null) {
            String emailNormalise = normaliserEmail(utilisateurModifie.getEmail());
            if (utilisateurRepository.existsByEmailIgnoreCaseAndIdNot(
                    emailNormalise,
                    id
            )) {
                throw new IllegalArgumentException("Cet email est déjà utilisé.");
            }
            utilisateur.setEmail(emailNormalise);
        }

        if (modificationRoleAutorisee && utilisateurModifie.getRole() != null) {
            verifierModificationRole(
                    utilisateur,
                    utilisateurModifie.getRole(),
                    emailUtilisateurCourant
            );
            utilisateur.setRole(utilisateurModifie.getRole());
        }

        return utilisateurRepository.save(utilisateur);
    }

    public Utilisateur changerStatutUtilisateur(
            Long id,
            boolean actif,
            String emailAdministrateurCourant) {

        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Utilisateur introuvable."));

        if (!actif && utilisateur.isActif()) {
            verifierDesactivation(utilisateur, emailAdministrateurCourant);
        }

        utilisateur.setActif(actif);
        return utilisateurRepository.save(utilisateur);
    }

    public void supprimerUtilisateur(Long id, String emailUtilisateurCourant) {

        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Utilisateur introuvable."));

        if (utilisateur.getEmail().equalsIgnoreCase(emailUtilisateurCourant)) {
            throw new IllegalArgumentException(
                    "Un administrateur ne peut pas supprimer son propre compte."
            );
        }

        if (utilisateur.getRole() == Role.ADMIN
                && utilisateur.isActif()
                && utilisateurRepository.countByRoleAndActifTrue(Role.ADMIN) <= 1) {
            throw new IllegalArgumentException(
                    "Le dernier administrateur ne peut pas être supprimé."
            );
        }

        utilisateurRepository.delete(utilisateur);
    }

    private void validerNouvelUtilisateur(Utilisateur utilisateur) {
        if (utilisateur == null
                || utilisateur.getNom() == null
                || utilisateur.getNom().isBlank()
                || utilisateur.getEmail() == null
                || utilisateur.getEmail().isBlank()
                || utilisateur.getMotDePasse() == null
                || utilisateur.getMotDePasse().isBlank()
                || utilisateur.getRole() == null) {
            throw new IllegalArgumentException(
                    "Le nom, l'email, le mot de passe et le rôle sont obligatoires."
            );
        }

        if (!utilisateur.getEmail().trim().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("L'adresse e-mail n'est pas valide.");
        }

        if (utilisateur.getMotDePasse().length() < 8) {
            throw new IllegalArgumentException(
                    "Le mot de passe doit contenir au moins 8 caractères."
            );
        }

        utilisateur.setNom(utilisateur.getNom().trim());
    }

    private void verifierModificationRole(
            Utilisateur utilisateur,
            Role nouveauRole,
            String emailUtilisateurCourant) {
        if (utilisateur.getRole() != Role.ADMIN || nouveauRole == Role.ADMIN) {
            return;
        }

        if (utilisateur.getEmail().equalsIgnoreCase(emailUtilisateurCourant)) {
            throw new IllegalArgumentException(
                    "Un administrateur ne peut pas retirer son propre rôle."
            );
        }

        if (utilisateur.isActif()
                && utilisateurRepository.countByRoleAndActifTrue(Role.ADMIN) <= 1) {
            throw new IllegalArgumentException(
                    "Le rôle du dernier administrateur ne peut pas être retiré."
            );
        }
    }

    private void verifierDesactivation(
            Utilisateur utilisateur,
            String emailAdministrateurCourant) {
        if (utilisateur.getEmail().equalsIgnoreCase(emailAdministrateurCourant)) {
            throw new IllegalArgumentException(
                    "Un administrateur ne peut pas désactiver son propre compte."
            );
        }

        if (utilisateur.getRole() == Role.ADMIN
                && utilisateurRepository.countByRoleAndActifTrue(Role.ADMIN) <= 1) {
            throw new IllegalArgumentException(
                    "Le dernier administrateur actif ne peut pas être désactivé."
            );
        }
    }

    private String normaliserEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("L'email est obligatoire.");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
