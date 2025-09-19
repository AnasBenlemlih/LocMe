package com.locme.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.locme.auth.dto.AuthResponse;
import com.locme.auth.dto.LoginRequest;
import com.locme.auth.dto.RegisterRequest;
import com.locme.common.ApiResponse;
import com.locme.common.exceptions.BusinessException;
import com.locme.config.TestSecurityConfig;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Test successful user registration")
    void testRegisterSuccess() throws Exception {
        // Given
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setNom("John Doe");
        registerRequest.setEmail("john@example.com");
        registerRequest.setMotDePasse("password123");
        registerRequest.setRole(Role.CLIENT);

        AuthResponse authResponse = new AuthResponse("jwt-token", 1L, "John Doe", "john@example.com", Role.CLIENT);
        
        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Inscription réussie"))
                .andExpect(jsonPath("$.data.token").value("jwt-token"))
                .andExpect(jsonPath("$.data.nom").value("John Doe"))
                .andExpect(jsonPath("$.data.email").value("john@example.com"));
    }

    @Test
    @DisplayName("Test registration with existing email")
    void testRegisterExistingEmail() throws Exception {
        // Given
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setNom("John Doe");
        registerRequest.setEmail("existing@example.com");
        registerRequest.setMotDePasse("password123");

        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new BusinessException("L'email est déjà utilisé"));

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("L'email est déjà utilisé"));
    }

    @Test
    @DisplayName("Test registration with invalid data")
    void testRegisterInvalidData() throws Exception {
        // Given
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setNom(""); // Invalid: empty name
        registerRequest.setEmail("invalid-email"); // Invalid: not an email
        registerRequest.setMotDePasse("123"); // Invalid: too short

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Test successful login")
    void testLoginSuccess() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest("john@example.com", "password123");
        AuthResponse authResponse = new AuthResponse("jwt-token", 1L, "John Doe", "john@example.com", Role.CLIENT);
        
        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Connexion réussie"))
                .andExpect(jsonPath("$.data.token").value("jwt-token"))
                .andExpect(jsonPath("$.data.nom").value("John Doe"));
    }

    @Test
    @DisplayName("Test login with invalid credentials")
    void testLoginInvalidCredentials() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest("john@example.com", "wrongpassword");
        
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BusinessException("Email ou mot de passe incorrect"));

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Email ou mot de passe incorrect"));
    }

    @Test
    @DisplayName("Test login with invalid data")
    void testLoginInvalidData() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest("invalid-email", ""); // Invalid data

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Test get current user profile")
    @WithMockUser(username = "john@example.com", roles = {"CLIENT"})
    void testGetCurrentUser() throws Exception {
        // Given
        User user = new User();
        user.setId(1L);
        user.setNom("John Doe");
        user.setEmail("john@example.com");
        user.setRole(Role.CLIENT);
        
        when(authService.getCurrentUser()).thenReturn(user);

        // When & Then
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.nom").value("John Doe"))
                .andExpect(jsonPath("$.data.email").value("john@example.com"));
    }

    @Test
    @DisplayName("Test get current user when not authenticated")
    void testGetCurrentUserNotAuthenticated() throws Exception {
        // Given
        when(authService.getCurrentUser())
                .thenThrow(new BusinessException("Utilisateur non authentifié"));

        // When & Then
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Utilisateur non authentifié"));
    }
}
