package com.locme.reservation;

import com.locme.auth.User;
import com.locme.voiture.Voiture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUser(User user);
    List<Reservation> findByVoiture(Voiture voiture);
    List<Reservation> findByStatut(StatutReservation statut);
    
    @Query("SELECT r FROM Reservation r WHERE r.voiture = :voiture AND r.statut IN ('CONFIRMEE', 'EN_COURS') " +
           "AND ((r.dateDebut <= :dateFin AND r.dateFin >= :dateDebut))")
    List<Reservation> findConflictingReservations(@Param("voiture") Voiture voiture, 
                                                  @Param("dateDebut") LocalDate dateDebut, 
                                                  @Param("dateFin") LocalDate dateFin);
    
    @Query("SELECT r FROM Reservation r WHERE r.user = :user AND r.statut = :statut")
    List<Reservation> findByUserAndStatut(@Param("user") User user, @Param("statut") StatutReservation statut);
}
