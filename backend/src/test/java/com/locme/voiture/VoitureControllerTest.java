package com.locme.voiture;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.locme.auth.AuthService;
import com.locme.auth.JwtService;
import com.locme.auth.User;
import com.locme.auth.Role;
import com.locme.common.ApiResponse;
import com.locme.common.exceptions.ResourceNotFoundException;
import com.locme.config.NoSecurityTestConfig;
import com.locme.societe.Societe;
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

@WebMvcTest(VoitureController.class)
@AutoConfigureMockMvc
@Import(NoSecurityTestConfig.class)
class VoitureControllerTest {

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

    private VoitureDto testVoitureDto;
    private User testUser;
    private Societe testSociete;

    @BeforeEach
    void setUp() {
        testSociete = new Societe();
        testSociete.setId(1L);
        testSociete.setNom("Test Societe");

        testUser = new User();
        testUser.setId(1L);
        testUser.setNom("Test User");
        testUser.setEmail("test@example.com");
        testUser.setRole(Role.SOCIETE);

        testVoitureDto = new VoitureDto();
        testVoitureDto.setId(1L);
        testVoitureDto.setMarque("Toyota");
        testVoitureDto.setModele("Camry");
        testVoitureDto.setPrixParJour(new BigDecimal("50.00"));
        testVoitureDto.setDisponible(true);
        testVoitureDto.setSocieteId(1L);
        testVoitureDto.setSocieteNom("Test Societe");
    }

