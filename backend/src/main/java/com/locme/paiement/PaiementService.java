package com.locme.paiement;

import com.locme.common.exceptions.BusinessException;
import com.locme.common.exceptions.ResourceNotFoundException;
import com.locme.reservation.Reservation;
import com.locme.reservation.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PaiementService {

    @Autowired
    private PaiementRepository paiementRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    public Paiement createPaiement(Long reservationId, MethodePaiement methodePaiement) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation non trouvée"));

        // Check if payment already exists for this reservation
        Optional<Paiement> existingPaiement = paiementRepository.findByReservation(reservation);
        if (existingPaiement.isPresent()) {
            throw new BusinessException("Un paiement existe déjà pour cette réservation");
        }

        Paiement paiement = new Paiement();
        paiement.setReservation(reservation);
        paiement.setMontant(reservation.getMontant());
        paiement.setStatut(StatutPaiement.EN_ATTENTE);
        paiement.setMethodePaiement(methodePaiement);

        return paiementRepository.save(paiement);
    }

    public Paiement processPayment(Long paiementId, String paymentIntentId, String transactionId) {
        Paiement paiement = paiementRepository.findById(paiementId)
                .orElseThrow(() -> new ResourceNotFoundException("Paiement non trouvé"));

        paiement.setStatut(StatutPaiement.PAYE);
        paiement.setStripePaymentIntentId(paymentIntentId);
        paiement.setTransactionId(transactionId);
        paiement.setDatePaiement(LocalDateTime.now());

        return paiementRepository.save(paiement);
    }

    public Paiement processPaymentFailure(Long paiementId, String paymentIntentId, String errorMessage) {
        Paiement paiement = paiementRepository.findById(paiementId)
                .orElseThrow(() -> new ResourceNotFoundException("Paiement non trouvé"));

        paiement.setStatut(StatutPaiement.ECHEC);
        paiement.setStripePaymentIntentId(paymentIntentId);

        return paiementRepository.save(paiement);
    }

    public Paiement getPaiementById(Long id) {
        return paiementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paiement non trouvé"));
    }

    public Paiement getPaiementByReservation(Reservation reservation) {
        return paiementRepository.findByReservation(reservation)
                .orElseThrow(() -> new ResourceNotFoundException("Paiement non trouvé pour cette réservation"));
    }

    public Paiement refundPaiement(Long paiementId, BigDecimal amount) {
        Paiement paiement = paiementRepository.findById(paiementId)
                .orElseThrow(() -> new ResourceNotFoundException("Paiement non trouvé"));

        if (paiement.getStatut() != StatutPaiement.PAYE) {
            throw new BusinessException("Seuls les paiements payés peuvent être remboursés");
        }

        paiement.setStatut(StatutPaiement.REMBOURSE);

        return paiementRepository.save(paiement);
    }
}
