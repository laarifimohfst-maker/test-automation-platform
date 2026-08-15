package com.testplatform.test_automation_platform.controller;

import com.testplatform.test_automation_platform.entity.Rapport;
import com.testplatform.test_automation_platform.enums.TypeRapport;
import com.testplatform.test_automation_platform.service.RapportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.List;

@RestController
@RequestMapping("/api/rapports")
public class RapportController {

    private final RapportService rapportService;

    public RapportController(RapportService rapportService) {
        this.rapportService = rapportService;
    }

    @PostMapping("/tests")
    public ResponseEntity<Rapport> genererRapportTests(
            @RequestParam Long executionId) {

        return ResponseEntity.ok(
                rapportService.genererRapportTests(executionId)
        );
    }

    @PostMapping("/analyse-qualite")
    public ResponseEntity<Rapport> genererRapportAnalyseQualite(
            @RequestParam Long executionId) {

        return ResponseEntity.ok(
                rapportService.genererRapportAnalyseQualite(executionId)
        );
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> telechargerRapport(@PathVariable Long id) throws IOException {

        Rapport rapport = rapportService.obtenirRapportParId(id);

        if (rapport.getCheminFichier() == null) {
            return ResponseEntity.notFound().build();
        }

        byte[] fichier = Files.readAllBytes(Paths.get(rapport.getCheminFichier()));

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + rapport.getNom() + ".pdf\"")
                .body(fichier);
    }

    @PostMapping
    public ResponseEntity<Rapport> enregistrerRapport(
            @RequestBody Rapport rapport,
            @RequestParam Long executionId) {

        return ResponseEntity.ok(
                rapportService.enregistrerRapport(rapport, executionId)
        );
    }

    @GetMapping
    public ResponseEntity<List<Rapport>> obtenirTousLesRapports() {
        return ResponseEntity.ok(rapportService.obtenirTousLesRapports());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Rapport> obtenirRapportParId(@PathVariable Long id) {
        return ResponseEntity.ok(rapportService.obtenirRapportParId(id));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<Rapport>> obtenirRapportsParType(@PathVariable TypeRapport type) {
        return ResponseEntity.ok(rapportService.obtenirRapportsParType(type));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Rapport> modifierRapport(@PathVariable Long id, @RequestBody Rapport rapport) {
        return ResponseEntity.ok(rapportService.modifierRapport(id, rapport));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerRapport(@PathVariable Long id) {
        rapportService.supprimerRapport(id);
        return ResponseEntity.noContent().build();
    }
}