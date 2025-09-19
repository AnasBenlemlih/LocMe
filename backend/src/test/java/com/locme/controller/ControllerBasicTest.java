package com.locme.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.locme.auth.AuthService;
import com.locme.auth.JwtService;
import com.locme.auth.User;
import com.locme.auth.Role;
import com.locme.common.exceptions.ResourceNotFoundException;
import com.locme.config.NoSecurityTestConfig;
import com.locme.voiture.VoitureService;
import com.locme.voiture.dto.VoitureDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test de base pour vérifier que les contrôleurs fonctionnent correctement
 * sans la complexité de la sécurité
 */
@WebMvcTest
@AutoConfigureMockMvc
@Import(NoSecurityTestConfig.class)
class ControllerBasicTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VoitureService voitureService;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private VoitureDto testVoitureDto;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setNom("Test User");
        testUser.setEmail("test@example.com");
        testUser.setRole(Role.SOCIETE);

        testVoitureDto = new VoitureDto();
        testVoitureDto.setId(1L);
        testVoitureDto.setMarque("Toyota");
        testVoitureDto.setModele("Corolla");
        testVoitureDto.setPrixParJour(new BigDecimal("50.00"));
        testVoitureDto.setDisponible(true);
    }

    @Test
    @DisplayName("Test basic controller functionality - GET all voitures")
    void testGetAllVoitures() throws Exception {
        // Given
        List<VoitureDto> voitures = Arrays.asList(testVoitureDto);
        when(voitureService.getAllVoitures()).thenReturn(voitures);

        // When & Then
        mockMvc.perform(get("/api/voitures"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].marque").value("Toyota"));
    }

    @Test
    @DisplayName("Test basic controller functionality - GET voiture by ID")
    void testGetVoitureById() throws Exception {
        // Given
        when(voitureService.getVoitureById(1L)).thenReturn(testVoitureDto);

        // When & Then
        mockMvc.perform(get("/api/voitures/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.marque").value("Toyota"));
    }

    @Test
    @DisplayName("Test basic controller functionality - GET voiture not found")
    void testGetVoitureNotFound() throws Exception {
        // Given
        when(voitureService.getVoitureById(999L))
                .thenThrow(new ResourceNotFoundException("Voiture non trouvée"));

        // When & Then
        mockMvc.perform(get("/api/voitures/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Test basic controller functionality - UPDATE voiture not found")
    void testUpdateVoitureNotFound() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(testUser);
        when(voitureService.updateVoiture(anyLong(), any(VoitureDto.class), any(User.class)))
                .thenThrow(new ResourceNotFoundException("Voiture non trouvée"));

        // When & Then
        mockMvc.perform(put("/api/voitures/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testVoitureDto)))
                .andExpect(status().isNotFound());
    }
}
