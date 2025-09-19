package com.locme.paiement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.locme.auth.AuthService;
import com.locme.auth.JwtService;
import com.locme.auth.User;
import com.locme.auth.Role;
import com.locme.common.ApiResponse;
import com.locme.common.exceptions.ResourceNotFoundException;
import com.locme.config.TestSecurityConfig;
import com.locme.reservation.Reservation;
import com.locme.reservation.StatutReservation;
import com.locme.societe.Societe;
import com.locme.voiture.Voiture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaiementController.class)
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
class PaiementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaiementService paiementService;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private Paiement testPaiement;
    private Reservation testReservation;
    private User testUser;
    private Voiture testVoiture;
    private Societe testSociete;

    @BeforeEach
    void setUp() {
        testSociete = new Societe();
        testSociete.setId(1L);
        testSociete.setNom("Test Societe");

        testUser = new User();
        testUser.setId(1L);
        testUser.setNom("Test Client");
        testUser.setEmail("client@example.com");
        testUser.setRole(Role.CLIENT);

        testVoiture = new Voiture();
        testVoiture.setId(1L);
        testVoiture.setMarque("Toyota");
        testVoiture.setModele("Camry");
        testVoiture.setPrixParJour(new BigDecimal("50.00"));
        testVoiture.setDisponible(true);
        testVoiture.setSociete(testSociete);

        testReservation = new Reservation();
        testReservation.setId(1L);
        testReservation.setVoiture(testVoiture);
        testReservation.setUser(testUser);
        testReservation.setDateDebut(LocalDate.now().plusDays(1));
        testReservation.setDateFin(LocalDate.now().plusDays(3));
        testReservation.setStatut(StatutReservation.EN_ATTENTE);
        testReservation.setMontant(new BigDecimal("150.00"));

        testPaiement = new Paiement();
        testPaiement.setId(1L);
        testPaiement.setReservation(testReservation);
        testPaiement.setMontant(new BigDecimal("150.00"));
        testPaiement.setStatut(StatutPaiement.EN_ATTENTE);
        testPaiement.setMethodePaiement(MethodePaiement.CARTE_CREDIT);
        testPaiement.setStripePaymentIntentId("pi_test_123");
        testPaiement.setTransactionId("txn_123");
        testPaiement.setCreatedAt(LocalDateTime.now());
        testPaiement.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Test create payment checkout")
    @WithMockUser(username = "client@example.com", roles = {"CLIENT"})
    void testCreatePaymentCheckout() throws Exception {
        // Given
        Map<String, Object> checkoutRequest = new HashMap<>();
        checkoutRequest.put("reservationId", 1L);
        checkoutRequest.put("methodePaiement", "CARTE_CREDIT");

        when(authService.getCurrentUser()).thenReturn(testUser);
        when(paiementService.createPaiement(1L, MethodePaiement.CARTE_CREDIT)).thenReturn(testPaiement);

        // When & Then
        mockMvc.perform(post("/api/payments/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(checkoutRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Paiement créé avec succès"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.montant").value(150.00))
                .andExpect(jsonPath("$.data.statut").value("EN_ATTENTE"));
    }

    @Test
    @DisplayName("Test create payment checkout with invalid data")
    @WithMockUser(username = "client@example.com", roles = {"CLIENT"})
    void testCreatePaymentCheckoutInvalidData() throws Exception {
        // Given
        Map<String, Object> invalidRequest = new HashMap<>();
        invalidRequest.put("reservationId", null); // Invalid: null reservation ID
        invalidRequest.put("methodePaiement", "INVALID_METHOD"); // Invalid: invalid payment method

        // When & Then
        mockMvc.perform(post("/api/payments/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Test create payment checkout with reservation not found")
    @WithMockUser(username = "client@example.com", roles = {"CLIENT"})
    void testCreatePaymentCheckoutReservationNotFound() throws Exception {
        // Given
        Map<String, Object> checkoutRequest = new HashMap<>();
        checkoutRequest.put("reservationId", 999L);
        checkoutRequest.put("methodePaiement", "CARTE_CREDIT");

        when(authService.getCurrentUser()).thenReturn(testUser);
        when(paiementService.createPaiement(999L, MethodePaiement.CARTE_CREDIT))
                .thenThrow(new ResourceNotFoundException("Réservation non trouvée"));

        // When & Then
        mockMvc.perform(post("/api/payments/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(checkoutRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Réservation non trouvée"));
    }

    @Test
    @DisplayName("Test create payment checkout unauthorized")
    void testCreatePaymentCheckoutUnauthorized() throws Exception {
        // Given
        Map<String, Object> checkoutRequest = new HashMap<>();
        checkoutRequest.put("reservationId", 1L);
        checkoutRequest.put("methodePaiement", "CARTE_CREDIT");

        // When & Then
        mockMvc.perform(post("/api/payments/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(checkoutRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Test process payment success")
    @WithMockUser(username = "client@example.com", roles = {"CLIENT"})
    void testProcessPaymentSuccess() throws Exception {
        // Given
        Map<String, Object> paymentRequest = new HashMap<>();
        paymentRequest.put("paymentIntentId", "pi_success_123");
        paymentRequest.put("transactionId", "txn_success_123");

        Paiement paidPaiement = new Paiement();
        paidPaiement.setId(1L);
        paidPaiement.setStatut(StatutPaiement.PAYE);
        paidPaiement.setStripePaymentIntentId("pi_success_123");
        paidPaiement.setTransactionId("txn_success_123");
        paidPaiement.setDatePaiement(LocalDateTime.now());

        when(authService.getCurrentUser()).thenReturn(testUser);
        when(paiementService.processPayment(1L, "pi_success_123", "txn_success_123")).thenReturn(paidPaiement);

        // When & Then
        mockMvc.perform(post("/api/payments/1/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Paiement traité avec succès"))
                .andExpect(jsonPath("$.data.statut").value("PAYE"));
    }

    @Test
    @DisplayName("Test process payment failure")
    @WithMockUser(username = "client@example.com", roles = {"CLIENT"})
    void testProcessPaymentFailure() throws Exception {
        // Given
        Map<String, Object> paymentRequest = new HashMap<>();
        paymentRequest.put("paymentIntentId", "pi_failed_123");
        paymentRequest.put("errorMessage", "Insufficient funds");

        Paiement failedPaiement = new Paiement();
        failedPaiement.setId(1L);
        failedPaiement.setStatut(StatutPaiement.ECHEC);
        failedPaiement.setStripePaymentIntentId("pi_failed_123");

        when(authService.getCurrentUser()).thenReturn(testUser);
        when(paiementService.processPaymentFailure(1L, "pi_failed_123", "Insufficient funds")).thenReturn(failedPaiement);

        // When & Then
        mockMvc.perform(post("/api/payments/1/failure")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Échec du paiement enregistré"))
                .andExpect(jsonPath("$.data.statut").value("ECHEC"));
    }

    @Test
    @DisplayName("Test get payment by ID")
    @WithMockUser(username = "client@example.com", roles = {"CLIENT"})
    void testGetPaymentById() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(testUser);
        when(paiementService.getPaiementById(1L)).thenReturn(testPaiement);

        // When & Then
        mockMvc.perform(get("/api/payments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.montant").value(150.00))
                .andExpect(jsonPath("$.data.statut").value("EN_ATTENTE"));
    }

    @Test
    @DisplayName("Test get payment by ID not found")
    @WithMockUser(username = "client@example.com", roles = {"CLIENT"})
    void testGetPaymentByIdNotFound() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(testUser);
        when(paiementService.getPaiementById(999L))
                .thenThrow(new ResourceNotFoundException("Paiement non trouvé"));

        // When & Then
        mockMvc.perform(get("/api/payments/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Paiement non trouvé"));
    }

    @Test
    @DisplayName("Test refund payment")
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void testRefundPayment() throws Exception {
        // Given
        Map<String, Object> refundRequest = new HashMap<>();
        refundRequest.put("amount", 150.00);

        Paiement refundedPaiement = new Paiement();
        refundedPaiement.setId(1L);
        refundedPaiement.setStatut(StatutPaiement.REMBOURSE);
        refundedPaiement.setMontant(new BigDecimal("150.00"));

        when(authService.getCurrentUser()).thenReturn(testUser);
        when(paiementService.refundPaiement(1L, new BigDecimal("150.00"))).thenReturn(refundedPaiement);

        // When & Then
        mockMvc.perform(post("/api/payments/1/refund")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refundRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Remboursement effectué avec succès"))
                .andExpect(jsonPath("$.data.statut").value("REMBOURSE"));
    }

    @Test
    @DisplayName("Test refund payment not found")
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void testRefundPaymentNotFound() throws Exception {
        // Given
        Map<String, Object> refundRequest = new HashMap<>();
        refundRequest.put("amount", 150.00);

        when(authService.getCurrentUser()).thenReturn(testUser);
        when(paiementService.refundPaiement(999L, new BigDecimal("150.00")))
                .thenThrow(new ResourceNotFoundException("Paiement non trouvé"));

        // When & Then
        mockMvc.perform(post("/api/payments/999/refund")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refundRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Paiement non trouvé"));
    }

    @Test
    @DisplayName("Test refund payment unauthorized")
    @WithMockUser(username = "client@example.com", roles = {"CLIENT"})
    void testRefundPaymentUnauthorized() throws Exception {
        // Given
        Map<String, Object> refundRequest = new HashMap<>();
        refundRequest.put("amount", 150.00);

        // When & Then
        mockMvc.perform(post("/api/payments/1/refund")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refundRequest)))
                .andExpect(status().isForbidden());
    }
}
