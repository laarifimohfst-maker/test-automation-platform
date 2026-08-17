package com.testplatform.test_automation_platform.service;

import com.testplatform.test_automation_platform.entity.Utilisateur;
import com.testplatform.test_automation_platform.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;

    public UtilisateurService(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    public Utilisateur creerUtilisateur(Utilisateur utilisateur) {

        if (utilisateurRepository.existsByEmail(utilisateur.getEmail())) {
            throw new IllegalArgumentException("Cet email est déjà utilisé.");
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
        return utilisateurRepository.findByEmail(email);
    }

    public Utilisateur modifierUtilisateur(Long id, Utilisateur utilisateurModifie) {

        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Utilisateur introuvable."));

        utilisateur.setNom(utilisateurModifie.getNom());
        utilisateur.setEmail(utilisateurModifie.getEmail());
        utilisateur.setRole(utilisateurModifie.getRole());

        return utilisateurRepository.save(utilisateur);
    }

    public void supprimerUtilisateur(Long id) {

        if (!utilisateurRepository.existsById(id)) {
            throw new IllegalArgumentException("Utilisateur introuvable.");
        }

        utilisateurRepository.deleteById(id);
    }
}