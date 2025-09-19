package com.locme.reservation;

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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class ReservationRepositoryTestSimple {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ReservationRepository reservationRepository;

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
    }

    @Test
    @DisplayName("Test save and find reservation by ID")
    void testSaveAndFindById() {
        // Given
        Reservation savedReservation = entityManager.persistAndFlush(testReservation);

        // When
        Optional<Reservation> foundReservation = reservationRepository.findById(savedReservation.getId());

        // Then
        assertTrue(foundReservation.isPresent());
        assertEquals(testVoiture.getId(), foundReservation.get().getVoiture().getId());
        assertEquals(testUser.getId(), foundReservation.get().getUser().getId());
        assertEquals(StatutReservation.EN_ATTENTE, foundReservation.get().getStatut());
        assertEquals(new BigDecimal("100.00"), foundReservation.get().getMontant());
    }

    @Test
    @DisplayName("Test find reservations by user")
    void testFindByUser() {
        // Given
        Reservation reservation1 = new Reservation();
        reservation1.setVoiture(testVoiture);
        reservation1.setUser(testUser);
        reservation1.setDateDebut(LocalDate.now().plusDays(1));
        reservation1.setDateFin(LocalDate.now().plusDays(3));
        reservation1.setMontant(new BigDecimal("100.00"));
        reservation1.setStatut(StatutReservation.EN_ATTENTE);
        reservation1.setCreatedAt(LocalDateTime.now());
        reservation1.setUpdatedAt(LocalDateTime.now());

        Reservation reservation2 = new Reservation();
        reservation2.setVoiture(testVoiture);
        reservation2.setUser(testUser);
        reservation2.setDateDebut(LocalDate.now().plusDays(5));
        reservation2.setDateFin(LocalDate.now().plusDays(7));
        reservation2.setMontant(new BigDecimal("100.00"));
        reservation2.setStatut(StatutReservation.CONFIRMEE);
        reservation2.setCreatedAt(LocalDateTime.now());
        reservation2.setUpdatedAt(LocalDateTime.now());

        entityManager.persistAndFlush(reservation1);
        entityManager.persistAndFlush(reservation2);

        // When
        List<Reservation> userReservations = reservationRepository.findByUser(testUser);

        // Then
        assertEquals(2, userReservations.size());
        assertTrue(userReservations.stream().allMatch(r -> r.getUser().getId().equals(testUser.getId())));
    }

    @Test
    @DisplayName("Test find reservations by status")
    void testFindByStatut() {
        // Given
        Reservation reservation1 = new Reservation();
        reservation1.setVoiture(testVoiture);
        reservation1.setUser(testUser);
        reservation1.setDateDebut(LocalDate.now().plusDays(1));
        reservation1.setDateFin(LocalDate.now().plusDays(3));
        reservation1.setMontant(new BigDecimal("100.00"));
        reservation1.setStatut(StatutReservation.EN_ATTENTE);
        reservation1.setCreatedAt(LocalDateTime.now());
        reservation1.setUpdatedAt(LocalDateTime.now());

        Reservation reservation2 = new Reservation();
        reservation2.setVoiture(testVoiture);
        reservation2.setUser(testUser);
        reservation2.setDateDebut(LocalDate.now().plusDays(5));
        reservation2.setDateFin(LocalDate.now().plusDays(7));
        reservation2.setMontant(new BigDecimal("100.00"));
        reservation2.setStatut(StatutReservation.CONFIRMEE);
        reservation2.setCreatedAt(LocalDateTime.now());
        reservation2.setUpdatedAt(LocalDateTime.now());

        entityManager.persistAndFlush(reservation1);
        entityManager.persistAndFlush(reservation2);

        // When
        List<Reservation> pendingReservations = reservationRepository.findByStatut(StatutReservation.EN_ATTENTE);

        // Then
        assertEquals(1, pendingReservations.size());
        assertEquals(StatutReservation.EN_ATTENTE, pendingReservations.get(0).getStatut());
    }

    @Test
    @DisplayName("Test delete reservation")
    void testDeleteReservation() {
        // Given
        Reservation savedReservation = entityManager.persistAndFlush(testReservation);
        Long reservationId = savedReservation.getId();

        // When
        reservationRepository.delete(savedReservation);
        entityManager.flush();

        // Then
        Optional<Reservation> deletedReservation = reservationRepository.findById(reservationId);
        assertFalse(deletedReservation.isPresent());
    }

    @Test
    @DisplayName("Test update reservation")
    void testUpdateReservation() {
        // Given
        Reservation savedReservation = entityManager.persistAndFlush(testReservation);
        savedReservation.setStatut(StatutReservation.CONFIRMEE);
        savedReservation.setMontant(new BigDecimal("120.00"));

        // When
        Reservation updatedReservation = reservationRepository.save(savedReservation);
        entityManager.flush();

        // Then
        Optional<Reservation> foundReservation = reservationRepository.findById(updatedReservation.getId());
        assertTrue(foundReservation.isPresent());
        assertEquals(StatutReservation.CONFIRMEE, foundReservation.get().getStatut());
        assertEquals(new BigDecimal("120.00"), foundReservation.get().getMontant());
    }
}
