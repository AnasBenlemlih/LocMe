package com.locme.voiture;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.locme.auth.AuthService;
import com.locme.auth.JwtService;
import com.locme.auth.User;
import com.locme.auth.Role;
import com.locme.common.exceptions.ResourceNotFoundException;
import com.locme.config.NoSecurityTestConfig;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VoitureController.class)
@AutoConfigureMockMvc
@Import(NoSecurityTestConfig.class)
class VoitureControllerSimpleTest {

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
        testVoitureDto.setMarque("Toyota");
        testVoitureDto.setModele("Corolla");
        testVoitureDto.setPrixParJour(new BigDecimal("50.00"));
        testVoitureDto.setDisponible(true);
    }

    @Test
    @DisplayName("Test update voiture not found - should return 404")
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
