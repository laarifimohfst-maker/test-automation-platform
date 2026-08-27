package com.testplatform.test_automation_platform.controller;

import com.testplatform.test_automation_platform.entity.ExecutionTest;
import com.testplatform.test_automation_platform.entity.ResultatTest;
import com.testplatform.test_automation_platform.service.ExecutionTestService;
import com.testplatform.test_automation_platform.service.ResultatTestService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resultats-tests")
public class ResultatTestController {

    private final ResultatTestService resultatTestService;
    private final ExecutionTestService executionTestService;

    public ResultatTestController(
            ResultatTestService resultatTestService,
            ExecutionTestService executionTestService) {

        this.resultatTestService = resultatTestService;
        this.executionTestService = executionTestService;
    }

    @PostMapping
    @PreAuthorize("@authorizationService.peutCreerResultat(#p0, authentication)")
    public ResponseEntity<ResultatTest> enregistrerResultat(
            @RequestBody ResultatTest resultatTest) {

        return ResponseEntity.ok(
                resultatTestService.enregistrerResultat(resultatTest)
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("@authorizationService.peutAccederResultat(#p0, authentication)")
    public ResponseEntity<ResultatTest> obtenirResultatParId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                resultatTestService.obtenirResultatParId(id)
        );
    }

    @GetMapping("/execution/{executionId}")
    @PreAuthorize("@authorizationService.peutAccederExecution(#p0, authentication)")
    public ResponseEntity<List<ResultatTest>> obtenirResultatsParExecution(
            @PathVariable Long executionId) {

        ExecutionTest execution =
                executionTestService.obtenirExecutionParId(executionId);

        return ResponseEntity.ok(
                resultatTestService.obtenirResultatsParExecution(execution)
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("@authorizationService.peutAccederResultat(#p0, authentication)")
    public ResponseEntity<ResultatTest> modifierResultat(
            @PathVariable Long id,
            @RequestBody ResultatTest resultatTest) {

        return ResponseEntity.ok(
                resultatTestService.modifierResultat(
                        id,
                        resultatTest
                )
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@authorizationService.peutAccederResultat(#p0, authentication)")
    public ResponseEntity<Void> supprimerResultat(
            @PathVariable Long id) {

        resultatTestService.supprimerResultat(id);

        return ResponseEntity.noContent().build();
    }
}
