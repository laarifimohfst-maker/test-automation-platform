package com.testplatform.test_automation_platform.controller;

import com.testplatform.test_automation_platform.entity.Execution;
import com.testplatform.test_automation_platform.entity.Projet;
import com.testplatform.test_automation_platform.enums.StatutExecution;
import com.testplatform.test_automation_platform.enums.TypeExecution;
import com.testplatform.test_automation_platform.service.ExecutionService;
import com.testplatform.test_automation_platform.service.ProjetService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/executions")
public class ExecutionController {

    private final ExecutionService executionService;
    private final ProjetService projetService;

    public ExecutionController(
            ExecutionService executionService,
            ProjetService projetService) {

        this.executionService = executionService;
        this.projetService = projetService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Execution>> obtenirToutesLesExecutions(
            @RequestParam(required = false) String recherche,
            @RequestParam(required = false) Long utilisateurId,
            @RequestParam(required = false) Long projetId,
            @RequestParam(required = false) StatutExecution statut,
            @RequestParam(required = false) TypeExecution type) {
        return ResponseEntity.ok(
                executionService.rechercherExecutionsAdministration(
                        recherche,
                        utilisateurId,
                        projetId,
                        statut,
                        type
                )
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("@authorizationService.peutAccederExecution(#p0, authentication)")
    public ResponseEntity<Execution> obtenirExecutionParId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                executionService.obtenirExecutionParId(id)
        );
    }

    @GetMapping("/projet/{projetId}")
    @PreAuthorize("@authorizationService.peutAccederProjet(#p0, authentication)")
    public ResponseEntity<List<Execution>> obtenirExecutionsParProjet(
            @PathVariable Long projetId) {

        Projet projet = projetService.obtenirProjetParId(projetId);

        return ResponseEntity.ok(
                executionService.obtenirExecutionsParProjet(projet)
        );
    }

    @PutMapping("/{id}/statut")
    @PreAuthorize("@authorizationService.peutAccederExecution(#p0, authentication)")
    public ResponseEntity<Execution> mettreAJourStatut(
            @PathVariable Long id,
            @RequestParam StatutExecution statut) {

        return ResponseEntity.ok(
                executionService.mettreAJourStatut(id, statut)
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@authorizationService.peutAccederExecution(#p0, authentication)")
    public ResponseEntity<Void> supprimerExecution(
            @PathVariable Long id) {

        executionService.supprimerExecution(id);

        return ResponseEntity.noContent().build();
    }
}
