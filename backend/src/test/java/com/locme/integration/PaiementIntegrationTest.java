package com.locme.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.locme.auth.Role;
import com.locme.auth.User;
import com.locme.auth.UserRepository;
import com.locme.auth.dto.AuthResponse;
import com.locme.auth.dto.LoginRequest;
import com.locme.common.ApiResponse;
import com.locme.paiement.MethodePaiement;
import com.locme.paiement.Paiement;
import com.locme.paiement.PaiementRepository;
import com.locme.paiement.StatutPaiement;
import com.locme.reservation.Reservation;
import com.locme.reservation.ReservationRepository;
import com.locme.reservation.StatutReservation;
import com.locme.societe.Societe;
import com.locme.societe.SocieteRepository;
import com.locme.voiture.Voiture;
import com.locme.voiture.VoitureRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Paiement API endpoints.
 * Tests payment checkout, processing, and failure scenarios with database persistence.
 */
public class PaiementIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private PaiementRepository paiementRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private VoitureRepository voitureRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SocieteRepository societeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreatePaymentCheckout_Success() {
        // Given - create a client user and a confirmed reservation
        User clientUser = createClientUser();
        String clientToken = getAuthToken(clientUser);
        
        User societeUser = createSocieteUser();
        Societe societe = createSociete(societeUser);
        Voiture voiture = createVoiture("Toyota", "Camry", new BigDecimal("50.00"), societe);
        voitureRepository.save(voiture);
        
