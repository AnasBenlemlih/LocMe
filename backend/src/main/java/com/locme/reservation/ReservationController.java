package com.locme.reservation;

import com.locme.auth.AuthService;
import com.locme.auth.User;
import com.locme.common.ApiResponse;
import com.locme.common.exceptions.ResourceNotFoundException;
import com.locme.reservation.dto.ReservationDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@CrossOrigin(origins = "*")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private AuthService authService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ReservationDto>>> getAllReservations() {
        List<ReservationDto> reservations = reservationService.getAllReservations();
        return ResponseEntity.ok(ApiResponse.success(reservations));
    }

    @GetMapping("/my-reservations")
    public ResponseEntity<ApiResponse<List<ReservationDto>>> getMyReservations() {
        try {
            User currentUser = authService.getCurrentUser();
            List<ReservationDto> reservations = reservationService.getReservationsByUser(currentUser);
            return ResponseEntity.ok(ApiResponse.success(reservations));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/societe/{id}")
    @PreAuthorize("hasRole('SOCIETE') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ReservationDto>>> getReservationsBySociete(@PathVariable Long id) {
        try {
            User currentUser = authService.getCurrentUser();
            // Vérifier que l'utilisateur peut accéder aux réservations de cette société
            if (!currentUser.getId().equals(id) && !currentUser.getRole().name().equals("ADMIN")) {
                return ResponseEntity.status(403).body(ApiResponse.error("Accès non autorisé"));
            }
            List<ReservationDto> reservations = reservationService.getReservationsBySociete(currentUser);
            return ResponseEntity.ok(ApiResponse.success(reservations));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReservationDto>> getReservationById(@PathVariable Long id) {
        try {
            ReservationDto reservation = reservationService.getReservationById(id);
            return ResponseEntity.ok(ApiResponse.success(reservation));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(ApiResponse.error("Réservation non trouvée"));
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('CLIENT') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ReservationDto>> createReservation(@Valid @RequestBody ReservationDto reservationDto) {
        try {
            User currentUser = authService.getCurrentUser();
            ReservationDto createdReservation = reservationService.createReservation(reservationDto, currentUser);
            return ResponseEntity.ok(ApiResponse.success("Réservation créée avec succès", createdReservation));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('SOCIETE') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ReservationDto>> updateReservationStatus(
            @PathVariable Long id, 
            @RequestParam StatutReservation status) {
        try {
            User currentUser = authService.getCurrentUser();
            ReservationDto updatedReservation = reservationService.updateReservationStatus(id, status, currentUser);
            return ResponseEntity.ok(ApiResponse.success("Statut de réservation mis à jour", updatedReservation));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(ApiResponse.error("Réservation non trouvée"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasRole('SOCIETE') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ReservationDto>> confirmReservation(@PathVariable Long id) {
        try {
            User currentUser = authService.getCurrentUser();
            ReservationDto updatedReservation = reservationService.updateReservationStatus(id, StatutReservation.CONFIRMEE, currentUser);
            return ResponseEntity.ok(ApiResponse.success("Réservation confirmée avec succès", updatedReservation));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(ApiResponse.error("Réservation non trouvée"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('SOCIETE') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ReservationDto>> cancelReservation(@PathVariable Long id) {
        try {
            User currentUser = authService.getCurrentUser();
            ReservationDto updatedReservation = reservationService.updateReservationStatus(id, StatutReservation.ANNULEE, currentUser);
            return ResponseEntity.ok(ApiResponse.success("Réservation annulée avec succès", updatedReservation));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(ApiResponse.error("Réservation non trouvée"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasRole('SOCIETE') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ReservationDto>> completeReservation(@PathVariable Long id) {
        try {
            User currentUser = authService.getCurrentUser();
            ReservationDto updatedReservation = reservationService.updateReservationStatus(id, StatutReservation.TERMINEE, currentUser);
            return ResponseEntity.ok(ApiResponse.success("Réservation marquée comme terminée", updatedReservation));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(ApiResponse.error("Réservation non trouvée"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReservation(@PathVariable Long id) {
        try {
            User currentUser = authService.getCurrentUser();
            reservationService.deleteReservation(id, currentUser);
            return ResponseEntity.ok(ApiResponse.success("Réservation supprimée avec succès", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(ApiResponse.error("Réservation non trouvée"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
