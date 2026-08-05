package com.testplatform.test_automation_platform.service;

import com.testplatform.test_automation_platform.entity.ExecutionTest;
import com.testplatform.test_automation_platform.entity.ResultatTest;
import com.testplatform.test_automation_platform.repository.ResultatTestRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResultatTestService {

    private final ResultatTestRepository resultatTestRepository;

    public ResultatTestService(
            ResultatTestRepository resultatTestRepository) {
        this.resultatTestRepository = resultatTestRepository;
    }

    public ResultatTest enregistrerResultat(ResultatTest resultatTest) {
        return resultatTestRepository.save(resultatTest);
    }

    public ResultatTest obtenirResultatParId(Long id) {
        return resultatTestRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Résultat de test introuvable."));
    }

    public List<ResultatTest> obtenirResultatsParExecution(
            ExecutionTest executionTest) {

        return resultatTestRepository.findByExecutionTest(executionTest);
    }

    public ResultatTest modifierResultat(
            Long id,
            ResultatTest resultatModifie) {

        ResultatTest resultat = obtenirResultatParId(id);

        resultat.setType(resultatModifie.getType());
        resultat.setNomTest(resultatModifie.getNomTest());
        resultat.setStatut(resultatModifie.getStatut());
        resultat.setDuree(resultatModifie.getDuree());
        resultat.setMessage(resultatModifie.getMessage());

        return resultatTestRepository.save(resultat);
    }

    public void supprimerResultat(Long id) {

        if (!resultatTestRepository.existsById(id)) {
            throw new IllegalArgumentException(
                    "Résultat de test introuvable.");
        }

        resultatTestRepository.deleteById(id);
    }
}