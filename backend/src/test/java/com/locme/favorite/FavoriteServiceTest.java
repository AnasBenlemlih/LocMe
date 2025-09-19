package com.locme.favorite;

import com.locme.auth.User;
import com.locme.auth.Role;
import com.locme.common.exceptions.ResourceNotFoundException;
import com.locme.societe.Societe;
import com.locme.voiture.Voiture;
import com.locme.voiture.VoitureRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private VoitureRepository voitureRepository;

    @InjectMocks
    private FavoriteService favoriteService;

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
    void testToggleFavoriteAdd() {
        // Given
        when(voitureRepository.findById(1L)).thenReturn(Optional.of(testVoiture));
        when(favoriteRepository.findByUserAndVoiture(testUser, testVoiture)).thenReturn(Optional.empty());
        when(favoriteRepository.save(any(Favorite.class))).thenReturn(testFavorite);

        // When
        Favorite result = favoriteService.toggleFavorite(1L, testUser);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(testUser, result.getUser());
        assertEquals(testVoiture, result.getVoiture());
        verify(voitureRepository).findById(1L);
        verify(favoriteRepository).findByUserAndVoiture(testUser, testVoiture);
        verify(favoriteRepository).save(any(Favorite.class));
    }

    @Test
    @DisplayName("Test toggle favorite - remove from favorites")
    void testToggleFavoriteRemove() {
        // Given
        when(voitureRepository.findById(1L)).thenReturn(Optional.of(testVoiture));
        when(favoriteRepository.findByUserAndVoiture(testUser, testVoiture)).thenReturn(Optional.of(testFavorite));

        // When
        Favorite result = favoriteService.toggleFavorite(1L, testUser);

        // Then
        assertNull(result);
        verify(voitureRepository).findById(1L);
        verify(favoriteRepository).findByUserAndVoiture(testUser, testVoiture);
        verify(favoriteRepository).delete(testFavorite);
        verify(favoriteRepository, never()).save(any(Favorite.class));
    }

    @Test
    @DisplayName("Test toggle favorite with voiture not found")
    void testToggleFavoriteVoitureNotFound() {
        // Given
        when(voitureRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            favoriteService.toggleFavorite(999L, testUser);
        });

        assertEquals("Voiture non trouvée", exception.getMessage());
        verify(voitureRepository).findById(999L);
        verify(favoriteRepository, never()).findByUserAndVoiture(any(User.class), any(Voiture.class));
        verify(favoriteRepository, never()).save(any(Favorite.class));
    }

    @Test
    @DisplayName("Test get user favorites")
    void testGetUserFavorites() {
        // Given
        List<Favorite> favorites = Arrays.asList(testFavorite);
        when(favoriteRepository.findByUser(testUser)).thenReturn(favorites);

        // When
        List<Favorite> result = favoriteService.getUserFavorites(testUser);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        verify(favoriteRepository).findByUser(testUser);
    }

    @Test
    @DisplayName("Test is favorite - true")
    void testIsFavoriteTrue() {
        // Given
        when(voitureRepository.findById(1L)).thenReturn(Optional.of(testVoiture));
        when(favoriteRepository.existsByUserAndVoiture(testUser, testVoiture)).thenReturn(true);

        // When
        boolean result = favoriteService.isFavorite(1L, testUser);

        // Then
        assertTrue(result);
        verify(voitureRepository).findById(1L);
        verify(favoriteRepository).existsByUserAndVoiture(testUser, testVoiture);
    }

    @Test
    @DisplayName("Test is favorite - false")
    void testIsFavoriteFalse() {
        // Given
        when(voitureRepository.findById(1L)).thenReturn(Optional.of(testVoiture));
        when(favoriteRepository.existsByUserAndVoiture(testUser, testVoiture)).thenReturn(false);

        // When
        boolean result = favoriteService.isFavorite(1L, testUser);

        // Then
        assertFalse(result);
        verify(voitureRepository).findById(1L);
        verify(favoriteRepository).existsByUserAndVoiture(testUser, testVoiture);
    }

    @Test
    @DisplayName("Test is favorite with voiture not found")
    void testIsFavoriteVoitureNotFound() {
        // Given
        when(voitureRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            favoriteService.isFavorite(999L, testUser);
        });

        assertEquals("Voiture non trouvée", exception.getMessage());
        verify(voitureRepository).findById(999L);
        verify(favoriteRepository, never()).existsByUserAndVoiture(any(User.class), any(Voiture.class));
    }

    @Test
    @DisplayName("Test remove favorite")
    void testRemoveFavorite() {
        // Given
        when(voitureRepository.findById(1L)).thenReturn(Optional.of(testVoiture));

        // When
        favoriteService.removeFavorite(1L, testUser);

        // Then
        verify(voitureRepository).findById(1L);
        verify(favoriteRepository).deleteByUserAndVoiture(testUser, testVoiture);
    }

    @Test
    @DisplayName("Test remove favorite with voiture not found")
    void testRemoveFavoriteVoitureNotFound() {
        // Given
        when(voitureRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            favoriteService.removeFavorite(999L, testUser);
        });

        assertEquals("Voiture non trouvée", exception.getMessage());
        verify(voitureRepository).findById(999L);
        verify(favoriteRepository, never()).deleteByUserAndVoiture(any(User.class), any(Voiture.class));
    }

    @Test
    @DisplayName("Test get user favorites empty list")
    void testGetUserFavoritesEmpty() {
        // Given
        when(favoriteRepository.findByUser(testUser)).thenReturn(Arrays.asList());

        // When
        List<Favorite> result = favoriteService.getUserFavorites(testUser);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(favoriteRepository).findByUser(testUser);
    }

    @Test
    @DisplayName("Test toggle favorite with multiple users")
    void testToggleFavoriteMultipleUsers() {
        // Given
        User anotherUser = new User();
        anotherUser.setId(2L);
        anotherUser.setRole(Role.CLIENT);

        when(voitureRepository.findById(1L)).thenReturn(Optional.of(testVoiture));
        when(favoriteRepository.findByUserAndVoiture(testUser, testVoiture)).thenReturn(Optional.empty());
        when(favoriteRepository.findByUserAndVoiture(anotherUser, testVoiture)).thenReturn(Optional.empty());
        when(favoriteRepository.save(any(Favorite.class))).thenReturn(testFavorite);

        // When
        Favorite result1 = favoriteService.toggleFavorite(1L, testUser);
        Favorite result2 = favoriteService.toggleFavorite(1L, anotherUser);

        // Then
        assertNotNull(result1);
        assertNotNull(result2);
        verify(voitureRepository, times(2)).findById(1L);
        verify(favoriteRepository).findByUserAndVoiture(testUser, testVoiture);
        verify(favoriteRepository).findByUserAndVoiture(anotherUser, testVoiture);
        verify(favoriteRepository, times(2)).save(any(Favorite.class));
    }
}
