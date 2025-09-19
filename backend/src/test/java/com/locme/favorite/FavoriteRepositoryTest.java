package com.locme.favorite;

import com.locme.auth.User;
import com.locme.auth.Role;
import com.locme.societe.Societe;
import com.locme.voiture.Voiture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class FavoriteRepositoryTestSimple {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private FavoriteRepository favoriteRepository;

    private Favorite testFavorite;
    private Voiture testVoiture;
    private User testUser;
    private Societe testSociete;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setNom("Test User");
        testUser.setEmail("test@example.com");
        testUser.setMotDePasse("password");
        testUser.setRole(Role.CLIENT);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
        testUser = entityManager.persistAndFlush(testUser);

        // Create test societe
        User societeUser = new User();
        societeUser.setNom("Societe User");
        societeUser.setEmail("societe@example.com");
        societeUser.setMotDePasse("password");
        societeUser.setRole(Role.SOCIETE);
        societeUser.setCreatedAt(LocalDateTime.now());
        societeUser.setUpdatedAt(LocalDateTime.now());
        societeUser = entityManager.persistAndFlush(societeUser);

        testSociete = new Societe();
        testSociete.setNom("Test Societe");
        testSociete.setUser(societeUser);
        testSociete.setCreatedAt(LocalDateTime.now());
        testSociete.setUpdatedAt(LocalDateTime.now());
        testSociete = entityManager.persistAndFlush(testSociete);

        // Create test voiture
        testVoiture = new Voiture();
        testVoiture.setMarque("Toyota");
        testVoiture.setModele("Camry");
        testVoiture.setPrixParJour(new BigDecimal("50.00"));
        testVoiture.setDisponible(true);
        testVoiture.setSociete(testSociete);
        testVoiture.setCreatedAt(LocalDateTime.now());
        testVoiture.setUpdatedAt(LocalDateTime.now());
        testVoiture = entityManager.persistAndFlush(testVoiture);

        // Create test favorite
        testFavorite = new Favorite();
        testFavorite.setUser(testUser);
        testFavorite.setVoiture(testVoiture);
        testFavorite.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Test save and find favorite by ID")
    void testSaveAndFindById() {
        // Given
        Favorite savedFavorite = entityManager.persistAndFlush(testFavorite);

        // When
        Optional<Favorite> foundFavorite = favoriteRepository.findById(savedFavorite.getId());

        // Then
        assertTrue(foundFavorite.isPresent());
        assertEquals(testUser.getId(), foundFavorite.get().getUser().getId());
        assertEquals(testVoiture.getId(), foundFavorite.get().getVoiture().getId());
    }

    @Test
    @DisplayName("Test find favorite by user and voiture")
    void testFindByUserAndVoiture() {
        // Given
        Favorite savedFavorite = entityManager.persistAndFlush(testFavorite);

        // When
        Optional<Favorite> foundFavorite = favoriteRepository.findByUserAndVoiture(testUser, testVoiture);

        // Then
        assertTrue(foundFavorite.isPresent());
        assertEquals(savedFavorite.getId(), foundFavorite.get().getId());
        assertEquals(testUser.getId(), foundFavorite.get().getUser().getId());
        assertEquals(testVoiture.getId(), foundFavorite.get().getVoiture().getId());
    }

    @Test
    @DisplayName("Test find favorites by user")
    void testFindByUser() {
        // Given
        Voiture voiture2 = new Voiture();
        voiture2.setMarque("Honda");
        voiture2.setModele("Civic");
        voiture2.setPrixParJour(new BigDecimal("40.00"));
        voiture2.setDisponible(true);
        voiture2.setSociete(testSociete);
        voiture2.setCreatedAt(LocalDateTime.now());
        voiture2.setUpdatedAt(LocalDateTime.now());
        voiture2 = entityManager.persistAndFlush(voiture2);

        Favorite favorite1 = new Favorite();
        favorite1.setUser(testUser);
        favorite1.setVoiture(testVoiture);
        favorite1.setCreatedAt(LocalDateTime.now());

        Favorite favorite2 = new Favorite();
        favorite2.setUser(testUser);
        favorite2.setVoiture(voiture2);
        favorite2.setCreatedAt(LocalDateTime.now());

        entityManager.persistAndFlush(favorite1);
        entityManager.persistAndFlush(favorite2);

        // When
        List<Favorite> userFavorites = favoriteRepository.findByUser(testUser);

        // Then
        assertEquals(2, userFavorites.size());
        assertTrue(userFavorites.stream().allMatch(f -> f.getUser().getId().equals(testUser.getId())));
    }

    @Test
    @DisplayName("Test exists by user and voiture")
    void testExistsByUserAndVoiture() {
        // Given
        entityManager.persistAndFlush(testFavorite);

        // When
        boolean exists = favoriteRepository.existsByUserAndVoiture(testUser, testVoiture);

        // Then
        assertTrue(exists);
    }

    @Test
    @DisplayName("Test exists by user and voiture when not exists")
    void testExistsByUserAndVoitureNotExists() {
        // Given
        Voiture otherVoiture = new Voiture();
        otherVoiture.setMarque("Ford");
        otherVoiture.setModele("Focus");
        otherVoiture.setPrixParJour(new BigDecimal("35.00"));
        otherVoiture.setDisponible(true);
        otherVoiture.setSociete(testSociete);
        otherVoiture.setCreatedAt(LocalDateTime.now());
        otherVoiture.setUpdatedAt(LocalDateTime.now());
        otherVoiture = entityManager.persistAndFlush(otherVoiture);

        // When
        boolean exists = favoriteRepository.existsByUserAndVoiture(testUser, otherVoiture);

        // Then
        assertFalse(exists);
    }

    @Test
    @DisplayName("Test delete favorite")
    void testDeleteFavorite() {
        // Given
        Favorite savedFavorite = entityManager.persistAndFlush(testFavorite);
        Long favoriteId = savedFavorite.getId();

        // When
        favoriteRepository.delete(savedFavorite);
        entityManager.flush();

        // Then
        Optional<Favorite> deletedFavorite = favoriteRepository.findById(favoriteId);
        assertFalse(deletedFavorite.isPresent());
    }

    @Test
    @DisplayName("Test unique constraint on user and voiture")
    void testUniqueConstraint() {
        // Given
        entityManager.persistAndFlush(testFavorite);

        // When & Then
        Favorite duplicateFavorite = new Favorite();
        duplicateFavorite.setUser(testUser);
        duplicateFavorite.setVoiture(testVoiture);
        duplicateFavorite.setCreatedAt(LocalDateTime.now());

        assertThrows(Exception.class, () -> {
            entityManager.persistAndFlush(duplicateFavorite);
        });
    }
}
