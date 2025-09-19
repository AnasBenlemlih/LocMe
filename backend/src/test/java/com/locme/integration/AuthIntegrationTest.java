package com.locme.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.locme.auth.Role;
import com.locme.auth.User;
import com.locme.auth.UserRepository;
import com.locme.auth.dto.AuthResponse;
import com.locme.auth.dto.LoginRequest;
import com.locme.auth.dto.RegisterRequest;
import com.locme.common.ApiResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Auth API endpoints.
 * Tests user registration, login, and authentication flows with database persistence.
 */
public class AuthIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${spring.security.jwt.secret}")
    private String jwtSecret;

    @Test
    void testRegisterUser_Success() {
        // Given
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setNom("John Doe");
        registerRequest.setEmail("john.doe@example.com");
        registerRequest.setMotDePasse("password123");
        registerRequest.setTelephone("+1234567890");
        registerRequest.setAdresse("123 Main St");
        registerRequest.setRole(Role.CLIENT);

        HttpEntity<RegisterRequest> request = new HttpEntity<>(registerRequest, createHeaders());

        // When
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                getApiUrl("/api/auth/register"),
                HttpMethod.POST,
                request,
                ApiResponse.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());

        // Verify user was saved in database
        Optional<User> savedUser = userRepository.findByEmail("john.doe@example.com");
        assertTrue(savedUser.isPresent(), "L'utilisateur devrait être sauvegardé en base");
        assertEquals("John Doe", savedUser.get().getNom());
        assertEquals("john.doe@example.com", savedUser.get().getEmail());
        assertEquals(Role.CLIENT, savedUser.get().getRole());
        assertTrue(passwordEncoder.matches("password123", savedUser.get().getMotDePasse()));

        // Verify JWT token is returned and valid
        AuthResponse authResponse = objectMapper.convertValue(response.getBody().getData(), AuthResponse.class);
        assertNotNull(authResponse.getToken(), "Le token JWT devrait être présent");
        assertTrue(isValidJwtToken(authResponse.getToken()), "Le token JWT devrait être valide");
        assertEquals("John Doe", authResponse.getNom());
        assertEquals("john.doe@example.com", authResponse.getEmail());
        assertEquals(Role.CLIENT, authResponse.getRole());
    }

    @Test
    void testRegisterUser_DuplicateEmail() {
        // Given - create a user first
        User existingUser = new User();
        existingUser.setNom("Existing User");
        existingUser.setEmail("existing@example.com");
        existingUser.setMotDePasse(passwordEncoder.encode("password123"));
        existingUser.setRole(Role.CLIENT);
        userRepository.save(existingUser);

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setNom("New User");
        registerRequest.setEmail("existing@example.com");
        registerRequest.setMotDePasse("password123");
        registerRequest.setRole(Role.CLIENT);

        HttpEntity<RegisterRequest> request = new HttpEntity<>(registerRequest, createHeaders());

        // When
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                getApiUrl("/api/auth/register"),
                HttpMethod.POST,
                request,
                ApiResponse.class
        );

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
    }

    @Test
    void testLoginUser_Success() {
        // Given - create a user first
        User user = new User();
        user.setNom("Jane Doe");
        user.setEmail("jane.doe@example.com");
        user.setMotDePasse(passwordEncoder.encode("password123"));
        user.setRole(Role.CLIENT);
        userRepository.save(user);

        LoginRequest loginRequest = new LoginRequest("jane.doe@example.com", "password123");
        HttpEntity<LoginRequest> request = new HttpEntity<>(loginRequest, createHeaders());

        // When
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                getApiUrl("/api/auth/login"),
                HttpMethod.POST,
                request,
                ApiResponse.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());

        // Verify JWT token is returned and valid
        AuthResponse authResponse = objectMapper.convertValue(response.getBody().getData(), AuthResponse.class);
        assertNotNull(authResponse.getToken());
        assertTrue(isValidJwtToken(authResponse.getToken()));
        assertEquals("Jane Doe", authResponse.getNom());
        assertEquals("jane.doe@example.com", authResponse.getEmail());
        assertEquals(Role.CLIENT, authResponse.getRole());
    }

    @Test
    void testLoginUser_InvalidCredentials() {
        // Given
        LoginRequest loginRequest = new LoginRequest("nonexistent@example.com", "wrongpassword");
        HttpEntity<LoginRequest> request = new HttpEntity<>(loginRequest, createHeaders());

        // When
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                getApiUrl("/api/auth/login"),
                HttpMethod.POST,
                request,
                ApiResponse.class
        );

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Email ou mot de passe incorrect", response.getBody().getMessage());
    }

    @Test
    void testLoginUser_WrongPassword() {
        // Given - create a user first
        User user = new User();
        user.setNom("Test User");
        user.setEmail("test@example.com");
        user.setMotDePasse(passwordEncoder.encode("correctpassword"));
        user.setRole(Role.CLIENT);
        userRepository.save(user);

        LoginRequest loginRequest = new LoginRequest("test@example.com", "wrongpassword");
        HttpEntity<LoginRequest> request = new HttpEntity<>(loginRequest, createHeaders());

        // When
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                getApiUrl("/api/auth/login"),
                HttpMethod.POST,
                request,
                ApiResponse.class
        );

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Email ou mot de passe incorrect", response.getBody().getMessage());
    }

    @Test
    void testGetCurrentUser_Success() {
        // Given - register and login a user
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setNom("Current User");
        registerRequest.setEmail("current@example.com");
        registerRequest.setMotDePasse("password123");
        registerRequest.setRole(Role.SOCIETE);

        HttpEntity<RegisterRequest> registerEntity = new HttpEntity<>(registerRequest, createHeaders());
        ResponseEntity<ApiResponse> registerResponse = restTemplate.exchange(
                getApiUrl("/api/auth/register"),
                HttpMethod.POST,
                registerEntity,
                ApiResponse.class
        );

        AuthResponse authResponse = objectMapper.convertValue(registerResponse.getBody().getData(), AuthResponse.class);
        String token = authResponse.getToken();

        // When - get current user
        HttpEntity<Void> request = new HttpEntity<>(createAuthHeaders(token));
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                getApiUrl("/api/auth/me"),
                HttpMethod.GET,
                request,
                ApiResponse.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());

        User currentUser = objectMapper.convertValue(response.getBody().getData(), User.class);
        assertEquals("Current User", currentUser.getNom());
        assertEquals("current@example.com", currentUser.getEmail());
        assertEquals(Role.SOCIETE, currentUser.getRole());
    }

    @Test
    void testGetCurrentUser_Unauthorized() {
        // Given - no authentication token
        HttpEntity<Void> request = new HttpEntity<>(createHeaders());

        // When
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                getApiUrl("/api/auth/me"),
                HttpMethod.GET,
                request,
                ApiResponse.class
        );

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    /**
     * Helper method to validate JWT token.
     */
    private boolean isValidJwtToken(String token) {
        try {
            // Utiliser la même méthode que JwtService pour décoder la clé
            byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
            SecretKey key = Keys.hmacShaKeyFor(keyBytes);
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getSubject() != null && !claims.getSubject().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
}
