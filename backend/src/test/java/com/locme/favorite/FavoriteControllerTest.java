package com.locme.favorite;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.locme.auth.AuthService;
import com.locme.auth.JwtService;
import com.locme.auth.User;
import com.locme.auth.Role;
import com.locme.common.ApiResponse;
import com.locme.common.exceptions.ResourceNotFoundException;
import com.locme.config.TestSecurityConfig;
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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FavoriteController.class)
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
class FavoriteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FavoriteService favoriteService;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private Favorite testFavorite;
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

        testFavorite = new Favorite();
        testFavorite.setId(1L);
        testFavorite.setUser(testUser);
        testFavorite.setVoiture(testVoiture);
        testFavorite.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Test toggle favorite - add to favorites")
    @WithMockUser(username = "client@example.com", roles = {"CLIENT"})
    void testToggleFavoriteAdd() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(testUser);
        when(favoriteService.toggleFavorite(1L, testUser)).thenReturn(testFavorite);

        // When & Then
        mockMvc.perform(post("/api/favorites/toggle/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Ajouté aux favoris"))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @DisplayName("Test toggle favorite - remove from favorites")
    @WithMockUser(username = "client@example.com", roles = {"CLIENT"})
    void testToggleFavoriteRemove() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(testUser);
        when(favoriteService.toggleFavorite(1L, testUser)).thenReturn(null);

        // When & Then
        mockMvc.perform(post("/api/favorites/toggle/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Retiré des favoris"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("Test toggle favorite with voiture not found")
    @WithMockUser(username = "client@example.com", roles = {"CLIENT"})
    void testToggleFavoriteVoitureNotFound() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(testUser);
        when(favoriteService.toggleFavorite(999L, testUser))
                .thenThrow(new ResourceNotFoundException("Voiture non trouvée"));

        // When & Then
        mockMvc.perform(post("/api/favorites/toggle/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Test toggle favorite unauthorized")
    void testToggleFavoriteUnauthorized() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/favorites/toggle/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Test get user favorites")
    @WithMockUser(username = "client@example.com", roles = {"CLIENT"})
    void testGetUserFavorites() throws Exception {
        // Given
        List<Favorite> favorites = Arrays.asList(testFavorite);
        when(authService.getCurrentUser()).thenReturn(testUser);
        when(favoriteService.getUserFavorites(testUser)).thenReturn(favorites);

        // When & Then
        mockMvc.perform(get("/api/favorites/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    @Test
    @DisplayName("Test get user favorites unauthorized access")
    @WithMockUser(username = "client@example.com", roles = {"CLIENT"})
    void testGetUserFavoritesUnauthorized() throws Exception {
        // Given
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setRole(Role.CLIENT);

        when(authService.getCurrentUser()).thenReturn(otherUser);

        // When & Then
        mockMvc.perform(get("/api/favorites/user/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Accès non autorisé"));
    }

    @Test
    @DisplayName("Test get user favorites as admin")
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void testGetUserFavoritesAsAdmin() throws Exception {
        // Given
        User adminUser = new User();
        adminUser.setId(3L);
        adminUser.setRole(Role.ADMIN);

        List<Favorite> favorites = Arrays.asList(testFavorite);
        when(authService.getCurrentUser()).thenReturn(adminUser);
        when(favoriteService.getUserFavorites(adminUser)).thenReturn(favorites);

        // When & Then
        mockMvc.perform(get("/api/favorites/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("Test check if voiture is favorite")
    @WithMockUser(username = "client@example.com", roles = {"CLIENT"})
    void testIsFavorite() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(testUser);
        when(favoriteService.isFavorite(1L, testUser)).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/favorites/check/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @DisplayName("Test check if voiture is favorite - not found")
    @WithMockUser(username = "client@example.com", roles = {"CLIENT"})
    void testIsFavoriteNotFound() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(testUser);
        when(favoriteService.isFavorite(999L, testUser))
                .thenThrow(new ResourceNotFoundException("Voiture non trouvée"));

        // When & Then
        mockMvc.perform(get("/api/favorites/check/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Test remove favorite")
    @WithMockUser(username = "client@example.com", roles = {"CLIENT"})
    void testRemoveFavorite() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(testUser);
        doNothing().when(favoriteService).removeFavorite(1L, testUser);

        // When & Then
        mockMvc.perform(delete("/api/favorites/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Favori supprimé"));
    }

    @Test
    @DisplayName("Test remove favorite not found")
    @WithMockUser(username = "client@example.com", roles = {"CLIENT"})
    void testRemoveFavoriteNotFound() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(testUser);
        doThrow(new ResourceNotFoundException("Voiture non trouvée"))
                .when(favoriteService).removeFavorite(999L, testUser);

        // When & Then
        mockMvc.perform(delete("/api/favorites/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Test remove favorite unauthorized")
    void testRemoveFavoriteUnauthorized() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/favorites/1"))
                .andExpect(status().isUnauthorized());
    }
}
