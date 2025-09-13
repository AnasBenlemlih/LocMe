package com.locme.voiture;

import com.locme.auth.AuthService;
import com.locme.auth.User;
import com.locme.common.ApiResponse;
import com.locme.common.exceptions.ResourceNotFoundException;
import com.locme.voiture.dto.VoitureDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/voitures")
@CrossOrigin(origins = "*")
@Tag(name = "Voitures", description = "API de gestion des voitures")
public class VoitureController {

    @Autowired
    private VoitureService voitureService;

    @Autowired
    private AuthService authService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<VoitureDto>>> getAllVoitures() {
        List<VoitureDto> voitures = voitureService.getAllVoitures();
        return ResponseEntity.ok(ApiResponse.success(voitures));
    }

    @GetMapping("/disponibles")
    @Operation(summary = "Voitures disponibles", description = "Récupérer la liste des voitures disponibles avec filtres optionnels")
    public ResponseEntity<ApiResponse<List<VoitureDto>>> getAvailableVoitures(
            @Parameter(description = "Date de début de location") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @Parameter(description = "Date de fin de location") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
            @Parameter(description = "Marque de la voiture") @RequestParam(required = false) String marque,
            @Parameter(description = "Prix minimum par jour") @RequestParam(required = false) BigDecimal prixMin,
            @Parameter(description = "Prix maximum par jour") @RequestParam(required = false) BigDecimal prixMax) {
        
        List<VoitureDto> voitures;
        
        if (dateDebut != null && dateFin != null) {
            voitures = voitureService.getAvailableVoituresByDate(dateDebut, dateFin);
        } else if (marque != null || prixMin != null || prixMax != null) {
            voitures = voitureService.getAvailableVoituresWithFilters(marque, prixMin, prixMax);
        } else {
            voitures = voitureService.getAvailableVoitures();
        }
        
        return ResponseEntity.ok(ApiResponse.success(voitures));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VoitureDto>> getVoitureById(@PathVariable Long id) {
        try {
            VoitureDto voiture = voitureService.getVoitureById(id);
            return ResponseEntity.ok(ApiResponse.success(voiture));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('SOCIETE') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<VoitureDto>> createVoiture(@Valid @RequestBody VoitureDto voitureDto) {
        try {
            User currentUser = authService.getCurrentUser();
            VoitureDto createdVoiture = voitureService.createVoiture(voitureDto, currentUser);
            return ResponseEntity.ok(ApiResponse.success("Voiture créée avec succès", createdVoiture));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SOCIETE') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<VoitureDto>> updateVoiture(@PathVariable Long id, @Valid @RequestBody VoitureDto voitureDto) {
        try {
            User currentUser = authService.getCurrentUser();
            VoitureDto updatedVoiture = voitureService.updateVoiture(id, voitureDto, currentUser);
            return ResponseEntity.ok(ApiResponse.success("Voiture mise à jour avec succès", updatedVoiture));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SOCIETE') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteVoiture(@PathVariable Long id) {
        try {
            User currentUser = authService.getCurrentUser();
            voitureService.deleteVoiture(id, currentUser);
            return ResponseEntity.ok(ApiResponse.success("Voiture supprimée avec succès", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
