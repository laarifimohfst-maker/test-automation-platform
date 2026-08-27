package com.testplatform.test_automation_platform.controller;

import com.testplatform.test_automation_platform.entity.Rapport;
import com.testplatform.test_automation_platform.enums.TypeRapport;
import com.testplatform.test_automation_platform.service.RapportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("@authorizationService.peutAccederExecution(#p0, authentication)")
    public ResponseEntity<Rapport> genererRapportTests(
            @RequestParam Long executionId) {

        return ResponseEntity.ok(
                rapportService.genererRapportTests(executionId)
        );
    }

    @PostMapping("/analyse-qualite")
    @PreAuthorize("@authorizationService.peutAccederExecution(#p0, authentication)")
    public ResponseEntity<Rapport> genererRapportAnalyseQualite(
            @RequestParam Long executionId) {

        return ResponseEntity.ok(
                rapportService.genererRapportAnalyseQualite(executionId)
        );
    }

    @GetMapping("/{id}/download")
    @PreAuthorize("@authorizationService.peutAccederRapport(#p0, authentication)")
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
    @PreAuthorize("@authorizationService.peutAccederExecution(#p1, authentication)")
    public ResponseEntity<Rapport> enregistrerRapport(
            @RequestBody Rapport rapport,
            @RequestParam Long executionId) {

        return ResponseEntity.ok(
                rapportService.enregistrerRapport(rapport, executionId)
        );
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Rapport>> obtenirTousLesRapports() {
        return ResponseEntity.ok(rapportService.obtenirTousLesRapports());
    }

    @GetMapping("/{id}")
    @PreAuthorize("@authorizationService.peutAccederRapport(#p0, authentication)")
    public ResponseEntity<Rapport> obtenirRapportParId(@PathVariable Long id) {
        return ResponseEntity.ok(rapportService.obtenirRapportParId(id));
    }

    @GetMapping("/type/{type}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Rapport>> obtenirRapportsParType(@PathVariable TypeRapport type) {
        return ResponseEntity.ok(rapportService.obtenirRapportsParType(type));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Rapport> modifierRapport(@PathVariable Long id, @RequestBody Rapport rapport) {
        return ResponseEntity.ok(rapportService.modifierRapport(id, rapport));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> supprimerRapport(@PathVariable Long id) {
        rapportService.supprimerRapport(id);
        return ResponseEntity.noContent().build();
    }
}
