package com.testplatform.test_automation_platform.controller;

import com.testplatform.test_automation_platform.entity.ExecutionAnalyseQualite;
import com.testplatform.test_automation_platform.entity.Projet;
import com.testplatform.test_automation_platform.enums.StatutExecution;
import com.testplatform.test_automation_platform.service.ExecutionAnalyseQualiteService;
import com.testplatform.test_automation_platform.service.ProjetService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/executions-analyse-qualite")
public class ExecutionAnalyseQualiteController {

    private final ExecutionAnalyseQualiteService executionAnalyseQualiteService;
    private final ProjetService projetService;

    public ExecutionAnalyseQualiteController(
            ExecutionAnalyseQualiteService executionAnalyseQualiteService,
            ProjetService projetService) {

        this.executionAnalyseQualiteService = executionAnalyseQualiteService;
        this.projetService = projetService;
    }

    @PostMapping
    @PreAuthorize("@authorizationService.peutAccederProjet(#p0, authentication)")
    public ResponseEntity<ExecutionAnalyseQualite> creerExecution(
            @RequestParam Long projetId) {

        Projet projet = projetService.obtenirProjetParId(projetId);

        return ResponseEntity.ok(
                executionAnalyseQualiteService.creerExecution(projet)
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("@authorizationService.peutAccederExecution(#p0, authentication)")
    public ResponseEntity<ExecutionAnalyseQualite> obtenirExecutionParId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                executionAnalyseQualiteService.obtenirExecutionParId(id)
        );
    }

    @GetMapping("/projet/{projetId}")
    @PreAuthorize("@authorizationService.peutAccederProjet(#p0, authentication)")
    public ResponseEntity<List<ExecutionAnalyseQualite>>
    obtenirExecutionsParProjet(
            @PathVariable Long projetId) {

        Projet projet = projetService.obtenirProjetParId(projetId);

        return ResponseEntity.ok(
                executionAnalyseQualiteService.obtenirExecutionsParProjet(projet)
        );
    }

    @PutMapping("/{id}/demarrer")
    @PreAuthorize("@authorizationService.peutAccederExecution(#p0, authentication)")
    public ResponseEntity<ExecutionAnalyseQualite> demarrerExecution(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                executionAnalyseQualiteService.demarrerExecution(id)
        );
    }

    @PutMapping("/{id}/terminer")
    @PreAuthorize("@authorizationService.peutAccederExecution(#p0, authentication)")
    public ResponseEntity<ExecutionAnalyseQualite> terminerExecution(
            @PathVariable Long id,
            @RequestParam StatutExecution statut,
            @RequestParam(required = false) String message) {

        return ResponseEntity.ok(
                executionAnalyseQualiteService.terminerExecution(
                        id,
                        statut,
                        message
                )
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@authorizationService.peutAccederExecution(#p0, authentication)")
    public ResponseEntity<Void> supprimerExecution(
            @PathVariable Long id) {

        executionAnalyseQualiteService.supprimerExecution(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/executer")
    @PreAuthorize("@authorizationService.peutAccederExecution(#p0, authentication)")
    public ResponseEntity<ExecutionAnalyseQualite> executerAnalyse(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                executionAnalyseQualiteService.executerAnalyse(id)
        );
    }


}
