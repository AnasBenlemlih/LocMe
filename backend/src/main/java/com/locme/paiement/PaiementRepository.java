package com.locme.paiement;

import com.locme.reservation.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaiementRepository extends JpaRepository<Paiement, Long> {
    Optional<Paiement> findByReservation(Reservation reservation);
    Optional<Paiement> findByStripePaymentIntentId(String stripePaymentIntentId);
    Optional<Paiement> findByTransactionId(String transactionId);
}
