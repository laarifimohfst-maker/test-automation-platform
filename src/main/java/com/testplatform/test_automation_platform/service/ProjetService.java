package com.testplatform.test_automation_platform.service;

import com.testplatform.test_automation_platform.entity.Projet;
import com.testplatform.test_automation_platform.entity.Utilisateur;
import com.testplatform.test_automation_platform.enums.StatutProjet;
import com.testplatform.test_automation_platform.repository.ProjetRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjetService {

    private final ProjetRepository projetRepository;

    public ProjetService(ProjetRepository projetRepository) {
        this.projetRepository = projetRepository;
    }

    public Projet creerProjet(Projet projet) {

        if (projet.getStatut() == null) {
            projet.setStatut(StatutProjet.IMPORTE);
        }

        return projetRepository.save(projet);
    }

    public List<Projet> obtenirTousLesProjets() {
        return projetRepository.findAll();
    }

    public Projet obtenirProjetParId(Long id) {
        return projetRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Projet introuvable."));
    }

    public List<Projet> obtenirProjetsUtilisateur(Utilisateur utilisateur) {
        return projetRepository.findByUtilisateur(utilisateur);
    }

    public Projet modifierProjet(Long id, Projet projetModifie) {

        Projet projet = obtenirProjetParId(id);

        projet.setNom(projetModifie.getNom());
        projet.setDescription(projetModifie.getDescription());
        projet.setTypeSource(projetModifie.getTypeSource());
        projet.setStatut(projetModifie.getStatut());

        return projetRepository.save(projet);
    }

    public void supprimerProjet(Long id) {

        if (!projetRepository.existsById(id)) {
            throw new IllegalArgumentException("Projet introuvable.");
        }

        projetRepository.deleteById(id);
    }
}