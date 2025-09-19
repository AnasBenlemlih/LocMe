package com.locme.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.locme.auth.Role;
import com.locme.auth.User;
import com.locme.auth.UserRepository;
import com.locme.auth.dto.AuthResponse;
import com.locme.auth.dto.LoginRequest;
import com.locme.common.ApiResponse;
import com.locme.favorite.Favorite;
import com.locme.favorite.FavoriteRepository;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Favorite API endpoints.
 * Tests favorite toggle, fetch favorites, and database persistence.
 */
public class FavoriteIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private FavoriteRepository favoriteRepository;

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
    void testToggleFavorite_AddToFavorites_Success() {
        // Given - create a client user and a voiture
        User clientUser = createClientUser();
        String clientToken = getAuthToken(clientUser);
        
        User societeUser = createSocieteUser();
        Societe societe = createSociete(societeUser);
        Voiture voiture = createVoiture("Toyota", "Camry", new BigDecimal("50.00"), societe);
        voitureRepository.save(voiture);

        HttpEntity<Void> request = new HttpEntity<>(createAuthHeaders(clientToken));

        // When
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                getApiUrl("/api/favorites/toggle/" + voiture.getId()),
                HttpMethod.POST,
                request,
                ApiResponse.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Ajouté aux favoris", response.getBody().getMessage());

        // Verify favorite was saved in database
        List<Favorite> savedFavorites = favoriteRepository.findAll();
        assertEquals(1, savedFavorites.size());
        
        Favorite savedFavorite = savedFavorites.get(0);
        assertEquals(clientUser.getId(), savedFavorite.getUser().getId());
        assertEquals(voiture.getId(), savedFavorite.getVoiture().getId());
        assertNotNull(savedFavorite.getCreatedAt());
    }

    @Test
    void testToggleFavorite_RemoveFromFavorites_Success() {
        // Given - create a client user, voiture, and existing favorite
        User clientUser = createClientUser();
        String clientToken = getAuthToken(clientUser);
        
        User societeUser = createSocieteUser();
        Societe societe = createSociete(societeUser);
        Voiture voiture = createVoiture("Toyota", "Camry", new BigDecimal("50.00"), societe);
        voitureRepository.save(voiture);
        
        Favorite existingFavorite = new Favorite(clientUser, voiture);
        favoriteRepository.save(existingFavorite);

        HttpEntity<Void> request = new HttpEntity<>(createAuthHeaders(clientToken));

        // When - toggle again to remove from favorites
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                getApiUrl("/api/favorites/toggle/" + voiture.getId()),
                HttpMethod.POST,
                request,
                ApiResponse.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Retiré des favoris", response.getBody().getMessage());

        // Verify favorite was removed from database
        List<Favorite> remainingFavorites = favoriteRepository.findAll();
        assertEquals(0, remainingFavorites.size());
        
        // Verify the specific favorite no longer exists
        Optional<Favorite> removedFavorite = favoriteRepository.findByUserAndVoiture(clientUser, voiture);
        assertFalse(removedFavorite.isPresent());
    }

    @Test
    void testToggleFavorite_Unauthorized() {
        // Given
        HttpEntity<Void> request = new HttpEntity<>(createHeaders());

        // When
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                getApiUrl("/api/favorites/toggle/1"),
                HttpMethod.POST,
                request,
                ApiResponse.class
        );

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testToggleFavorite_SocieteRole_Forbidden() {
        // Given - create a societe user
        User societeUser = createSocieteUser();
        String societeToken = getAuthToken(societeUser);

        HttpEntity<Void> request = new HttpEntity<>(createAuthHeaders(societeToken));

        // When
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                getApiUrl("/api/favorites/toggle/1"),
                HttpMethod.POST,
                request,
                ApiResponse.class
        );

        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void testToggleFavorite_VoitureNotFound() {
        // Given - create a client user
        User clientUser = createClientUser();
        String clientToken = getAuthToken(clientUser);

        HttpEntity<Void> request = new HttpEntity<>(createAuthHeaders(clientToken));

        // When
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                getApiUrl("/api/favorites/toggle/999"),
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
    void testGetUserFavorites_Success() {
        // Given - create a client user with multiple favorites
        User clientUser = createClientUser();
        String clientToken = getAuthToken(clientUser);
        
        User societeUser = createSocieteUser();
        Societe societe = createSociete(societeUser);
        
        Voiture voiture1 = createVoiture("Toyota", "Camry", new BigDecimal("50.00"), societe);
        Voiture voiture2 = createVoiture("Honda", "Civic", new BigDecimal("45.00"), societe);
        voitureRepository.save(voiture1);
        voitureRepository.save(voiture2);
        
        Favorite favorite1 = new Favorite(clientUser, voiture1);
        Favorite favorite2 = new Favorite(clientUser, voiture2);
        favoriteRepository.save(favorite1);
        favoriteRepository.save(favorite2);

        HttpEntity<Void> request = new HttpEntity<>(createAuthHeaders(clientToken));

        // When
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                getApiUrl("/api/favorites/user/" + clientUser.getId()),
                HttpMethod.GET,
                request,
                ApiResponse.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());

        List<Favorite> favorites = objectMapper.convertValue(
                response.getBody().getData(), 
                new TypeReference<List<Favorite>>() {}
        );
        assertEquals(2, favorites.size());
        
        // Verify the favorites belong to the client
        assertTrue(favorites.stream().allMatch(f -> f.getUser().getId().equals(clientUser.getId())));
        
        // Verify both voitures are in favorites
        assertTrue(favorites.stream().anyMatch(f -> f.getVoiture().getId().equals(voiture1.getId())));
        assertTrue(favorites.stream().anyMatch(f -> f.getVoiture().getId().equals(voiture2.getId())));
    }

    @Test
    void testGetUserFavorites_Unauthorized() {
        // Given
        HttpEntity<Void> request = new HttpEntity<>(createHeaders());

        // When
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                getApiUrl("/api/favorites/user/1"),
                HttpMethod.GET,
                request,
                ApiResponse.class
        );

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testGetUserFavorites_AccessDenied() {
        // Given - create two different users
        User clientUser1 = createClientUser();
        User clientUser2 = createClientUser();
        clientUser2.setEmail("client2@example.com");
        userRepository.save(clientUser2);
        String clientToken1 = getAuthToken(clientUser1);

        HttpEntity<Void> request = new HttpEntity<>(createAuthHeaders(clientToken1));

        // When - try to access another user's favorites
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                getApiUrl("/api/favorites/user/" + clientUser2.getId()),
                HttpMethod.GET,
                request,
                ApiResponse.class
        );

        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
    }

    @Test
    void testIsFavorite_True() {
        // Given - create a client user and a favorite
        User clientUser = createClientUser();
        String clientToken = getAuthToken(clientUser);
        
        User societeUser = createSocieteUser();
        Societe societe = createSociete(societeUser);
        Voiture voiture = createVoiture("Toyota", "Camry", new BigDecimal("50.00"), societe);
        voitureRepository.save(voiture);
        
        Favorite favorite = new Favorite(clientUser, voiture);
        favoriteRepository.save(favorite);

        HttpEntity<Void> request = new HttpEntity<>(createAuthHeaders(clientToken));

        // When
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                getApiUrl("/api/favorites/check/" + voiture.getId()),
                HttpMethod.GET,
                request,
                ApiResponse.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());

        Boolean isFavorite = objectMapper.convertValue(response.getBody().getData(), Boolean.class);
        assertTrue(isFavorite);
    }

    @Test
    void testIsFavorite_False() {
        // Given - create a client user and a voiture (no favorite)
        User clientUser = createClientUser();
        String clientToken = getAuthToken(clientUser);
        
        User societeUser = createSocieteUser();
        Societe societe = createSociete(societeUser);
        Voiture voiture = createVoiture("Toyota", "Camry", new BigDecimal("50.00"), societe);
        voitureRepository.save(voiture);

        HttpEntity<Void> request = new HttpEntity<>(createAuthHeaders(clientToken));

        // When
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                getApiUrl("/api/favorites/check/" + voiture.getId()),
                HttpMethod.GET,
                request,
                ApiResponse.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());

        Boolean isFavorite = objectMapper.convertValue(response.getBody().getData(), Boolean.class);
        assertFalse(isFavorite);
    }

    @Test
    void testRemoveFavorite_Success() {
        // Given - create a client user and a favorite
        User clientUser = createClientUser();
        String clientToken = getAuthToken(clientUser);
        
        User societeUser = createSocieteUser();
        Societe societe = createSociete(societeUser);
        Voiture voiture = createVoiture("Toyota", "Camry", new BigDecimal("50.00"), societe);
        voitureRepository.save(voiture);
        
        Favorite favorite = new Favorite(clientUser, voiture);
        favoriteRepository.save(favorite);

        HttpEntity<Void> request = new HttpEntity<>(createAuthHeaders(clientToken));

        // When
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                getApiUrl("/api/favorites/" + voiture.getId()),
                HttpMethod.DELETE,
                request,
                ApiResponse.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Favori supprimé", response.getBody().getMessage());

        // Verify favorite was removed from database
        List<Favorite> remainingFavorites = favoriteRepository.findAll();
        assertEquals(0, remainingFavorites.size());
        
        // Verify the specific favorite no longer exists
        Optional<Favorite> removedFavorite = favoriteRepository.findByUserAndVoiture(clientUser, voiture);
        assertFalse(removedFavorite.isPresent());
    }

    @Test
    void testRemoveFavorite_NotFound() {
        // Given - create a client user
        User clientUser = createClientUser();
        String clientToken = getAuthToken(clientUser);

        HttpEntity<Void> request = new HttpEntity<>(createAuthHeaders(clientToken));

        // When
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                getApiUrl("/api/favorites/999"),
                HttpMethod.DELETE,
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
