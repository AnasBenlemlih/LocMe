package com.locme.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.locme.auth.Role;
import com.locme.auth.User;
import com.locme.auth.UserRepository;
import com.locme.auth.dto.AuthResponse;
import com.locme.auth.dto.LoginRequest;
import com.locme.auth.dto.RegisterRequest;
import com.locme.common.ApiResponse;
import com.locme.societe.Societe;
import com.locme.societe.SocieteRepository;
import com.locme.voiture.Voiture;
import com.locme.voiture.VoitureRepository;
import com.locme.voiture.TypeCarburant;
import com.locme.voiture.TypeTransmission;
import com.locme.voiture.dto.VoitureDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Voiture API endpoints.
 * Tests voiture creation, fetching, and filtering with database persistence.
 */
public class VoitureIntegrationTest extends BaseIntegrationTest {

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
    void testCreateVoiture_Success() {
        // Given - create a societe user first
        User societeUser = createSocieteUser();
        String token = getAuthToken(societeUser);

        VoitureDto voitureDto = new VoitureDto();
        voitureDto.setMarque("Toyota");
        voitureDto.setModele("Camry");
        voitureDto.setPrixParJour(new BigDecimal("50.00"));
        voitureDto.setAnnee(2022);
        voitureDto.setKilometrage(25000L);
        voitureDto.setCarburant(TypeCarburant.ESSENCE);
        voitureDto.setTransmission(TypeTransmission.AUTOMATIQUE);
        voitureDto.setNombrePlaces(5);
        voitureDto.setDescription("Voiture confortable et économique");
        voitureDto.setImageUrl("https://example.com/camry.jpg");

        HttpEntity<VoitureDto> request = new HttpEntity<>(voitureDto, createAuthHeaders(token));

        // When
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                getApiUrl("/api/voitures"),
                HttpMethod.POST,
                request,
                ApiResponse.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());

        // Verify voiture was saved in database
        List<Voiture> savedVoitures = voitureRepository.findAll();
        assertEquals(1, savedVoitures.size());
        
