package com.testplatform.test_automation_platform.controller;

import com.testplatform.test_automation_platform.entity.ExecutionAnalyseQualite;
import com.testplatform.test_automation_platform.entity.Projet;
import com.testplatform.test_automation_platform.enums.StatutExecution;
import com.testplatform.test_automation_platform.service.ExecutionAnalyseQualiteService;
import com.testplatform.test_automation_platform.service.ProjetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/executions-analyse-qualite")
public class ExecutionAnalyseQualiteController {

    private final ExecutionAnalyseQualiteService executionService;
    private final ProjetService projetService;

    public ExecutionAnalyseQualiteController(
            ExecutionAnalyseQualiteService executionService,
            ProjetService projetService) {

        this.executionService = executionService;
        this.projetService = projetService;
    }

    @PostMapping
    public ResponseEntity<ExecutionAnalyseQualite> creerExecution(
            @RequestParam Long projetId) {

        Projet projet = projetService.obtenirProjetParId(projetId);

        return ResponseEntity.ok(
                executionService.creerExecution(projet)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExecutionAnalyseQualite> obtenirExecutionParId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                executionService.obtenirExecutionParId(id)
        );
    }

    @GetMapping("/projet/{projetId}")
    public ResponseEntity<List<ExecutionAnalyseQualite>>
    obtenirExecutionsParProjet(
            @PathVariable Long projetId) {

        Projet projet = projetService.obtenirProjetParId(projetId);

        return ResponseEntity.ok(
                executionService.obtenirExecutionsParProjet(projet)
        );
    }

    @PutMapping("/{id}/demarrer")
    public ResponseEntity<ExecutionAnalyseQualite> demarrerExecution(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                executionService.demarrerExecution(id)
        );
    }

    @PutMapping("/{id}/terminer")
    public ResponseEntity<ExecutionAnalyseQualite> terminerExecution(
            @PathVariable Long id,
            @RequestParam StatutExecution statut,
            @RequestParam(required = false) String message) {

        return ResponseEntity.ok(
                executionService.terminerExecution(
                        id,
                        statut,
                        message
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerExecution(
            @PathVariable Long id) {

        executionService.supprimerExecution(id);

        return ResponseEntity.noContent().build();
    }
}