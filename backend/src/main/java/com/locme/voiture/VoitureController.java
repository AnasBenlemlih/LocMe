package com.locme.voiture;

import com.locme.auth.AuthService;
import com.locme.auth.User;
import com.locme.common.ApiResponse;
import com.locme.common.exceptions.ResourceNotFoundException;
import com.locme.voiture.dto.VoitureDto;
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
    public ResponseEntity<ApiResponse<List<VoitureDto>>> getAvailableVoitures(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
            @RequestParam(required = false) String marque,
            @RequestParam(required = false) BigDecimal prixMin,
            @RequestParam(required = false) BigDecimal prixMax) {
        
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
