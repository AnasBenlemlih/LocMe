package com.locme.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.locme.auth.Role;
import com.locme.auth.User;
import com.locme.auth.UserRepository;
import com.locme.auth.dto.AuthResponse;
import com.locme.auth.dto.LoginRequest;
import com.locme.common.ApiResponse;
import com.locme.reservation.Reservation;
import com.locme.reservation.ReservationRepository;
import com.locme.reservation.StatutReservation;
import com.locme.reservation.dto.ReservationDto;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Reservation API endpoints.
 * Tests reservation creation, status updates, and database persistence.
 */
public class ReservationIntegrationTest extends BaseIntegrationTest {

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
    void testCreateReservation_Success() {
        // Given - create a client user and a voiture
        User clientUser = createClientUser();
        String clientToken = getAuthToken(clientUser);
        
        User societeUser = createSocieteUser();
        Societe societe = createSociete(societeUser);
        Voiture voiture = createVoiture("Toyota", "Camry", new BigDecimal("50.00"), societe);
        voitureRepository.save(voiture);

        ReservationDto reservationDto = new ReservationDto();
        reservationDto.setVoitureId(voiture.getId());
        reservationDto.setDateDebut(LocalDate.now().plusDays(1));
        reservationDto.setDateFin(LocalDate.now().plusDays(3));
        reservationDto.setMontant(new BigDecimal("100.00"));
        reservationDto.setCommentaires("Test reservation");
        reservationDto.setLieuPrise("Airport");
        reservationDto.setLieuRetour("Airport");

        HttpEntity<ReservationDto> request = new HttpEntity<>(reservationDto, createAuthHeaders(clientToken));

        // When
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                getApiUrl("/api/reservations"),
                HttpMethod.POST,
                request,
                ApiResponse.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());

        // Verify reservation was saved in database
        List<Reservation> savedReservations = reservationRepository.findAll();
        assertEquals(1, savedReservations.size());
        
