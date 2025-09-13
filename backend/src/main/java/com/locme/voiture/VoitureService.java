package com.locme.voiture;

import com.locme.auth.User;
import com.locme.common.exceptions.ResourceNotFoundException;
import com.locme.societe.Societe;
import com.locme.societe.SocieteRepository;
import com.locme.voiture.dto.VoitureDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VoitureService {

    @Autowired
    private VoitureRepository voitureRepository;

    @Autowired
    private SocieteRepository societeRepository;

    public List<VoitureDto> getAllVoitures() {
        return voitureRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<VoitureDto> getAvailableVoitures() {
        return voitureRepository.findByDisponibleTrue().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<VoitureDto> getAvailableVoituresByDate(LocalDate dateDebut, LocalDate dateFin) {
        return voitureRepository.findAvailableCars(dateDebut, dateFin).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<VoitureDto> getAvailableVoituresWithFilters(String marque, BigDecimal prixMin, BigDecimal prixMax) {
        return voitureRepository.findAvailableCarsWithFilters(marque, prixMin, prixMax).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public VoitureDto getVoitureById(Long id) {
        Voiture voiture = voitureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voiture", id));
        return convertToDto(voiture);
    }

    public VoitureDto createVoiture(VoitureDto voitureDto, User currentUser) {
        Societe societe = societeRepository.findByUser(currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Société non trouvée pour cet utilisateur"));

        Voiture voiture = new Voiture();
        voiture.setMarque(voitureDto.getMarque());
        voiture.setModele(voitureDto.getModele());
        voiture.setPrixParJour(voitureDto.getPrixParJour());
        voiture.setDisponible(voitureDto.getDisponible());
        voiture.setAnnee(voitureDto.getAnnee());
        voiture.setKilometrage(voitureDto.getKilometrage());
        voiture.setCarburant(voitureDto.getCarburant());
        voiture.setTransmission(voitureDto.getTransmission());
        voiture.setNombrePlaces(voitureDto.getNombrePlaces());
        voiture.setImageUrl(voitureDto.getImageUrl());
        voiture.setDescription(voitureDto.getDescription());
        voiture.setSociete(societe);

        Voiture savedVoiture = voitureRepository.save(voiture);
        return convertToDto(savedVoiture);
    }

    public VoitureDto updateVoiture(Long id, VoitureDto voitureDto, User currentUser) {
        Voiture voiture = voitureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voiture", id));

        // Vérifier que l'utilisateur est propriétaire de la société
        Societe societe = societeRepository.findByUser(currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Société non trouvée"));
        
        if (!voiture.getSociete().getId().equals(societe.getId())) {
            throw new ResourceNotFoundException("Vous n'êtes pas autorisé à modifier cette voiture");
        }

        voiture.setMarque(voitureDto.getMarque());
        voiture.setModele(voitureDto.getModele());
        voiture.setPrixParJour(voitureDto.getPrixParJour());
        voiture.setDisponible(voitureDto.getDisponible());
        voiture.setAnnee(voitureDto.getAnnee());
        voiture.setKilometrage(voitureDto.getKilometrage());
        voiture.setCarburant(voitureDto.getCarburant());
        voiture.setTransmission(voitureDto.getTransmission());
        voiture.setNombrePlaces(voitureDto.getNombrePlaces());
        voiture.setImageUrl(voitureDto.getImageUrl());
        voiture.setDescription(voitureDto.getDescription());

        Voiture updatedVoiture = voitureRepository.save(voiture);
        return convertToDto(updatedVoiture);
    }

    public void deleteVoiture(Long id, User currentUser) {
        Voiture voiture = voitureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voiture", id));

        Societe societe = societeRepository.findByUser(currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Société non trouvée"));
        
        if (!voiture.getSociete().getId().equals(societe.getId())) {
            throw new ResourceNotFoundException("Vous n'êtes pas autorisé à supprimer cette voiture");
        }

        voitureRepository.delete(voiture);
    }

    private VoitureDto convertToDto(Voiture voiture) {
        return new VoitureDto(
                voiture.getId(),
                voiture.getMarque(),
                voiture.getModele(),
                voiture.getPrixParJour(),
                voiture.getDisponible(),
                voiture.getSociete().getId(),
                voiture.getSociete().getNom()
        );
    }
}
