package com.locme.reservation;

import com.locme.auth.User;
import com.locme.auth.Role;
import com.locme.common.exceptions.BusinessException;
import com.locme.common.exceptions.ResourceNotFoundException;
import com.locme.reservation.dto.ReservationDto;
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
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private VoitureRepository voitureRepository;

    @InjectMocks
    private ReservationService reservationService;

    private Reservation testReservation;
    private ReservationDto testReservationDto;
    private User testUser;
    private User societeUser;
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

        societeUser = new User();
        societeUser.setId(2L);
        societeUser.setNom("Test Societe");
        societeUser.setEmail("societe@example.com");
        societeUser.setRole(Role.SOCIETE);

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

        testReservationDto = new ReservationDto();
        testReservationDto.setId(1L);
        testReservationDto.setVoitureId(1L);
        testReservationDto.setVoitureMarque("Toyota");
        testReservationDto.setVoitureModele("Camry");
        testReservationDto.setUserId(1L);
        testReservationDto.setUserNom("Test Client");
        testReservationDto.setDateDebut(LocalDate.now().plusDays(1));
        testReservationDto.setDateFin(LocalDate.now().plusDays(3));
        testReservationDto.setStatut(StatutReservation.EN_ATTENTE);
        testReservationDto.setMontant(new BigDecimal("150.00"));
    }

    @Test
    @DisplayName("Test get all reservations")
    void testGetAllReservations() {
        // Given
        List<Reservation> reservations = Arrays.asList(testReservation);
        when(reservationRepository.findAll()).thenReturn(reservations);

        // When
        List<ReservationDto> result = reservationService.getAllReservations();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("Toyota", result.get(0).getVoitureMarque());
        verify(reservationRepository).findAll();
    }

    @Test
    @DisplayName("Test get reservations by user")
    void testGetReservationsByUser() {
        // Given
        List<Reservation> reservations = Arrays.asList(testReservation);
        when(reservationRepository.findByUser(testUser)).thenReturn(reservations);

        // When
        List<ReservationDto> result = reservationService.getReservationsByUser(testUser);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getUserId());
        verify(reservationRepository).findByUser(testUser);
    }

    @Test
    @DisplayName("Test get reservations by societe")
    void testGetReservationsBySociete() {
        // Given
        List<Reservation> reservations = Arrays.asList(testReservation);
        when(reservationRepository.findByVoitureSocieteUser(societeUser)).thenReturn(reservations);

        // When
        List<ReservationDto> result = reservationService.getReservationsBySociete(societeUser);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(reservationRepository).findByVoitureSocieteUser(societeUser);
    }

    @Test
    @DisplayName("Test get reservation by ID")
    void testGetReservationById() {
        // Given
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(testReservation));

        // When
        ReservationDto result = reservationService.getReservationById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Toyota", result.getVoitureMarque());
        assertEquals("Test Client", result.getUserNom());
        verify(reservationRepository).findById(1L);
    }

    @Test
    @DisplayName("Test get reservation by ID not found")
    void testGetReservationByIdNotFound() {
        // Given
        when(reservationRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            reservationService.getReservationById(999L);
        });

        assertEquals("Réservation non trouvée", exception.getMessage());
        verify(reservationRepository).findById(999L);
    }

    @Test
    @DisplayName("Test create reservation")
    void testCreateReservation() {
        // Given
        ReservationDto newReservationDto = new ReservationDto();
        newReservationDto.setVoitureId(1L);
        newReservationDto.setDateDebut(LocalDate.now().plusDays(1));
        newReservationDto.setDateFin(LocalDate.now().plusDays(3));
        newReservationDto.setMontant(new BigDecimal("150.00"));

        when(voitureRepository.findById(1L)).thenReturn(Optional.of(testVoiture));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);

        // When
        ReservationDto result = reservationService.createReservation(newReservationDto, testUser);

        // Then
        assertNotNull(result);
        verify(voitureRepository).findById(1L);
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    @DisplayName("Test create reservation with voiture not found")
    void testCreateReservationVoitureNotFound() {
        // Given
        ReservationDto newReservationDto = new ReservationDto();
        newReservationDto.setVoitureId(999L);
        newReservationDto.setDateDebut(LocalDate.now().plusDays(1));
        newReservationDto.setDateFin(LocalDate.now().plusDays(3));
        newReservationDto.setMontant(new BigDecimal("150.00"));

        when(voitureRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            reservationService.createReservation(newReservationDto, testUser);
        });

        assertEquals("Voiture non trouvée", exception.getMessage());
        verify(voitureRepository).findById(999L);
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    @DisplayName("Test create reservation with voiture not available")
    void testCreateReservationVoitureNotAvailable() {
        // Given
        Voiture unavailableVoiture = new Voiture();
        unavailableVoiture.setId(1L);
        unavailableVoiture.setDisponible(false);

        ReservationDto newReservationDto = new ReservationDto();
        newReservationDto.setVoitureId(1L);
        newReservationDto.setDateDebut(LocalDate.now().plusDays(1));
        newReservationDto.setDateFin(LocalDate.now().plusDays(3));
        newReservationDto.setMontant(new BigDecimal("150.00"));

        when(voitureRepository.findById(1L)).thenReturn(Optional.of(unavailableVoiture));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            reservationService.createReservation(newReservationDto, testUser);
        });

        assertEquals("Cette voiture n'est pas disponible", exception.getMessage());
        verify(voitureRepository).findById(1L);
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    @DisplayName("Test create reservation with invalid dates")
    void testCreateReservationInvalidDates() {
        // Given
        ReservationDto newReservationDto = new ReservationDto();
        newReservationDto.setVoitureId(1L);
        newReservationDto.setDateDebut(LocalDate.now().minusDays(1)); // Past date
        newReservationDto.setDateFin(LocalDate.now().plusDays(3));
        newReservationDto.setMontant(new BigDecimal("150.00"));

        when(voitureRepository.findById(1L)).thenReturn(Optional.of(testVoiture));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            reservationService.createReservation(newReservationDto, testUser);
        });

        assertEquals("La date de début doit être dans le futur", exception.getMessage());
        verify(voitureRepository).findById(1L);
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    @DisplayName("Test create reservation with end date before start date")
    void testCreateReservationEndDateBeforeStartDate() {
        // Given
        ReservationDto newReservationDto = new ReservationDto();
        newReservationDto.setVoitureId(1L);
        newReservationDto.setDateDebut(LocalDate.now().plusDays(3));
        newReservationDto.setDateFin(LocalDate.now().plusDays(1)); // End before start
        newReservationDto.setMontant(new BigDecimal("150.00"));

        when(voitureRepository.findById(1L)).thenReturn(Optional.of(testVoiture));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            reservationService.createReservation(newReservationDto, testUser);
        });

        assertEquals("La date de fin doit être après la date de début", exception.getMessage());
        verify(voitureRepository).findById(1L);
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    @DisplayName("Test update reservation status")
    void testUpdateReservationStatus() {
        // Given
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(testReservation));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);

        // When
        ReservationDto result = reservationService.updateReservationStatus(1L, StatutReservation.CONFIRMEE, societeUser);

        // Then
        assertNotNull(result);
        verify(reservationRepository).findById(1L);
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    @DisplayName("Test update reservation status not found")
    void testUpdateReservationStatusNotFound() {
        // Given
        when(reservationRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            reservationService.updateReservationStatus(999L, StatutReservation.CONFIRMEE, societeUser);
        });

        assertEquals("Réservation non trouvée", exception.getMessage());
        verify(reservationRepository).findById(999L);
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    @DisplayName("Test update reservation status unauthorized")
    void testUpdateReservationStatusUnauthorized() {
        // Given
        User unauthorizedUser = new User();
        unauthorizedUser.setId(3L);
        unauthorizedUser.setRole(Role.CLIENT);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(testReservation));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            reservationService.updateReservationStatus(1L, StatutReservation.CONFIRMEE, unauthorizedUser);
        });

        assertEquals("Vous n'êtes pas autorisé à modifier cette réservation", exception.getMessage());
        verify(reservationRepository).findById(1L);
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    @DisplayName("Test delete reservation")
    void testDeleteReservation() {
        // Given
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(testReservation));

        // When
        reservationService.deleteReservation(1L, testUser);

        // Then
        verify(reservationRepository).findById(1L);
        verify(reservationRepository).delete(testReservation);
    }

    @Test
    @DisplayName("Test delete reservation not found")
    void testDeleteReservationNotFound() {
        // Given
        when(reservationRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            reservationService.deleteReservation(999L, testUser);
        });

        assertEquals("Réservation non trouvée", exception.getMessage());
        verify(reservationRepository).findById(999L);
        verify(reservationRepository, never()).delete(any(Reservation.class));
    }

    @Test
    @DisplayName("Test delete reservation unauthorized")
    void testDeleteReservationUnauthorized() {
        // Given
        User unauthorizedUser = new User();
        unauthorizedUser.setId(3L);
        unauthorizedUser.setRole(Role.CLIENT);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(testReservation));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            reservationService.deleteReservation(1L, unauthorizedUser);
        });

        assertEquals("Vous n'êtes pas autorisé à supprimer cette réservation", exception.getMessage());
        verify(reservationRepository).findById(1L);
        verify(reservationRepository, never()).delete(any(Reservation.class));
    }
}
