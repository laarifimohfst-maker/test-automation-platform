package com.testplatform.test_automation_platform.service;

import com.testplatform.test_automation_platform.entity.Rapport;
import com.testplatform.test_automation_platform.enums.TypeRapport;
import com.testplatform.test_automation_platform.repository.RapportRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RapportService {

    private final RapportRepository rapportRepository;

    public RapportService(RapportRepository rapportRepository) {
        this.rapportRepository = rapportRepository;
    }

    public Rapport enregistrerRapport(Rapport rapport) {

        if (rapport.getDateGeneration() == null) {
            rapport.setDateGeneration(LocalDateTime.now());
        }

        return rapportRepository.save(rapport);
    }

    public Rapport obtenirRapportParId(Long id) {

        return rapportRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Rapport introuvable."));
    }

    public List<Rapport> obtenirRapportsParType(TypeRapport type) {
        return rapportRepository.findByType(type);
    }

    public List<Rapport> obtenirTousLesRapports() {
        return rapportRepository.findAll();
    }

    public Rapport modifierRapport(
            Long id,
            Rapport rapportModifie) {

        Rapport rapport = obtenirRapportParId(id);

        rapport.setNom(rapportModifie.getNom());
        rapport.setCheminFichier(rapportModifie.getCheminFichier());
        rapport.setType(rapportModifie.getType());
        rapport.setTaille(rapportModifie.getTaille());

        return rapportRepository.save(rapport);
    }

    public void supprimerRapport(Long id) {

        if (!rapportRepository.existsById(id)) {
            throw new IllegalArgumentException(
                    "Rapport introuvable.");
        }

        rapportRepository.deleteById(id);
    }
}