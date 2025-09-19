package com.locme.reservation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.locme.auth.AuthService;
import com.locme.auth.JwtService;
import com.locme.auth.User;
import com.locme.auth.Role;
import com.locme.common.ApiResponse;
import com.locme.common.exceptions.ResourceNotFoundException;
import com.locme.config.TestSecurityConfig;
import com.locme.reservation.dto.ReservationDto;
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
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReservationController.class)
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReservationService reservationService;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private ReservationDto testReservationDto;
    private User testUser;
    private User societeUser;

    @BeforeEach
    void setUp() {
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
    @DisplayName("Test get all reservations - Admin only")
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void testGetAllReservations() throws Exception {
        // Given
        List<ReservationDto> reservations = Arrays.asList(testReservationDto);
        when(reservationService.getAllReservations()).thenReturn(reservations);

        // When & Then
        mockMvc.perform(get("/api/reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].voitureMarque").value("Toyota"));
    }

    @Test
    @DisplayName("Test get all reservations - Unauthorized")
    @WithMockUser(username = "client@example.com", roles = {"CLIENT"})
    void testGetAllReservationsUnauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/reservations"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Test get my reservations")
    @WithMockUser(username = "client@example.com", roles = {"CLIENT"})
    void testGetMyReservations() throws Exception {
        // Given
        List<ReservationDto> reservations = Arrays.asList(testReservationDto);
        when(authService.getCurrentUser()).thenReturn(testUser);
        when(reservationService.getReservationsByUser(testUser)).thenReturn(reservations);

        // When & Then
        mockMvc.perform(get("/api/reservations/my-reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].userId").value(1));
    }

    @Test
    @DisplayName("Test get reservations by societe")
    @WithMockUser(username = "societe@example.com", roles = {"SOCIETE"})
    void testGetReservationsBySociete() throws Exception {
        // Given
        List<ReservationDto> reservations = Arrays.asList(testReservationDto);
        when(authService.getCurrentUser()).thenReturn(societeUser);
        when(reservationService.getReservationsBySociete(societeUser)).thenReturn(reservations);

        // When & Then
        mockMvc.perform(get("/api/reservations/societe/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("Test get reservations by societe - Unauthorized access")
    @WithMockUser(username = "client@example.com", roles = {"CLIENT"})
    void testGetReservationsBySocieteUnauthorized() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(testUser);

        // When & Then
        mockMvc.perform(get("/api/reservations/societe/2"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Accès non autorisé"));
    }

    @Test
    @DisplayName("Test get reservation by ID")
    void testGetReservationById() throws Exception {
        // Given
        when(reservationService.getReservationById(1L)).thenReturn(testReservationDto);

        // When & Then
        mockMvc.perform(get("/api/reservations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.voitureMarque").value("Toyota"));
    }

    @Test
    @DisplayName("Test get reservation by ID not found")
    void testGetReservationByIdNotFound() throws Exception {
        // Given
        when(reservationService.getReservationById(999L))
                .thenThrow(new ResourceNotFoundException("Réservation non trouvée"));

        // When & Then
        mockMvc.perform(get("/api/reservations/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Test create reservation")
    @WithMockUser(username = "client@example.com", roles = {"CLIENT"})
    void testCreateReservation() throws Exception {
        // Given
        ReservationDto newReservationDto = new ReservationDto();
        newReservationDto.setVoitureId(1L);
        newReservationDto.setDateDebut(LocalDate.now().plusDays(1));
        newReservationDto.setDateFin(LocalDate.now().plusDays(3));
        newReservationDto.setMontant(new BigDecimal("150.00"));

        when(authService.getCurrentUser()).thenReturn(testUser);
        when(reservationService.createReservation(any(ReservationDto.class), any(User.class)))
                .thenReturn(testReservationDto);

        // When & Then
        mockMvc.perform(post("/api/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newReservationDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Réservation créée avec succès"))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @DisplayName("Test create reservation with invalid data")
    @WithMockUser(username = "client@example.com", roles = {"CLIENT"})
    void testCreateReservationInvalidData() throws Exception {
        // Given
        ReservationDto invalidReservationDto = new ReservationDto();
        invalidReservationDto.setVoitureId(null); // Invalid: null voiture ID
        invalidReservationDto.setDateDebut(null); // Invalid: null date
        invalidReservationDto.setDateFin(null); // Invalid: null date
        invalidReservationDto.setMontant(new BigDecimal("-10.00")); // Invalid: negative amount

        // When & Then
        mockMvc.perform(post("/api/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidReservationDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Test create reservation unauthorized")
    void testCreateReservationUnauthorized() throws Exception {
        // Given
        ReservationDto newReservationDto = new ReservationDto();
        newReservationDto.setVoitureId(1L);
        newReservationDto.setDateDebut(LocalDate.now().plusDays(1));
        newReservationDto.setDateFin(LocalDate.now().plusDays(3));
        newReservationDto.setMontant(new BigDecimal("150.00"));

        // When & Then
        mockMvc.perform(post("/api/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newReservationDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Test update reservation status")
    @WithMockUser(username = "societe@example.com", roles = {"SOCIETE"})
    void testUpdateReservationStatus() throws Exception {
        // Given
        ReservationDto updatedReservation = new ReservationDto();
        updatedReservation.setId(1L);
        updatedReservation.setStatut(StatutReservation.CONFIRMEE);

        when(authService.getCurrentUser()).thenReturn(societeUser);
        when(reservationService.updateReservationStatus(anyLong(), any(StatutReservation.class), any(User.class)))
                .thenReturn(updatedReservation);

        // When & Then
        mockMvc.perform(put("/api/reservations/1/status")
                .param("status", "CONFIRMEE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Statut de réservation mis à jour"));
    }

    @Test
    @DisplayName("Test confirm reservation")
    @WithMockUser(username = "societe@example.com", roles = {"SOCIETE"})
    void testConfirmReservation() throws Exception {
        // Given
        ReservationDto confirmedReservation = new ReservationDto();
        confirmedReservation.setId(1L);
        confirmedReservation.setStatut(StatutReservation.CONFIRMEE);

        when(authService.getCurrentUser()).thenReturn(societeUser);
        when(reservationService.updateReservationStatus(anyLong(), eq(StatutReservation.CONFIRMEE), any(User.class)))
                .thenReturn(confirmedReservation);

        // When & Then
        mockMvc.perform(put("/api/reservations/1/confirm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Réservation confirmée avec succès"));
    }

    @Test
    @DisplayName("Test cancel reservation")
    @WithMockUser(username = "societe@example.com", roles = {"SOCIETE"})
    void testCancelReservation() throws Exception {
        // Given
        ReservationDto cancelledReservation = new ReservationDto();
        cancelledReservation.setId(1L);
        cancelledReservation.setStatut(StatutReservation.ANNULEE);

        when(authService.getCurrentUser()).thenReturn(societeUser);
        when(reservationService.updateReservationStatus(anyLong(), eq(StatutReservation.ANNULEE), any(User.class)))
                .thenReturn(cancelledReservation);

        // When & Then
        mockMvc.perform(put("/api/reservations/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Réservation annulée avec succès"));
    }

    @Test
    @DisplayName("Test complete reservation")
    @WithMockUser(username = "societe@example.com", roles = {"SOCIETE"})
    void testCompleteReservation() throws Exception {
        // Given
        ReservationDto completedReservation = new ReservationDto();
        completedReservation.setId(1L);
        completedReservation.setStatut(StatutReservation.TERMINEE);

        when(authService.getCurrentUser()).thenReturn(societeUser);
        when(reservationService.updateReservationStatus(anyLong(), eq(StatutReservation.TERMINEE), any(User.class)))
                .thenReturn(completedReservation);

        // When & Then
        mockMvc.perform(put("/api/reservations/1/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Réservation marquée comme terminée"));
    }

    @Test
    @DisplayName("Test update reservation status not found")
    @WithMockUser(username = "societe@example.com", roles = {"SOCIETE"})
    void testUpdateReservationStatusNotFound() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(societeUser);
        when(reservationService.updateReservationStatus(anyLong(), any(StatutReservation.class), any(User.class)))
                .thenThrow(new ResourceNotFoundException("Réservation non trouvée"));

        // When & Then
        mockMvc.perform(put("/api/reservations/999/status")
                .param("status", "CONFIRMEE"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Test delete reservation")
    @WithMockUser(username = "client@example.com", roles = {"CLIENT"})
    void testDeleteReservation() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(testUser);
        doNothing().when(reservationService).deleteReservation(anyLong(), any(User.class));

        // When & Then
        mockMvc.perform(delete("/api/reservations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Réservation supprimée avec succès"));
    }

    @Test
    @DisplayName("Test delete reservation not found")
    @WithMockUser(username = "client@example.com", roles = {"CLIENT"})
    void testDeleteReservationNotFound() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(testUser);

        // When & Then
        mockMvc.perform(delete("/api/reservations/999"))
                .andExpect(status().isNotFound());
    }
}
