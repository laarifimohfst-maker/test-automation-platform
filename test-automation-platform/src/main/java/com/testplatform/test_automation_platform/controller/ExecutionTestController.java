package com.testplatform.test_automation_platform.controller;

import com.testplatform.test_automation_platform.entity.ConfigurationTest;
import com.testplatform.test_automation_platform.entity.ExecutionTest;
import com.testplatform.test_automation_platform.entity.Projet;
import com.testplatform.test_automation_platform.enums.StatutExecution;
import com.testplatform.test_automation_platform.service.ConfigurationTestService;
import com.testplatform.test_automation_platform.service.ExecutionTestService;
import com.testplatform.test_automation_platform.service.ProjetService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/executions-tests")
public class ExecutionTestController {

    private final ExecutionTestService executionTestService;
    private final ProjetService projetService;
    private final ConfigurationTestService configurationTestService;

    public ExecutionTestController(
            ExecutionTestService executionTestService,
            ProjetService projetService,
            ConfigurationTestService configurationTestService) {

        this.executionTestService = executionTestService;
        this.projetService = projetService;
        this.configurationTestService = configurationTestService;
    }

    @PostMapping
    @PreAuthorize("@authorizationService.peutAccederProjet(#p0, authentication)"
            + " && @authorizationService.peutAccederConfiguration(#p1, authentication)")
    public ResponseEntity<ExecutionTest> lancerTest(
            @RequestParam Long projetId,
            @RequestParam Long configurationTestId) {

        try {

            Projet projet = projetService.obtenirProjetParId(projetId);

            ConfigurationTest configuration =
                    configurationTestService.obtenirConfigurationParId(
                            configurationTestId
                    );

            return ResponseEntity.ok(
                    executionTestService.lancerTest(
                            projet,
                            configuration
                    )
            );

        } catch (Exception e) {

            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("@authorizationService.peutAccederExecution(#p0, authentication)")
    public ResponseEntity<ExecutionTest> obtenirExecutionParId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                executionTestService.obtenirExecutionParId(id)
        );
    }

    @GetMapping("/projet/{projetId}")
    @PreAuthorize("@authorizationService.peutAccederProjet(#p0, authentication)")
    public ResponseEntity<List<ExecutionTest>> obtenirExecutionsParProjet(
            @PathVariable Long projetId) {

        Projet projet = projetService.obtenirProjetParId(projetId);

        return ResponseEntity.ok(
                executionTestService.obtenirExecutionsParProjet(projet)
        );
    }

    @PutMapping("/{id}/demarrer")
    @PreAuthorize("@authorizationService.peutAccederExecution(#p0, authentication)")
    public ResponseEntity<ExecutionTest> demarrerExecution(
            @PathVariable Long id) {

        try {

            return ResponseEntity.ok(
                    executionTestService.executerTests(id)
            );

        } catch (Exception e) {

            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}/terminer")
    @PreAuthorize("@authorizationService.peutAccederExecution(#p0, authentication)")
    public ResponseEntity<ExecutionTest> terminerExecution(
            @PathVariable Long id,
            @RequestParam StatutExecution statut,
            @RequestParam(required = false) String message) {

        return ResponseEntity.ok(
                executionTestService.terminerExecution(
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

        executionTestService.supprimerExecution(id);

        return ResponseEntity.noContent().build();
    }
}
