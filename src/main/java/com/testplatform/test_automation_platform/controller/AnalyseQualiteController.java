package com.testplatform.test_automation_platform.controller;

import com.testplatform.test_automation_platform.entity.AnalyseQualite;
import com.testplatform.test_automation_platform.entity.ExecutionAnalyseQualite;
import com.testplatform.test_automation_platform.service.AnalyseQualiteService;
import com.testplatform.test_automation_platform.service.ExecutionAnalyseQualiteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analyses-qualite")
public class AnalyseQualiteController {
    private final ExecutionAnalyseQualiteService executionService;
    private final AnalyseQualiteService analyseQualiteService;

    public AnalyseQualiteController(
            ExecutionAnalyseQualiteService executionService,
            AnalyseQualiteService analyseQualiteService) {

        this.executionService = executionService;
        this.analyseQualiteService = analyseQualiteService;
    }

    @PostMapping
    public ResponseEntity<AnalyseQualite> enregistrerAnalyse(
            @RequestBody AnalyseQualite analyseQualite) {

        return ResponseEntity.ok(
                analyseQualiteService.enregistrerAnalyse(analyseQualite)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnalyseQualite> obtenirAnalyseParId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                analyseQualiteService.obtenirAnalyseParId(id)
        );
    }

    @GetMapping("/execution/{executionId}")
    public ResponseEntity<AnalyseQualite> obtenirAnalyseParExecution(
            @PathVariable Long executionId) {

        ExecutionAnalyseQualite execution =
                executionService.obtenirExecutionParId(executionId);

        return ResponseEntity.ok(
                analyseQualiteService.obtenirAnalyseParExecution(execution)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<AnalyseQualite> modifierAnalyse(
            @PathVariable Long id,
            @RequestBody AnalyseQualite analyseQualite) {

        return ResponseEntity.ok(
                analyseQualiteService.modifierAnalyse(
                        id,
                        analyseQualite
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerAnalyse(
            @PathVariable Long id) {

        analyseQualiteService.supprimerAnalyse(id);

        return ResponseEntity.noContent().build();
    }
}