package com.locme.voiture;

import com.locme.auth.User;
import com.locme.auth.Role;
import com.locme.common.exceptions.BusinessException;
import com.locme.common.exceptions.ResourceNotFoundException;
import com.locme.societe.Societe;
import com.locme.societe.SocieteRepository;
import com.locme.voiture.dto.VoitureDto;
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
class VoitureServiceTest {

    @Mock
    private VoitureRepository voitureRepository;

    @Mock
    private SocieteRepository societeRepository;

    @InjectMocks
    private VoitureService voitureService;

    private Voiture testVoiture;
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

        testVoiture = new Voiture();
        testVoiture.setId(1L);
        testVoiture.setMarque("Toyota");
        testVoiture.setModele("Camry");
        testVoiture.setPrixParJour(new BigDecimal("50.00"));
        testVoiture.setDisponible(true);
        testVoiture.setSociete(testSociete);

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
    void testGetAllVoitures() {
        // Given
        List<Voiture> voitures = Arrays.asList(testVoiture);
        when(voitureRepository.findAll()).thenReturn(voitures);

        // When
        List<VoitureDto> result = voitureService.getAllVoitures();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Toyota", result.get(0).getMarque());
        assertEquals("Camry", result.get(0).getModele());
        verify(voitureRepository).findAll();
    }