    @Test
    @DisplayName("Test get all voitures")
    void testGetAllVoitures() throws Exception {
        // Given
        List<VoitureDto> voitures = Arrays.asList(testVoitureDto);
        when(voitureService.getAllVoitures()).thenReturn(voitures);

        // When & Then
        mockMvc.perform(get("/api/voitures"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].marque").value("Toyota"))
                .andExpect(jsonPath("$.data[0].modele").value("Camry"));
    }

    @Test
    @DisplayName("Test get available voitures")
    void testGetAvailableVoitures() throws Exception {
        // Given
        List<VoitureDto> voitures = Arrays.asList(testVoitureDto);
        when(voitureService.getAvailableVoitures()).thenReturn(voitures);

        // When & Then
        mockMvc.perform(get("/api/voitures/disponibles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].disponible").value(true));
    }

    @Test
    @DisplayName("Test get available voitures with date filters")
    void testGetAvailableVoituresWithDateFilters() throws Exception {
        // Given
        LocalDate dateDebut = LocalDate.now().plusDays(1);
        LocalDate dateFin = LocalDate.now().plusDays(3);
        List<VoitureDto> voitures = Arrays.asList(testVoitureDto);
        
        when(voitureService.getAvailableVoituresByDate(dateDebut, dateFin)).thenReturn(voitures);

        // When & Then
        mockMvc.perform(get("/api/voitures/disponibles")
                .param("dateDebut", dateDebut.toString())
                .param("dateFin", dateFin.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("Test get available voitures with filters")
    void testGetAvailableVoituresWithFilters() throws Exception {
        // Given
        String marque = "Toyota";
        BigDecimal prixMin = new BigDecimal("30.00");
        BigDecimal prixMax = new BigDecimal("100.00");
        List<VoitureDto> voitures = Arrays.asList(testVoitureDto);
        
        when(voitureService.getAvailableVoituresWithFilters(marque, prixMin, prixMax)).thenReturn(voitures);

        // When & Then
        mockMvc.perform(get("/api/voitures/disponibles")
                .param("marque", marque)
                .param("prixMin", prixMin.toString())
                .param("prixMax", prixMax.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("Test get voiture by ID")
    void testGetVoitureById() throws Exception {
        // Given
        when(voitureService.getVoitureById(1L)).thenReturn(testVoitureDto);

        // When & Then
        mockMvc.perform(get("/api/voitures/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.marque").value("Toyota"));
    }

    @Test
    @DisplayName("Test get voiture by ID not found")
    void testGetVoitureByIdNotFound() throws Exception {
        // Given
        when(voitureService.getVoitureById(999L))
                .thenThrow(new ResourceNotFoundException("Voiture non trouvée"));

        // When & Then
        mockMvc.perform(get("/api/voitures/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Test get voitures by societe")
    @WithMockUser(username = "test@example.com", roles = {"SOCIETE"})
    void testGetVoituresBySociete() throws Exception {
        // Given
        List<VoitureDto> voitures = Arrays.asList(testVoitureDto);
        when(authService.getCurrentUser()).thenReturn(testUser);
        when(voitureService.getVoituresBySociete(testUser)).thenReturn(voitures);

        // When & Then
        mockMvc.perform(get("/api/voitures/societe/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("Test create voiture")
    @WithMockUser(username = "test@example.com", roles = {"SOCIETE"})
    void testCreateVoiture() throws Exception {
        // Given
        VoitureDto newVoitureDto = new VoitureDto();
        newVoitureDto.setMarque("Honda");
        newVoitureDto.setModele("Civic");
        newVoitureDto.setPrixParJour(new BigDecimal("45.00"));
        newVoitureDto.setDisponible(true);

        VoitureDto createdVoiture = new VoitureDto();
        createdVoiture.setId(2L);
        createdVoiture.setMarque("Honda");
        createdVoiture.setModele("Civic");
        createdVoiture.setPrixParJour(new BigDecimal("45.00"));
        createdVoiture.setDisponible(true);

        when(authService.getCurrentUser()).thenReturn(testUser);
        when(voitureService.createVoiture(any(VoitureDto.class), any(User.class))).thenReturn(createdVoiture);

        // When & Then
        mockMvc.perform(post("/api/voitures")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newVoitureDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Voiture créée avec succès"))
                .andExpect(jsonPath("$.data.marque").value("Honda"));
    }

    @Test
    @DisplayName("Test create voiture with invalid data")
    @WithMockUser(username = "test@example.com", roles = {"SOCIETE"})
    void testCreateVoitureInvalidData() throws Exception {
        // Given
        VoitureDto invalidVoitureDto = new VoitureDto();
        invalidVoitureDto.setMarque(""); // Invalid: empty marque
        invalidVoitureDto.setModele(""); // Invalid: empty modele
        invalidVoitureDto.setPrixParJour(new BigDecimal("-10.00")); // Invalid: negative price

        // When & Then
        mockMvc.perform(post("/api/voitures")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidVoitureDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Test update voiture")
    @WithMockUser(username = "test@example.com", roles = {"SOCIETE"})
    void testUpdateVoiture() throws Exception {
        // Given
        VoitureDto updatedVoitureDto = new VoitureDto();
        updatedVoitureDto.setMarque("Toyota Updated");
        updatedVoitureDto.setModele("Camry Updated");
        updatedVoitureDto.setPrixParJour(new BigDecimal("60.00"));

        VoitureDto updatedVoiture = new VoitureDto();
        updatedVoiture.setId(1L);
        updatedVoiture.setMarque("Toyota Updated");
        updatedVoiture.setModele("Camry Updated");
        updatedVoiture.setPrixParJour(new BigDecimal("60.00"));

        when(authService.getCurrentUser()).thenReturn(testUser);
        when(voitureService.updateVoiture(anyLong(), any(VoitureDto.class), any(User.class))).thenReturn(updatedVoiture);

        // When & Then
        mockMvc.perform(put("/api/voitures/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedVoitureDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Voiture mise à jour avec succès"))
                .andExpect(jsonPath("$.data.marque").value("Toyota Updated"));
    }

    @Test
    @DisplayName("Test update voiture not found")
    @WithMockUser(username = "test@example.com", roles = {"SOCIETE"})
    void testUpdateVoitureNotFound() throws Exception {
        // Given
        VoitureDto updatedVoitureDto = new VoitureDto();
        updatedVoitureDto.setMarque("Toyota Updated");

        when(authService.getCurrentUser()).thenReturn(testUser);
        when(voitureService.updateVoiture(anyLong(), any(VoitureDto.class), any(User.class)))
                .thenThrow(new ResourceNotFoundException("Voiture non trouvée"));

        // When & Then
        mockMvc.perform(put("/api/voitures/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedVoitureDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Test delete voiture")
    @WithMockUser(username = "test@example.com", roles = {"SOCIETE"})
    void testDeleteVoiture() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(testUser);
        doNothing().when(voitureService).deleteVoiture(anyLong(), any(User.class));

        // When & Then
        mockMvc.perform(delete("/api/voitures/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Voiture supprimée avec succès"));
    }

    @Test
    @DisplayName("Test delete voiture not found")
    @WithMockUser(username = "test@example.com", roles = {"SOCIETE"})
    void testDeleteVoitureNotFound() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(testUser);
        doThrow(new ResourceNotFoundException("Voiture non trouvée"))
                .when(voitureService).deleteVoiture(anyLong(), any(User.class));

        // When & Then
        mockMvc.perform(delete("/api/voitures/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Test unauthorized access to create voiture")
    void testCreateVoitureUnauthorized() throws Exception {
        // Given
        VoitureDto newVoitureDto = new VoitureDto();
        newVoitureDto.setMarque("Honda");
        newVoitureDto.setModele("Civic");
        newVoitureDto.setPrixParJour(new BigDecimal("45.00"));

        // When & Then
        mockMvc.perform(post("/api/voitures")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newVoitureDto)))
                .andExpect(status().isUnauthorized());
    }
}
