package com.locme.reservation;

import com.locme.auth.User;
import com.locme.common.exceptions.BusinessException;
import com.locme.common.exceptions.ResourceNotFoundException;
import com.locme.reservation.dto.ReservationDto;
import com.locme.voiture.Voiture;
import com.locme.voiture.VoitureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private VoitureRepository voitureRepository;

    public List<ReservationDto> getAllReservations() {
        return reservationRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<ReservationDto> getReservationsByUser(User user) {
        return reservationRepository.findByUser(user).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<ReservationDto> getReservationsBySociete(User societeUser) {
        return reservationRepository.findByVoitureSocieteUser(societeUser).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public ReservationDto getReservationById(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation non trouvée"));
        return convertToDto(reservation);
    }

    public ReservationDto createReservation(ReservationDto reservationDto, User currentUser) {
        Voiture voiture = voitureRepository.findById(reservationDto.getVoitureId())
                .orElseThrow(() -> new ResourceNotFoundException("Voiture non trouvée"));

        // Vérifier que la voiture est disponible
        if (!voiture.getDisponible()) {
            throw new BusinessException("Cette voiture n'est pas disponible");
        }

        // Vérifier les conflits de réservation
        List<Reservation> conflictingReservations = reservationRepository.findConflictingReservations(
                voiture, reservationDto.getDateDebut(), reservationDto.getDateFin());
        
        if (!conflictingReservations.isEmpty()) {
            throw new BusinessException("Cette voiture est déjà réservée pour cette période");
        }

        // Vérifier que les dates sont valides
        if (reservationDto.getDateDebut().isBefore(LocalDate.now())) {
            throw new BusinessException("La date de début doit être dans le futur");
        }

        if (reservationDto.getDateFin().isBefore(reservationDto.getDateDebut())) {
            throw new BusinessException("La date de fin doit être après la date de début");
        }

        // Calculer le montant
        long days = ChronoUnit.DAYS.between(reservationDto.getDateDebut(), reservationDto.getDateFin());
        BigDecimal montant = voiture.getPrixParJour().multiply(BigDecimal.valueOf(days));

        Reservation reservation = new Reservation();
        reservation.setVoiture(voiture);
        reservation.setUser(currentUser);
        reservation.setDateDebut(reservationDto.getDateDebut());
        reservation.setDateFin(reservationDto.getDateFin());
        reservation.setMontant(montant);
        reservation.setCommentaires(reservationDto.getCommentaires());
        reservation.setLieuPrise(reservationDto.getLieuPrise());
        reservation.setLieuRetour(reservationDto.getLieuRetour());
        reservation.setStatut(StatutReservation.EN_ATTENTE);

        Reservation savedReservation = reservationRepository.save(reservation);
        return convertToDto(savedReservation);
    }

    public ReservationDto updateReservationStatus(Long id, StatutReservation newStatus, User currentUser) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation non trouvée"));

        // Vérifier les permissions (propriétaire de la voiture ou admin)
        if (!reservation.getUser().getId().equals(currentUser.getId()) && 
            !currentUser.getRole().name().equals("ADMIN") &&
            !reservation.getVoiture().getSociete().getUser().getId().equals(currentUser.getId())) {
            throw new BusinessException("Vous n'êtes pas autorisé à modifier cette réservation");
        }

        reservation.setStatut(newStatus);
        Reservation updatedReservation = reservationRepository.save(reservation);
        return convertToDto(updatedReservation);
    }

    public void deleteReservation(Long id, User currentUser) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation non trouvée"));

        // Vérifier les permissions
        if (!reservation.getUser().getId().equals(currentUser.getId()) && 
            !currentUser.getRole().name().equals("ADMIN")) {
            throw new BusinessException("Vous n'êtes pas autorisé à supprimer cette réservation");
        }

        reservationRepository.delete(reservation);
    }

    private ReservationDto convertToDto(Reservation reservation) {
        return new ReservationDto(
                reservation.getId(),
                reservation.getVoiture().getId(),
                reservation.getVoiture().getMarque(),
                reservation.getVoiture().getModele(),
                reservation.getUser().getId(),
                reservation.getUser().getNom(),
                reservation.getDateDebut(),
                reservation.getDateFin(),
                reservation.getStatut(),
                reservation.getMontant()
        );
    }
}