    @Test
    @DisplayName("Test get available voitures")
    void testGetAvailableVoitures() {
        // Given
        List<Voiture> voitures = Arrays.asList(testVoiture);
        when(voitureRepository.findByDisponibleTrue()).thenReturn(voitures);

        // When
        List<VoitureDto> result = voitureService.getAvailableVoitures();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getDisponible());
        verify(voitureRepository).findByDisponibleTrue();
    }

    @Test
    @DisplayName("Test get available voitures by date")
    void testGetAvailableVoituresByDate() {
        // Given
        LocalDate dateDebut = LocalDate.now().plusDays(1);
        LocalDate dateFin = LocalDate.now().plusDays(3);
        List<Voiture> voitures = Arrays.asList(testVoiture);
        
        when(voitureRepository.findAvailableVoituresByDate(dateDebut, dateFin)).thenReturn(voitures);

        // When
        List<VoitureDto> result = voitureService.getAvailableVoituresByDate(dateDebut, dateFin);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(voitureRepository).findAvailableVoituresByDate(dateDebut, dateFin);
    }

    @Test
    @DisplayName("Test get available voitures with filters")
    void testGetAvailableVoituresWithFilters() {
        // Given
        String marque = "Toyota";
        BigDecimal prixMin = new BigDecimal("30.00");
        BigDecimal prixMax = new BigDecimal("100.00");
        List<Voiture> voitures = Arrays.asList(testVoiture);
        
        when(voitureRepository.findAvailableVoituresWithFilters(marque, prixMin, prixMax)).thenReturn(voitures);

        // When
        List<VoitureDto> result = voitureService.getAvailableVoituresWithFilters(marque, prixMin, prixMax);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(voitureRepository).findAvailableVoituresWithFilters(marque, prixMin, prixMax);
    }

    @Test
    @DisplayName("Test get voiture by ID")
    void testGetVoitureById() {
        // Given
        when(voitureRepository.findById(1L)).thenReturn(Optional.of(testVoiture));

        // When
        VoitureDto result = voitureService.getVoitureById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Toyota", result.getMarque());
        assertEquals("Camry", result.getModele());
        verify(voitureRepository).findById(1L);
    }

    @Test
    @DisplayName("Test get voiture by ID not found")
    void testGetVoitureByIdNotFound() {
        // Given
        when(voitureRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            voitureService.getVoitureById(999L);
        });

        assertEquals("Voiture non trouvée", exception.getMessage());
        verify(voitureRepository).findById(999L);
    }

    @Test
    @DisplayName("Test get voitures by societe")
    void testGetVoituresBySociete() {
        // Given
        List<Voiture> voitures = Arrays.asList(testVoiture);
        when(voitureRepository.findBySocieteUser(testUser)).thenReturn(voitures);

        // When
        List<VoitureDto> result = voitureService.getVoituresBySociete(testUser);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(voitureRepository).findBySocieteUser(testUser);
    }

    @Test
    @DisplayName("Test create voiture")
    void testCreateVoiture() {
        // Given
        VoitureDto newVoitureDto = new VoitureDto();
        newVoitureDto.setMarque("Honda");
        newVoitureDto.setModele("Civic");
        newVoitureDto.setPrixParJour(new BigDecimal("45.00"));
        newVoitureDto.setDisponible(true);

        when(societeRepository.findByUser(testUser)).thenReturn(Optional.of(testSociete));
        when(voitureRepository.save(any(Voiture.class))).thenReturn(testVoiture);

        // When
        VoitureDto result = voitureService.createVoiture(newVoitureDto, testUser);

        // Then
        assertNotNull(result);
        verify(societeRepository).findByUser(testUser);
        verify(voitureRepository).save(any(Voiture.class));
    }

    @Test
    @DisplayName("Test create voiture when societe not found")
    void testCreateVoitureSocieteNotFound() {
        // Given
        VoitureDto newVoitureDto = new VoitureDto();
        newVoitureDto.setMarque("Honda");
        newVoitureDto.setModele("Civic");
        newVoitureDto.setPrixParJour(new BigDecimal("45.00"));

        when(societeRepository.findByUser(testUser)).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            voitureService.createVoiture(newVoitureDto, testUser);
        });

        assertEquals("Société non trouvée pour cet utilisateur", exception.getMessage());
        verify(societeRepository).findByUser(testUser);
        verify(voitureRepository, never()).save(any(Voiture.class));
    }

    @Test
    @DisplayName("Test update voiture")
    void testUpdateVoiture() {
        // Given
        VoitureDto updatedVoitureDto = new VoitureDto();
        updatedVoitureDto.setMarque("Toyota Updated");
        updatedVoitureDto.setModele("Camry Updated");
        updatedVoitureDto.setPrixParJour(new BigDecimal("60.00"));

        when(voitureRepository.findById(1L)).thenReturn(Optional.of(testVoiture));
        when(voitureRepository.save(any(Voiture.class))).thenReturn(testVoiture);

        // When
        VoitureDto result = voitureService.updateVoiture(1L, updatedVoitureDto, testUser);

        // Then
        assertNotNull(result);
        verify(voitureRepository).findById(1L);
        verify(voitureRepository).save(any(Voiture.class));
    }

    @Test
    @DisplayName("Test update voiture not found")
    void testUpdateVoitureNotFound() {
        // Given
        VoitureDto updatedVoitureDto = new VoitureDto();
        updatedVoitureDto.setMarque("Toyota Updated");

        when(voitureRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            voitureService.updateVoiture(999L, updatedVoitureDto, testUser);
        });

        assertEquals("Voiture non trouvée", exception.getMessage());
        verify(voitureRepository).findById(999L);
        verify(voitureRepository, never()).save(any(Voiture.class));
    }

    @Test
    @DisplayName("Test update voiture unauthorized")
    void testUpdateVoitureUnauthorized() {
        // Given
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setRole(Role.SOCIETE);

        VoitureDto updatedVoitureDto = new VoitureDto();
        updatedVoitureDto.setMarque("Toyota Updated");

        when(voitureRepository.findById(1L)).thenReturn(Optional.of(testVoiture));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            voitureService.updateVoiture(1L, updatedVoitureDto, otherUser);
        });

        assertEquals("Vous n'êtes pas autorisé à modifier cette voiture", exception.getMessage());
        verify(voitureRepository).findById(1L);
        verify(voitureRepository, never()).save(any(Voiture.class));
    }

    @Test
    @DisplayName("Test delete voiture")
    void testDeleteVoiture() {
        // Given
        when(voitureRepository.findById(1L)).thenReturn(Optional.of(testVoiture));

        // When
        voitureService.deleteVoiture(1L, testUser);

        // Then
        verify(voitureRepository).findById(1L);
        verify(voitureRepository).delete(testVoiture);
    }

    @Test
    @DisplayName("Test delete voiture not found")
    void testDeleteVoitureNotFound() {
        // Given
        when(voitureRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            voitureService.deleteVoiture(999L, testUser);
        });

        assertEquals("Voiture non trouvée", exception.getMessage());
        verify(voitureRepository).findById(999L);
        verify(voitureRepository, never()).delete(any(Voiture.class));
    }

    @Test
    @DisplayName("Test delete voiture unauthorized")
    void testDeleteVoitureUnauthorized() {
        // Given
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setRole(Role.SOCIETE);

        when(voitureRepository.findById(1L)).thenReturn(Optional.of(testVoiture));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            voitureService.deleteVoiture(1L, otherUser);
        });

        assertEquals("Vous n'êtes pas autorisé à supprimer cette voiture", exception.getMessage());
        verify(voitureRepository).findById(1L);
        verify(voitureRepository, never()).delete(any(Voiture.class));
    }
}
