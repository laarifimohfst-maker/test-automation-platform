package com.testplatform.test_automation_platform.controller;

import com.testplatform.test_automation_platform.entity.Rapport;
import com.testplatform.test_automation_platform.enums.TypeRapport;
import com.testplatform.test_automation_platform.service.RapportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rapports")
public class RapportController {

    private final RapportService rapportService;

    public RapportController(RapportService rapportService) {
        this.rapportService = rapportService;
    }

    @PostMapping
    public ResponseEntity<Rapport> enregistrerRapport(
            @RequestBody Rapport rapport) {

        return ResponseEntity.ok(
                rapportService.enregistrerRapport(rapport)
        );
    }

    @GetMapping
    public ResponseEntity<List<Rapport>> obtenirTousLesRapports() {

        return ResponseEntity.ok(
                rapportService.obtenirTousLesRapports()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Rapport> obtenirRapportParId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                rapportService.obtenirRapportParId(id)
        );
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<Rapport>> obtenirRapportsParType(
            @PathVariable TypeRapport type) {

        return ResponseEntity.ok(
                rapportService.obtenirRapportsParType(type)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<Rapport> modifierRapport(
            @PathVariable Long id,
            @RequestBody Rapport rapport) {

        return ResponseEntity.ok(
                rapportService.modifierRapport(id, rapport)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerRapport(
            @PathVariable Long id) {

        rapportService.supprimerRapport(id);

        return ResponseEntity.noContent().build();
    }
}