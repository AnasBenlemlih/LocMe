package com.locme.paiement;

import com.locme.auth.User;
import com.locme.auth.Role;
import com.locme.reservation.Reservation;
import com.locme.reservation.StatutReservation;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class PaiementRepositoryTestSimple {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PaiementRepository paiementRepository;

    private Paiement testPaiement;
    private Reservation testReservation;
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

        // Create test reservation
        testReservation = new Reservation();
        testReservation.setVoiture(testVoiture);
        testReservation.setUser(testUser);
        testReservation.setDateDebut(LocalDate.now().plusDays(1));
        testReservation.setDateFin(LocalDate.now().plusDays(3));
        testReservation.setMontant(new BigDecimal("100.00"));
        testReservation.setStatut(StatutReservation.EN_ATTENTE);
        testReservation.setCreatedAt(LocalDateTime.now());
        testReservation.setUpdatedAt(LocalDateTime.now());
        testReservation = entityManager.persistAndFlush(testReservation);

        // Create test paiement
        testPaiement = new Paiement();
        testPaiement.setReservation(testReservation);
        testPaiement.setMontant(new BigDecimal("100.00"));
        testPaiement.setStatut(StatutPaiement.EN_ATTENTE);
        testPaiement.setMethodePaiement(MethodePaiement.CARTE_CREDIT);
        testPaiement.setTransactionId("TXN123456");
        testPaiement.setCreatedAt(LocalDateTime.now());
        testPaiement.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Test save and find paiement by ID")
    void testSaveAndFindById() {
        // Given
        Paiement savedPaiement = entityManager.persistAndFlush(testPaiement);

        // When
        Optional<Paiement> foundPaiement = paiementRepository.findById(savedPaiement.getId());

        // Then
        assertTrue(foundPaiement.isPresent());
        assertEquals(testReservation.getId(), foundPaiement.get().getReservation().getId());
        assertEquals(new BigDecimal("100.00"), foundPaiement.get().getMontant());
        assertEquals(StatutPaiement.EN_ATTENTE, foundPaiement.get().getStatut());
        assertEquals(MethodePaiement.CARTE_CREDIT, foundPaiement.get().getMethodePaiement());
        assertEquals("TXN123456", foundPaiement.get().getTransactionId());
    }

    @Test
    @DisplayName("Test find paiement by reservation")
    void testFindByReservation() {
        // Given
        Paiement savedPaiement = entityManager.persistAndFlush(testPaiement);

        // When
        Optional<Paiement> foundPaiement = paiementRepository.findByReservation(testReservation);

        // Then
        assertTrue(foundPaiement.isPresent());
        assertEquals(savedPaiement.getId(), foundPaiement.get().getId());
        assertEquals(testReservation.getId(), foundPaiement.get().getReservation().getId());
    }

    @Test
    @DisplayName("Test find paiement by transaction ID")
    void testFindByTransactionId() {
        // Given
        Paiement savedPaiement = entityManager.persistAndFlush(testPaiement);

        // When
        Optional<Paiement> foundPaiement = paiementRepository.findByTransactionId("TXN123456");

        // Then
        assertTrue(foundPaiement.isPresent());
        assertEquals(savedPaiement.getId(), foundPaiement.get().getId());
        assertEquals("TXN123456", foundPaiement.get().getTransactionId());
    }

    @Test
    @DisplayName("Test find paiements by status")
    void testFindByStatut() {
        // Given
        Paiement paiement1 = new Paiement();
        paiement1.setReservation(testReservation);
        paiement1.setMontant(new BigDecimal("100.00"));
        paiement1.setStatut(StatutPaiement.EN_ATTENTE);
        paiement1.setMethodePaiement(MethodePaiement.CARTE_CREDIT);
        paiement1.setTransactionId("TXN111111");
        paiement1.setCreatedAt(LocalDateTime.now());
        paiement1.setUpdatedAt(LocalDateTime.now());

        Paiement paiement2 = new Paiement();
        paiement2.setReservation(testReservation);
        paiement2.setMontant(new BigDecimal("100.00"));
        paiement2.setStatut(StatutPaiement.PAYE);
        paiement2.setMethodePaiement(MethodePaiement.CARTE_CREDIT);
        paiement2.setTransactionId("TXN222222");
        paiement2.setCreatedAt(LocalDateTime.now());
        paiement2.setUpdatedAt(LocalDateTime.now());

        entityManager.persistAndFlush(paiement1);
        entityManager.persistAndFlush(paiement2);

        // When
        List<Paiement> pendingPaiements = paiementRepository.findByStatut(StatutPaiement.EN_ATTENTE);

        // Then
        assertEquals(1, pendingPaiements.size());
        assertEquals(StatutPaiement.EN_ATTENTE, pendingPaiements.get(0).getStatut());
    }

    @Test
    @DisplayName("Test delete paiement")
    void testDeletePaiement() {
        // Given
        Paiement savedPaiement = entityManager.persistAndFlush(testPaiement);
        Long paiementId = savedPaiement.getId();

        // When
        paiementRepository.delete(savedPaiement);
        entityManager.flush();

        // Then
        Optional<Paiement> deletedPaiement = paiementRepository.findById(paiementId);
        assertFalse(deletedPaiement.isPresent());
    }

    @Test
    @DisplayName("Test update paiement")
    void testUpdatePaiement() {
        // Given
        Paiement savedPaiement = entityManager.persistAndFlush(testPaiement);
        savedPaiement.setStatut(StatutPaiement.PAYE);
        savedPaiement.setMontant(new BigDecimal("120.00"));

        // When
        Paiement updatedPaiement = paiementRepository.save(savedPaiement);
        entityManager.flush();

        // Then
        Optional<Paiement> foundPaiement = paiementRepository.findById(updatedPaiement.getId());
        assertTrue(foundPaiement.isPresent());
        assertEquals(StatutPaiement.PAYE, foundPaiement.get().getStatut());
        assertEquals(new BigDecimal("120.00"), foundPaiement.get().getMontant());
    }
}
