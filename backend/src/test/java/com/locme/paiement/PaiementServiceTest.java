package com.locme.paiement;

import com.locme.auth.User;
import com.locme.auth.Role;
import com.locme.common.exceptions.BusinessException;
import com.locme.common.exceptions.ResourceNotFoundException;
import com.locme.reservation.Reservation;
import com.locme.reservation.ReservationRepository;
import com.locme.reservation.StatutReservation;
import com.locme.societe.Societe;
import com.locme.voiture.Voiture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaiementServiceTest {

    @Mock
    private PaiementRepository paiementRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private PaiementService paiementService;

    private Paiement testPaiement;
    private Reservation testReservation;
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

        testReservation = new Reservation();
        testReservation.setId(1L);
        testReservation.setVoiture(testVoiture);
        testReservation.setUser(testUser);
        testReservation.setDateDebut(LocalDate.now().plusDays(1));
        testReservation.setDateFin(LocalDate.now().plusDays(3));
        testReservation.setStatut(StatutReservation.EN_ATTENTE);
        testReservation.setMontant(new BigDecimal("150.00"));

        testPaiement = new Paiement();
        testPaiement.setId(1L);
        testPaiement.setReservation(testReservation);
        testPaiement.setMontant(new BigDecimal("150.00"));
        testPaiement.setStatut(StatutPaiement.EN_ATTENTE);
        testPaiement.setMethodePaiement(MethodePaiement.CARTE_CREDIT);
        testPaiement.setStripePaymentIntentId("pi_test_123");
        testPaiement.setTransactionId("txn_123");
    }

    @Test
    @DisplayName("Test create paiement")
    void testCreatePaiement() {
        // Given
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(testReservation));
        when(paiementRepository.findByReservation(testReservation)).thenReturn(Optional.empty());
        when(paiementRepository.save(any(Paiement.class))).thenReturn(testPaiement);

        // When
        Paiement result = paiementService.createPaiement(1L, MethodePaiement.CARTE_CREDIT);

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("150.00"), result.getMontant());
        assertEquals(StatutPaiement.EN_ATTENTE, result.getStatut());
        assertEquals(MethodePaiement.CARTE_CREDIT, result.getMethodePaiement());
        verify(reservationRepository).findById(1L);
        verify(paiementRepository).findByReservation(testReservation);
        verify(paiementRepository).save(any(Paiement.class));
    }

    @Test
    @DisplayName("Test create paiement with reservation not found")
    void testCreatePaiementReservationNotFound() {
        // Given
        when(reservationRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            paiementService.createPaiement(999L, MethodePaiement.CARTE_CREDIT);
        });

        assertEquals("Réservation non trouvée", exception.getMessage());
        verify(reservationRepository).findById(999L);
        verify(paiementRepository, never()).save(any(Paiement.class));
    }

    @Test
    @DisplayName("Test create paiement when paiement already exists")
    void testCreatePaiementAlreadyExists() {
        // Given
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(testReservation));
        when(paiementRepository.findByReservation(testReservation)).thenReturn(Optional.of(testPaiement));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            paiementService.createPaiement(1L, MethodePaiement.CARTE_CREDIT);
        });

        assertEquals("Un paiement existe déjà pour cette réservation", exception.getMessage());
        verify(reservationRepository).findById(1L);
        verify(paiementRepository).findByReservation(testReservation);
        verify(paiementRepository, never()).save(any(Paiement.class));
    }

    @Test
    @DisplayName("Test process payment success")
    void testProcessPaymentSuccess() {
        // Given
        when(paiementRepository.findById(1L)).thenReturn(Optional.of(testPaiement));
        when(paiementRepository.save(any(Paiement.class))).thenReturn(testPaiement);

        // When
        Paiement result = paiementService.processPayment(1L, "pi_success_123", "txn_success_123");

        // Then
        assertNotNull(result);
        assertEquals(StatutPaiement.PAYE, result.getStatut());
        assertEquals("pi_success_123", result.getStripePaymentIntentId());
        assertEquals("txn_success_123", result.getTransactionId());
        assertNotNull(result.getDatePaiement());
        verify(paiementRepository).findById(1L);
        verify(paiementRepository).save(any(Paiement.class));
    }

    @Test
    @DisplayName("Test process payment failure")
    void testProcessPaymentFailure() {
        // Given
        when(paiementRepository.findById(1L)).thenReturn(Optional.of(testPaiement));
        when(paiementRepository.save(any(Paiement.class))).thenReturn(testPaiement);

        // When
        Paiement result = paiementService.processPaymentFailure(1L, "pi_failed_123", "Insufficient funds");

        // Then
        assertNotNull(result);
        assertEquals(StatutPaiement.ECHEC, result.getStatut());
        assertEquals("pi_failed_123", result.getStripePaymentIntentId());
        verify(paiementRepository).findById(1L);
        verify(paiementRepository).save(any(Paiement.class));
    }

    @Test
    @DisplayName("Test process payment with paiement not found")
    void testProcessPaymentNotFound() {
        // Given
        when(paiementRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            paiementService.processPayment(999L, "pi_test_123", "txn_test_123");
        });

        assertEquals("Paiement non trouvé", exception.getMessage());
        verify(paiementRepository).findById(999L);
        verify(paiementRepository, never()).save(any(Paiement.class));
    }

    @Test
    @DisplayName("Test get paiement by ID")
    void testGetPaiementById() {
        // Given
        when(paiementRepository.findById(1L)).thenReturn(Optional.of(testPaiement));

        // When
        Paiement result = paiementService.getPaiementById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(new BigDecimal("150.00"), result.getMontant());
        verify(paiementRepository).findById(1L);
    }

    @Test
    @DisplayName("Test get paiement by ID not found")
    void testGetPaiementByIdNotFound() {
        // Given
        when(paiementRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            paiementService.getPaiementById(999L);
        });

        assertEquals("Paiement non trouvé", exception.getMessage());
        verify(paiementRepository).findById(999L);
    }

    @Test
    @DisplayName("Test get paiement by reservation")
    void testGetPaiementByReservation() {
        // Given
        when(paiementRepository.findByReservation(testReservation)).thenReturn(Optional.of(testPaiement));

        // When
        Paiement result = paiementService.getPaiementByReservation(testReservation);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(testReservation.getId(), result.getReservation().getId());
        verify(paiementRepository).findByReservation(testReservation);
    }

    @Test
    @DisplayName("Test get paiement by reservation not found")
    void testGetPaiementByReservationNotFound() {
        // Given
        when(paiementRepository.findByReservation(testReservation)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            paiementService.getPaiementByReservation(testReservation);
        });

        assertEquals("Paiement non trouvé pour cette réservation", exception.getMessage());
        verify(paiementRepository).findByReservation(testReservation);
    }

    @Test
    @DisplayName("Test refund paiement")
    void testRefundPaiement() {
        // Given
        testPaiement.setStatut(StatutPaiement.PAYE);
        when(paiementRepository.findById(1L)).thenReturn(Optional.of(testPaiement));
        when(paiementRepository.save(any(Paiement.class))).thenReturn(testPaiement);

        // When
        Paiement result = paiementService.refundPaiement(1L, new BigDecimal("150.00"));

        // Then
        assertNotNull(result);
        assertEquals(StatutPaiement.REMBOURSE, result.getStatut());
        verify(paiementRepository).findById(1L);
        verify(paiementRepository).save(any(Paiement.class));
    }

    @Test
    @DisplayName("Test refund paiement not found")
    void testRefundPaiementNotFound() {
        // Given
        when(paiementRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            paiementService.refundPaiement(999L, new BigDecimal("150.00"));
        });

        assertEquals("Paiement non trouvé", exception.getMessage());
        verify(paiementRepository).findById(999L);
        verify(paiementRepository, never()).save(any(Paiement.class));
    }

    @Test
    @DisplayName("Test refund paiement not paid")
    void testRefundPaiementNotPaid() {
        // Given
        testPaiement.setStatut(StatutPaiement.EN_ATTENTE);
        when(paiementRepository.findById(1L)).thenReturn(Optional.of(testPaiement));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            paiementService.refundPaiement(1L, new BigDecimal("150.00"));
        });

        assertEquals("Seuls les paiements payés peuvent être remboursés", exception.getMessage());
        verify(paiementRepository).findById(1L);
        verify(paiementRepository, never()).save(any(Paiement.class));
    }
}
