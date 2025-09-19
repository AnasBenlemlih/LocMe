package com.locme.auth;

import com.locme.auth.dto.AuthResponse;
import com.locme.auth.dto.LoginRequest;
import com.locme.auth.dto.RegisterRequest;
import com.locme.common.exceptions.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setNom("John Doe");
        testUser.setEmail("john@example.com");
        testUser.setMotDePasse("encodedPassword");
        testUser.setRole(Role.CLIENT);

        registerRequest = new RegisterRequest();
        registerRequest.setNom("John Doe");
        registerRequest.setEmail("john@example.com");
        registerRequest.setMotDePasse("password123");
        registerRequest.setRole(Role.CLIENT);

        loginRequest = new LoginRequest("john@example.com", "password123");
    }

    @Test
    @DisplayName("Test successful user registration")
    void testRegisterSuccess() {
        // Given
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtService.generateToken(testUser)).thenReturn("jwt-token");

        // When
        AuthResponse response = authService.register(registerRequest);

        // Then
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals(1L, response.getId());
        assertEquals("John Doe", response.getNom());
        assertEquals("john@example.com", response.getEmail());
        assertEquals(Role.CLIENT, response.getRole());

        verify(userRepository).existsByEmail("john@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken(testUser);
    }

    @Test
    @DisplayName("Test registration with existing email")
    void testRegisterExistingEmail() {
        // Given
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.register(registerRequest);
        });

        assertEquals("L'email est déjà utilisé", exception.getMessage());
        verify(userRepository).existsByEmail("john@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Test successful login")
    void testLoginSuccess() {
        // Given
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtService.generateToken(testUser)).thenReturn("jwt-token");

        // When
        AuthResponse response = authService.login(loginRequest);

        // Then
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals(1L, response.getId());
        assertEquals("John Doe", response.getNom());
        assertEquals("john@example.com", response.getEmail());
        assertEquals(Role.CLIENT, response.getRole());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken(testUser);
    }

    @Test
    @DisplayName("Test login with invalid credentials")
    void testLoginInvalidCredentials() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // When & Then
        assertThrows(BadCredentialsException.class, () -> {
            authService.login(loginRequest);
        });

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, never()).generateToken(any(User.class));
    }

    @Test
    @DisplayName("Test get current user when authenticated")
    void testGetCurrentUserAuthenticated() {
        // Given
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(testUser);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // When
        User currentUser = authService.getCurrentUser();

        // Then
        assertNotNull(currentUser);
        assertEquals(1L, currentUser.getId());
        assertEquals("John Doe", currentUser.getNom());
        assertEquals("john@example.com", currentUser.getEmail());
    }

    @Test
    @DisplayName("Test get current user when not authenticated")
    void testGetCurrentUserNotAuthenticated() {
        // Given
        SecurityContextHolder.clearContext();

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.getCurrentUser();
        });

        assertEquals("Utilisateur non authentifié", exception.getMessage());
    }

    @Test
    @DisplayName("Test get current user with non-User principal")
    void testGetCurrentUserWithNonUserPrincipal() {
        // Given
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn("not-a-user");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.getCurrentUser();
        });

        assertEquals("Utilisateur non authentifié", exception.getMessage());
    }

    @Test
    @DisplayName("Test registration with null authentication context")
    void testGetCurrentUserWithNullAuthentication() {
        // Given
        SecurityContextHolder.getContext().setAuthentication(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.getCurrentUser();
        });

        assertEquals("Utilisateur non authentifié", exception.getMessage());
    }
}