        Reservation reservation = createReservation(voiture, clientUser);
        reservation.setStatut(StatutReservation.CONFIRMEE);
        reservationRepository.save(reservation);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("reservationId", reservation.getId());
        requestBody.put("methodePaiement", "CARTE_CREDIT");

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, createAuthHeaders(clientToken));

        // When
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                getApiUrl("/api/payments/checkout"),
                HttpMethod.POST,
                request,
                ApiResponse.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());

        // Verify payment was saved in database
        List<Paiement> savedPayments = paiementRepository.findAll();
        assertEquals(1, savedPayments.size());
        
        Paiement savedPayment = savedPayments.get(0);
        assertEquals(reservation.getId(), savedPayment.getReservation().getId());
        assertEquals(new BigDecimal("100.00"), savedPayment.getMontant());
        assertEquals(MethodePaiement.CARTE_CREDIT, savedPayment.getMethodePaiement());
        assertEquals(StatutPaiement.EN_ATTENTE, savedPayment.getStatut());
    }

    @Test
    void testCreatePaymentCheckout_Unauthorized() {
        // Given
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("reservationId", 1L);
        requestBody.put("methodePaiement", "CARTE_CREDIT");

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, createHeaders());

        // When
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                getApiUrl("/api/payments/checkout"),
                HttpMethod.POST,
                request,
                ApiResponse.class
        );

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testCreatePaymentCheckout_SocieteRole_Forbidden() {
        // Given - create a societe user
        User societeUser = createSocieteUser();
        String societeToken = getAuthToken(societeUser);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("reservationId", 1L);
        requestBody.put("methodePaiement", "CARTE_CREDIT");

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, createAuthHeaders(societeToken));

        // When
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                getApiUrl("/api/payments/checkout"),
                HttpMethod.POST,
                request,
                ApiResponse.class
        );

        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void testProcessPayment_Success() {
        // Given - create a payment
        User clientUser = createClientUser();
        String clientToken = getAuthToken(clientUser);
        
        User societeUser = createSocieteUser();
        Societe societe = createSociete(societeUser);
        Voiture voiture = createVoiture("Toyota", "Camry", new BigDecimal("50.00"), societe);
        voitureRepository.save(voiture);
        
        Reservation reservation = createReservation(voiture, clientUser);
        reservation.setStatut(StatutReservation.CONFIRMEE);
        reservationRepository.save(reservation);
        
        Paiement paiement = createPayment(reservation);
        paiementRepository.save(paiement);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("paymentIntentId", "pi_test_123456789");
        requestBody.put("transactionId", "txn_123456789");

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, createAuthHeaders(clientToken));

        // When
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                getApiUrl("/api/payments/" + paiement.getId() + "/process"),
                HttpMethod.POST,
                request,
                ApiResponse.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());

        // Verify payment was updated in database
        Optional<Paiement> updatedPayment = paiementRepository.findById(paiement.getId());
        assertTrue(updatedPayment.isPresent());
        assertEquals(StatutPaiement.PAYE, updatedPayment.get().getStatut());
        assertEquals("pi_test_123456789", updatedPayment.get().getStripePaymentIntentId());
        assertEquals("txn_123456789", updatedPayment.get().getTransactionId());
        assertNotNull(updatedPayment.get().getDatePaiement());

        // Verify reservation status was updated to PAID
        Optional<Reservation> updatedReservation = reservationRepository.findById(reservation.getId());
        assertTrue(updatedReservation.isPresent());
        assertEquals(StatutReservation.CONFIRMEE, updatedReservation.get().getStatut());
    }

    @Test
    void testProcessPaymentFailure_Success() {
        // Given - create a payment
        User clientUser = createClientUser();
        String clientToken = getAuthToken(clientUser);
        
        User societeUser = createSocieteUser();
        Societe societe = createSociete(societeUser);
        Voiture voiture = createVoiture("Toyota", "Camry", new BigDecimal("50.00"), societe);
        voitureRepository.save(voiture);
        
        Reservation reservation = createReservation(voiture, clientUser);
        reservation.setStatut(StatutReservation.CONFIRMEE);
        reservationRepository.save(reservation);
        
        Paiement paiement = createPayment(reservation);
        paiementRepository.save(paiement);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("paymentIntentId", "pi_test_123456789");
        requestBody.put("errorMessage", "Card declined");

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, createAuthHeaders(clientToken));

        // When
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                getApiUrl("/api/payments/" + paiement.getId() + "/failure"),
                HttpMethod.POST,
                request,
                ApiResponse.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());

        // Verify payment was updated in database
        Optional<Paiement> updatedPayment = paiementRepository.findById(paiement.getId());
        assertTrue(updatedPayment.isPresent());
        assertEquals(StatutPaiement.ECHEC, updatedPayment.get().getStatut());
        assertEquals("pi_test_123456789", updatedPayment.get().getStripePaymentIntentId());
    }

    @Test
    void testProcessPayment_NotFound() {
        // Given
        User clientUser = createClientUser();
        String clientToken = getAuthToken(clientUser);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("paymentIntentId", "pi_test_123456789");
        requestBody.put("transactionId", "txn_123456789");

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, createAuthHeaders(clientToken));

        // When
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                getApiUrl("/api/payments/999/process"),
                HttpMethod.POST,
                request,
                ApiResponse.class
        );

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
    }

    @Test
    void testGetPaymentById_Success() {
        // Given - create a payment
        User clientUser = createClientUser();
        String clientToken = getAuthToken(clientUser);
        
        User societeUser = createSocieteUser();
        Societe societe = createSociete(societeUser);
        Voiture voiture = createVoiture("Toyota", "Camry", new BigDecimal("50.00"), societe);
        voitureRepository.save(voiture);
        
        Reservation reservation = createReservation(voiture, clientUser);
        reservation.setStatut(StatutReservation.CONFIRMEE);
        reservationRepository.save(reservation);
        
        Paiement paiement = createPayment(reservation);
        paiementRepository.save(paiement);

        HttpEntity<Void> request = new HttpEntity<>(createAuthHeaders(clientToken));

        // When
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                getApiUrl("/api/payments/" + paiement.getId()),
                HttpMethod.GET,
                request,
                ApiResponse.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());

        Paiement responsePayment = objectMapper.convertValue(response.getBody().getData(), Paiement.class);
        assertEquals(paiement.getId(), responsePayment.getId());
        assertEquals(reservation.getId(), responsePayment.getReservation().getId());
        assertEquals(new BigDecimal("100.00"), responsePayment.getMontant());
        assertEquals(MethodePaiement.CARTE_CREDIT, responsePayment.getMethodePaiement());
        assertEquals(StatutPaiement.EN_ATTENTE, responsePayment.getStatut());
    }

    @Test
    void testGetPaymentById_Unauthorized() {
        // Given
        HttpEntity<Void> request = new HttpEntity<>(createHeaders());

        // When
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                getApiUrl("/api/payments/1"),
                HttpMethod.GET,
                request,
                ApiResponse.class
        );

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testRefundPayment_Success() {
        // Given - create a paid payment
        User clientUser = createClientUser();
        User adminUser = createAdminUser();
        String adminToken = getAuthToken(adminUser);
        
        User societeUser = createSocieteUser();
        Societe societe = createSociete(societeUser);
        Voiture voiture = createVoiture("Toyota", "Camry", new BigDecimal("50.00"), societe);
        voitureRepository.save(voiture);
        
        Reservation reservation = createReservation(voiture, clientUser);
        reservation.setStatut(StatutReservation.CONFIRMEE);
        reservationRepository.save(reservation);
        
        Paiement paiement = createPayment(reservation);
        paiement.setStatut(StatutPaiement.PAYE);
        paiementRepository.save(paiement);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("amount", new BigDecimal("50.00"));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, createAuthHeaders(adminToken));

        // When
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                getApiUrl("/api/payments/" + paiement.getId() + "/refund"),
                HttpMethod.POST,
                request,
                ApiResponse.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());

        // Verify payment status was updated in database
        Optional<Paiement> updatedPayment = paiementRepository.findById(paiement.getId());
        assertTrue(updatedPayment.isPresent());
        assertEquals(StatutPaiement.PARTIELLEMENT_REMBOURSE, updatedPayment.get().getStatut());
    }

    @Test
    void testRefundPayment_NonAdmin_Forbidden() {
        // Given - create a paid payment
        User clientUser = createClientUser();
        String clientToken = getAuthToken(clientUser);
        
        User societeUser = createSocieteUser();
        Societe societe = createSociete(societeUser);
        Voiture voiture = createVoiture("Toyota", "Camry", new BigDecimal("50.00"), societe);
        voitureRepository.save(voiture);
        
        Reservation reservation = createReservation(voiture, clientUser);
        reservation.setStatut(StatutReservation.CONFIRMEE);
        reservationRepository.save(reservation);
        
        Paiement paiement = createPayment(reservation);
        paiement.setStatut(StatutPaiement.PAYE);
        paiementRepository.save(paiement);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("amount", new BigDecimal("50.00"));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, createAuthHeaders(clientToken));

        // When
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                getApiUrl("/api/payments/" + paiement.getId() + "/refund"),
                HttpMethod.POST,
                request,
                ApiResponse.class
        );

        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    // Helper methods
    private User createClientUser() {
        User user = new User();
        user.setNom("Client User");
        user.setEmail("client@example.com");
        user.setMotDePasse(passwordEncoder.encode("password123"));
        user.setRole(Role.CLIENT);
        return userRepository.save(user);
    }

    private User createSocieteUser() {
        User user = new User();
        user.setNom("Societe User");
        user.setEmail("societe@example.com");
        user.setMotDePasse(passwordEncoder.encode("password123"));
        user.setRole(Role.SOCIETE);
        return userRepository.save(user);
    }

    private User createAdminUser() {
        User user = new User();
        user.setNom("Admin User");
        user.setEmail("admin@example.com");
        user.setMotDePasse(passwordEncoder.encode("password123"));
        user.setRole(Role.ADMIN);
        return userRepository.save(user);
    }

    private Societe createSociete(User user) {
        Societe societe = new Societe();
        societe.setNom("Test Societe");
        societe.setAdresse("123 Test Street");
        societe.setEmail("societe@test.com");
        societe.setUser(user);
        return societeRepository.save(societe);
    }

    private Voiture createVoiture(String marque, String modele, BigDecimal prixParJour, Societe societe) {
        Voiture voiture = new Voiture();
        voiture.setMarque(marque);
        voiture.setModele(modele);
        voiture.setPrixParJour(prixParJour);
        voiture.setSociete(societe);
        voiture.setDisponible(true);
        return voiture;
    }

    private Reservation createReservation(Voiture voiture, User user) {
        Reservation reservation = new Reservation();
        reservation.setVoiture(voiture);
        reservation.setUser(user);
        reservation.setDateDebut(LocalDate.now().plusDays(1));
        reservation.setDateFin(LocalDate.now().plusDays(3));
        reservation.setMontant(new BigDecimal("100.00"));
        reservation.setStatut(StatutReservation.EN_ATTENTE);
        return reservation;
    }

    private Paiement createPayment(Reservation reservation) {
        Paiement paiement = new Paiement();
        paiement.setReservation(reservation);
        paiement.setMontant(reservation.getMontant());
        paiement.setMethodePaiement(MethodePaiement.CARTE_CREDIT);
        paiement.setStatut(StatutPaiement.EN_ATTENTE);
        return paiement;
    }

    private String getAuthToken(User user) {
        LoginRequest loginRequest = new LoginRequest(user.getEmail(), "password123");
        HttpEntity<LoginRequest> request = new HttpEntity<>(loginRequest, createHeaders());
        
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                getApiUrl("/api/auth/login"),
                HttpMethod.POST,
                request,
                ApiResponse.class
        );
        
        AuthResponse authResponse = objectMapper.convertValue(response.getBody().getData(), AuthResponse.class);
        return authResponse.getToken();
    }
}