        Reservation savedReservation = savedReservations.get(0);
        assertEquals(voiture.getId(), savedReservation.getVoiture().getId());
        assertEquals(clientUser.getId(), savedReservation.getUser().getId());
        assertEquals(LocalDate.now().plusDays(1), savedReservation.getDateDebut());
        assertEquals(LocalDate.now().plusDays(3), savedReservation.getDateFin());
        assertEquals(new BigDecimal("100.00"), savedReservation.getMontant());
        assertEquals("Test reservation", savedReservation.getCommentaires());
        assertEquals("Airport", savedReservation.getLieuPrise());
        assertEquals("Airport", savedReservation.getLieuRetour());
        assertEquals(StatutReservation.EN_ATTENTE, savedReservation.getStatut());
    }

    @Test
    void testCreateReservation_Unauthorized() {
        // Given
        ReservationDto reservationDto = new ReservationDto();
        reservationDto.setVoitureId(1L);
        reservationDto.setDateDebut(LocalDate.now().plusDays(1));
        reservationDto.setDateFin(LocalDate.now().plusDays(3));
        reservationDto.setMontant(new BigDecimal("100.00"));

        HttpEntity<ReservationDto> request = new HttpEntity<>(reservationDto, createHeaders());

        // When
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                getApiUrl("/api/reservations"),
                HttpMethod.POST,
                request,
                ApiResponse.class
        );

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testCreateReservation_SocieteRole_Forbidden() {
        // Given - create a societe user
        User societeUser = createSocieteUser();
        String societeToken = getAuthToken(societeUser);

        ReservationDto reservationDto = new ReservationDto();
        reservationDto.setVoitureId(1L);
        reservationDto.setDateDebut(LocalDate.now().plusDays(1));
        reservationDto.setDateFin(LocalDate.now().plusDays(3));
        reservationDto.setMontant(new BigDecimal("100.00"));

        HttpEntity<ReservationDto> request = new HttpEntity<>(reservationDto, createAuthHeaders(societeToken));

        // When
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                getApiUrl("/api/reservations"),
                HttpMethod.POST,
                request,
                ApiResponse.class
        );

        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void testConfirmReservation_Success() {
        // Given - create a reservation
        User clientUser = createClientUser();
        User societeUser = createSocieteUser();
        String societeToken = getAuthToken(societeUser);
        
        Societe societe = createSociete(societeUser);
        Voiture voiture = createVoiture("Toyota", "Camry", new BigDecimal("50.00"), societe);
        voitureRepository.save(voiture);
        
        Reservation reservation = createReservation(voiture, clientUser);
        reservationRepository.save(reservation);

        HttpEntity<Void> request = new HttpEntity<>(createAuthHeaders(societeToken));

        // When
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                getApiUrl("/api/reservations/" + reservation.getId() + "/confirm"),
                HttpMethod.PUT,
                request,
                ApiResponse.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());

        // Verify reservation status was updated in database
        Optional<Reservation> updatedReservation = reservationRepository.findById(reservation.getId());
        assertTrue(updatedReservation.isPresent());
        assertEquals(StatutReservation.CONFIRMEE, updatedReservation.get().getStatut());
    }

    @Test
    void testCancelReservation_Success() {
        // Given - create a confirmed reservation
        User clientUser = createClientUser();
        User societeUser = createSocieteUser();
        String societeToken = getAuthToken(societeUser);
        
        Societe societe = createSociete(societeUser);
        Voiture voiture = createVoiture("Toyota", "Camry", new BigDecimal("50.00"), societe);
        voitureRepository.save(voiture);
        
        Reservation reservation = createReservation(voiture, clientUser);
        reservation.setStatut(StatutReservation.CONFIRMEE);
        reservationRepository.save(reservation);

        HttpEntity<Void> request = new HttpEntity<>(createAuthHeaders(societeToken));

        // When
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                getApiUrl("/api/reservations/" + reservation.getId() + "/cancel"),
                HttpMethod.PUT,
                request,
                ApiResponse.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());

        // Verify reservation status was updated in database
        Optional<Reservation> updatedReservation = reservationRepository.findById(reservation.getId());
        assertTrue(updatedReservation.isPresent());
        assertEquals(StatutReservation.ANNULEE, updatedReservation.get().getStatut());
    }

    @Test
    void testCompleteReservation_Success() {
        // Given - create a confirmed reservation
        User clientUser = createClientUser();
        User societeUser = createSocieteUser();
        String societeToken = getAuthToken(societeUser);
        
        Societe societe = createSociete(societeUser);
        Voiture voiture = createVoiture("Toyota", "Camry", new BigDecimal("50.00"), societe);
        voitureRepository.save(voiture);
        
        Reservation reservation = createReservation(voiture, clientUser);
        reservation.setStatut(StatutReservation.EN_COURS);
        reservationRepository.save(reservation);

        HttpEntity<Void> request = new HttpEntity<>(createAuthHeaders(societeToken));

        // When
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                getApiUrl("/api/reservations/" + reservation.getId() + "/complete"),
                HttpMethod.PUT,
                request,
                ApiResponse.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());

        // Verify reservation status was updated in database
        Optional<Reservation> updatedReservation = reservationRepository.findById(reservation.getId());
        assertTrue(updatedReservation.isPresent());
        assertEquals(StatutReservation.TERMINEE, updatedReservation.get().getStatut());
    }

    @Test
    void testGetMyReservations_Success() {
        // Given - create a client user with reservations
        User clientUser = createClientUser();
        String clientToken = getAuthToken(clientUser);
        
        User societeUser = createSocieteUser();
        Societe societe = createSociete(societeUser);
        Voiture voiture1 = createVoiture("Toyota", "Camry", new BigDecimal("50.00"), societe);
        Voiture voiture2 = createVoiture("Honda", "Civic", new BigDecimal("45.00"), societe);
        voitureRepository.save(voiture1);
        voitureRepository.save(voiture2);
        
        Reservation reservation1 = createReservation(voiture1, clientUser);
        Reservation reservation2 = createReservation(voiture2, clientUser);
        reservationRepository.save(reservation1);
        reservationRepository.save(reservation2);

        HttpEntity<Void> request = new HttpEntity<>(createAuthHeaders(clientToken));

        // When
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                getApiUrl("/api/reservations/my-reservations"),
                HttpMethod.GET,
                request,
                ApiResponse.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());

        List<ReservationDto> reservations = objectMapper.convertValue(
                response.getBody().getData(), 
                new TypeReference<List<ReservationDto>>() {}
        );
        assertEquals(2, reservations.size());
        
        // Verify the reservations belong to the client
        assertTrue(reservations.stream().allMatch(r -> r.getUserId().equals(clientUser.getId())));
    }

    @Test
    void testGetReservationById_Success() {
        // Given - create a reservation
        User clientUser = createClientUser();
        User societeUser = createSocieteUser();
        
        Societe societe = createSociete(societeUser);
        Voiture voiture = createVoiture("Toyota", "Camry", new BigDecimal("50.00"), societe);
        voitureRepository.save(voiture);
        
        Reservation reservation = createReservation(voiture, clientUser);
        reservationRepository.save(reservation);

        HttpEntity<Void> request = new HttpEntity<>(createHeaders());

        // When
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                getApiUrl("/api/reservations/" + reservation.getId()),
                HttpMethod.GET,
                request,
                ApiResponse.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());

        ReservationDto reservationDto = objectMapper.convertValue(response.getBody().getData(), ReservationDto.class);
        assertEquals(reservation.getId(), reservationDto.getId());
        assertEquals(voiture.getId(), reservationDto.getVoitureId());
        assertEquals(clientUser.getId(), reservationDto.getUserId());
        assertEquals(StatutReservation.EN_ATTENTE, reservationDto.getStatut());
    }

    @Test
    void testGetReservationById_NotFound() {
        // Given
        HttpEntity<Void> request = new HttpEntity<>(createHeaders());

        // When
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                getApiUrl("/api/reservations/999"),
                HttpMethod.GET,
                request,
                ApiResponse.class
        );

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
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