        Voiture savedVoiture = savedVoitures.get(0);
        assertEquals("Toyota", savedVoiture.getMarque());
        assertEquals("Camry", savedVoiture.getModele());
        assertEquals(new BigDecimal("50.00"), savedVoiture.getPrixParJour());
        assertEquals(2022, savedVoiture.getAnnee());
        assertEquals(25000L, savedVoiture.getKilometrage());
        assertEquals(TypeCarburant.ESSENCE, savedVoiture.getCarburant());
        assertEquals(TypeTransmission.AUTOMATIQUE, savedVoiture.getTransmission());
        assertEquals(5, savedVoiture.getNombrePlaces());
        assertEquals("Voiture confortable et économique", savedVoiture.getDescription());
        assertEquals("https://example.com/camry.jpg", savedVoiture.getImageUrl());
        assertTrue(savedVoiture.getDisponible());
        assertEquals(societeUser.getId(), savedVoiture.getSociete().getUser().getId());
    }

    @Test
    void testCreateVoiture_Unauthorized() {
        // Given
        VoitureDto voitureDto = new VoitureDto();
        voitureDto.setMarque("Toyota");
        voitureDto.setModele("Camry");
        voitureDto.setPrixParJour(new BigDecimal("50.00"));

        HttpEntity<VoitureDto> request = new HttpEntity<>(voitureDto, createHeaders());

        // When
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                getApiUrl("/api/voitures"),
                HttpMethod.POST,
                request,
                ApiResponse.class
        );

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testCreateVoiture_ClientRole_Forbidden() {
        // Given - create a client user
        User clientUser = createClientUser();
        String token = getAuthToken(clientUser);

        VoitureDto voitureDto = new VoitureDto();
        voitureDto.setMarque("Toyota");
        voitureDto.setModele("Camry");
        voitureDto.setPrixParJour(new BigDecimal("50.00"));

        HttpEntity<VoitureDto> request = new HttpEntity<>(voitureDto, createAuthHeaders(token));

        // When
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                getApiUrl("/api/voitures"),
                HttpMethod.POST,
                request,
                ApiResponse.class
        );

        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void testGetAllVoitures_Success() {
        // Given - create some voitures
        User societeUser = createSocieteUser();
        Societe societe = createSociete(societeUser);
        
        Voiture voiture1 = createVoiture("Toyota", "Camry", new BigDecimal("50.00"), societe);
        Voiture voiture2 = createVoiture("Honda", "Civic", new BigDecimal("45.00"), societe);
        voitureRepository.save(voiture1);
        voitureRepository.save(voiture2);

        HttpEntity<Void> request = new HttpEntity<>(createHeaders());

        // When
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                getApiUrl("/api/voitures"),
                HttpMethod.GET,
                request,
                ApiResponse.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());

        List<VoitureDto> voitures = objectMapper.convertValue(
                response.getBody().getData(), 
                new TypeReference<List<VoitureDto>>() {}
        );
        assertEquals(2, voitures.size());
        
        // Verify the voitures are returned correctly
        assertTrue(voitures.stream().anyMatch(v -> "Toyota".equals(v.getMarque()) && "Camry".equals(v.getModele())));
        assertTrue(voitures.stream().anyMatch(v -> "Honda".equals(v.getMarque()) && "Civic".equals(v.getModele())));
    }

    @Test
    void testGetAvailableVoitures_Success() {
        // Given - create some voitures with different availability
        User societeUser = createSocieteUser();
        Societe societe = createSociete(societeUser);
        
        Voiture availableVoiture = createVoiture("Toyota", "Camry", new BigDecimal("50.00"), societe);
        availableVoiture.setDisponible(true);
        
        Voiture unavailableVoiture = createVoiture("Honda", "Civic", new BigDecimal("45.00"), societe);
        unavailableVoiture.setDisponible(false);
        
        voitureRepository.save(availableVoiture);
        voitureRepository.save(unavailableVoiture);

        HttpEntity<Void> request = new HttpEntity<>(createHeaders());

        // When
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                getApiUrl("/api/voitures/disponibles"),
                HttpMethod.GET,
                request,
                ApiResponse.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());

        List<VoitureDto> voitures = objectMapper.convertValue(
                response.getBody().getData(), 
                new TypeReference<List<VoitureDto>>() {}
        );
        assertEquals(1, voitures.size());
        assertEquals("Toyota", voitures.get(0).getMarque());
        assertTrue(voitures.get(0).getDisponible());
    }

    @Test
    void testGetAvailableVoitures_WithFilters() {
        // Given - create voitures with different brands and prices
        User societeUser = createSocieteUser();
        Societe societe = createSociete(societeUser);
        
        Voiture toyota = createVoiture("Toyota", "Camry", new BigDecimal("50.00"), societe);
        Voiture honda = createVoiture("Honda", "Civic", new BigDecimal("45.00"), societe);
        Voiture bmw = createVoiture("BMW", "X5", new BigDecimal("100.00"), societe);
        
        voitureRepository.save(toyota);
        voitureRepository.save(honda);
        voitureRepository.save(bmw);

        HttpEntity<Void> request = new HttpEntity<>(createHeaders());

        // When - filter by brand
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                getApiUrl("/api/voitures/disponibles?marque=Toyota"),
                HttpMethod.GET,
                request,
                ApiResponse.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());

        List<VoitureDto> voitures = objectMapper.convertValue(
                response.getBody().getData(), 
                new TypeReference<List<VoitureDto>>() {}
        );
        assertEquals(1, voitures.size());
        assertEquals("Toyota", voitures.get(0).getMarque());
    }

    @Test
    void testGetAvailableVoitures_WithPriceRange() {
        // Given - create voitures with different prices
        User societeUser = createSocieteUser();
        Societe societe = createSociete(societeUser);
        
        Voiture cheap = createVoiture("Toyota", "Camry", new BigDecimal("30.00"), societe);
        Voiture medium = createVoiture("Honda", "Civic", new BigDecimal("50.00"), societe);
        Voiture expensive = createVoiture("BMW", "X5", new BigDecimal("100.00"), societe);
        
        voitureRepository.save(cheap);
        voitureRepository.save(medium);
        voitureRepository.save(expensive);

        HttpEntity<Void> request = new HttpEntity<>(createHeaders());

        // When - filter by price range
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                getApiUrl("/api/voitures/disponibles?prixMin=40&prixMax=60"),
                HttpMethod.GET,
                request,
                ApiResponse.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());

        List<VoitureDto> voitures = objectMapper.convertValue(
                response.getBody().getData(), 
                new TypeReference<List<VoitureDto>>() {}
        );
        assertEquals(1, voitures.size());
        assertEquals("Honda", voitures.get(0).getMarque());
        assertEquals(new BigDecimal("50.00"), voitures.get(0).getPrixParJour());
    }

    @Test
    void testGetVoitureById_Success() {
        // Given - create a voiture
        User societeUser = createSocieteUser();
        Societe societe = createSociete(societeUser);
        Voiture voiture = createVoiture("Toyota", "Camry", new BigDecimal("50.00"), societe);
        voitureRepository.save(voiture);

        HttpEntity<Void> request = new HttpEntity<>(createHeaders());

        // When
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                getApiUrl("/api/voitures/" + voiture.getId()),
                HttpMethod.GET,
                request,
                ApiResponse.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());

        VoitureDto voitureDto = objectMapper.convertValue(response.getBody().getData(), VoitureDto.class);
        assertEquals("Toyota", voitureDto.getMarque());
        assertEquals("Camry", voitureDto.getModele());
        assertEquals(new BigDecimal("50.00"), voitureDto.getPrixParJour());
    }

    @Test
    void testGetVoitureById_NotFound() {
        // Given
        HttpEntity<Void> request = new HttpEntity<>(createHeaders());

        // When
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                getApiUrl("/api/voitures/999"),
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
    private User createSocieteUser() {
        User user = new User();
        user.setNom("Societe User");
        user.setEmail("societe@example.com");
        user.setMotDePasse(passwordEncoder.encode("password123"));
        user.setRole(Role.SOCIETE);
        return userRepository.save(user);
    }

    private User createClientUser() {
        User user = new User();
        user.setNom("Client User");
        user.setEmail("client@example.com");
        user.setMotDePasse(passwordEncoder.encode("password123"));
        user.setRole(Role.CLIENT);
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
