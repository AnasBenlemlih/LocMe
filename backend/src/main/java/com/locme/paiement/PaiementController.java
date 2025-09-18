package com.locme.paiement;

import com.locme.auth.AuthService;
import com.locme.auth.User;
import com.locme.common.ApiResponse;
import com.locme.common.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaiementController {

    @Autowired
    private PaiementService paiementService;

    @Autowired
    private AuthService authService;

    @PostMapping("/checkout")
    @PreAuthorize("hasRole('CLIENT') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Paiement>> createPaymentCheckout(@Valid @RequestBody Map<String, Object> request) {
        try {
            Long reservationId = Long.valueOf(request.get("reservationId").toString());
            MethodePaiement methodePaiement = MethodePaiement.valueOf(request.get("methodePaiement").toString());
            
            Paiement paiement = paiementService.createPaiement(reservationId, methodePaiement);
            return ResponseEntity.ok(ApiResponse.success("Paiement créé avec succès", paiement));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{id}/process")
    @PreAuthorize("hasRole('CLIENT') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Paiement>> processPayment(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            String paymentIntentId = request.get("paymentIntentId").toString();
            String transactionId = request.get("transactionId").toString();
            
            Paiement paiement = paiementService.processPayment(id, paymentIntentId, transactionId);
            return ResponseEntity.ok(ApiResponse.success("Paiement traité avec succès", paiement));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{id}/failure")
    @PreAuthorize("hasRole('CLIENT') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Paiement>> processPaymentFailure(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            String paymentIntentId = request.get("paymentIntentId").toString();
            String errorMessage = request.get("errorMessage").toString();
            
            Paiement paiement = paiementService.processPaymentFailure(id, paymentIntentId, errorMessage);
            return ResponseEntity.ok(ApiResponse.success("Échec du paiement enregistré", paiement));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CLIENT') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Paiement>> getPaymentById(@PathVariable Long id) {
        try {
            Paiement paiement = paiementService.getPaiementById(id);
            return ResponseEntity.ok(ApiResponse.success(paiement));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{id}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Paiement>> refundPayment(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            Paiement paiement = paiementService.refundPaiement(id, amount);
            return ResponseEntity.ok(ApiResponse.success("Remboursement effectué avec succès", paiement));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
