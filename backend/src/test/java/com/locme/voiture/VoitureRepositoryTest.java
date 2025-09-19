package com.locme.voiture;

import com.locme.auth.User;
import com.locme.auth.Role;
import com.locme.societe.Societe;
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
class VoitureRepositoryTestSimple {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private VoitureRepository voitureRepository;

    private Voiture testVoiture;
    private Societe testSociete;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setNom("Test User");
        testUser.setEmail("test@example.com");
        testUser.setMotDePasse("password");
        testUser.setRole(Role.SOCIETE);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
        testUser = entityManager.persistAndFlush(testUser);

        testSociete = new Societe();
        testSociete.setNom("Test Societe");
        testSociete.setUser(testUser);
        testSociete.setCreatedAt(LocalDateTime.now());
        testSociete.setUpdatedAt(LocalDateTime.now());
        testSociete = entityManager.persistAndFlush(testSociete);

        testVoiture = new Voiture();
        testVoiture.setMarque("Toyota");
        testVoiture.setModele("Camry");
        testVoiture.setPrixParJour(new BigDecimal("50.00"));
        testVoiture.setDisponible(true);
        testVoiture.setSociete(testSociete);
        testVoiture.setCreatedAt(LocalDateTime.now());
        testVoiture.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Test save and find voiture by ID")
    void testSaveAndFindById() {
        // Given
        Voiture savedVoiture = entityManager.persistAndFlush(testVoiture);

        // When
        Optional<Voiture> foundVoiture = voitureRepository.findById(savedVoiture.getId());

        // Then
        assertTrue(foundVoiture.isPresent());
        assertEquals("Toyota", foundVoiture.get().getMarque());
        assertEquals("Camry", foundVoiture.get().getModele());
        assertEquals(new BigDecimal("50.00"), foundVoiture.get().getPrixParJour());
        assertTrue(foundVoiture.get().getDisponible());
    }

    @Test
    @DisplayName("Test find all voitures")
    void testFindAll() {
        // Given
        Voiture voiture1 = new Voiture();
        voiture1.setMarque("Honda");
        voiture1.setModele("Civic");
        voiture1.setPrixParJour(new BigDecimal("40.00"));
        voiture1.setDisponible(true);
        voiture1.setSociete(testSociete);
        voiture1.setCreatedAt(LocalDateTime.now());
        voiture1.setUpdatedAt(LocalDateTime.now());

        Voiture voiture2 = new Voiture();
        voiture2.setMarque("Ford");
        voiture2.setModele("Focus");
        voiture2.setPrixParJour(new BigDecimal("35.00"));
        voiture2.setDisponible(false);
        voiture2.setSociete(testSociete);
        voiture2.setCreatedAt(LocalDateTime.now());
        voiture2.setUpdatedAt(LocalDateTime.now());

        entityManager.persistAndFlush(voiture1);
        entityManager.persistAndFlush(voiture2);

        // When
        List<Voiture> voitures = voitureRepository.findAll();

        // Then
        assertEquals(2, voitures.size());
        assertTrue(voitures.stream().anyMatch(v -> v.getMarque().equals("Honda")));
        assertTrue(voitures.stream().anyMatch(v -> v.getMarque().equals("Ford")));
    }

    @Test
    @DisplayName("Test find available voitures")
    void testFindByDisponibleTrue() {
        // Given
        Voiture availableVoiture = new Voiture();
        availableVoiture.setMarque("Honda");
        availableVoiture.setModele("Civic");
        availableVoiture.setPrixParJour(new BigDecimal("40.00"));
        availableVoiture.setDisponible(true);
        availableVoiture.setSociete(testSociete);
        availableVoiture.setCreatedAt(LocalDateTime.now());
        availableVoiture.setUpdatedAt(LocalDateTime.now());

        Voiture unavailableVoiture = new Voiture();
        unavailableVoiture.setMarque("Ford");
        unavailableVoiture.setModele("Focus");
        unavailableVoiture.setPrixParJour(new BigDecimal("35.00"));
        unavailableVoiture.setDisponible(false);
        unavailableVoiture.setSociete(testSociete);
        unavailableVoiture.setCreatedAt(LocalDateTime.now());
        unavailableVoiture.setUpdatedAt(LocalDateTime.now());

        entityManager.persistAndFlush(availableVoiture);
        entityManager.persistAndFlush(unavailableVoiture);

        // When
        List<Voiture> availableVoitures = voitureRepository.findByDisponibleTrue();

        // Then
        assertEquals(1, availableVoitures.size());
        assertEquals("Honda", availableVoitures.get(0).getMarque());
        assertTrue(availableVoitures.get(0).getDisponible());
    }

    @Test
    @DisplayName("Test delete voiture")
    void testDeleteVoiture() {
        // Given
        Voiture savedVoiture = entityManager.persistAndFlush(testVoiture);
        Long voitureId = savedVoiture.getId();

        // When
        voitureRepository.delete(savedVoiture);
        entityManager.flush();

        // Then
        Optional<Voiture> deletedVoiture = voitureRepository.findById(voitureId);
        assertFalse(deletedVoiture.isPresent());
    }

    @Test
    @DisplayName("Test update voiture")
    void testUpdateVoiture() {
        // Given
        Voiture savedVoiture = entityManager.persistAndFlush(testVoiture);
        savedVoiture.setMarque("Updated Toyota");
        savedVoiture.setPrixParJour(new BigDecimal("60.00"));

        // When
        Voiture updatedVoiture = voitureRepository.save(savedVoiture);
        entityManager.flush();

        // Then
        Optional<Voiture> foundVoiture = voitureRepository.findById(updatedVoiture.getId());
        assertTrue(foundVoiture.isPresent());
        assertEquals("Updated Toyota", foundVoiture.get().getMarque());
        assertEquals(new BigDecimal("60.00"), foundVoiture.get().getPrixParJour());
    }
}
