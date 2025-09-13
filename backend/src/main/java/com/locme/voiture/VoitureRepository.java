package com.locme.voiture;

import com.locme.societe.Societe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface VoitureRepository extends JpaRepository<Voiture, Long> {
    List<Voiture> findByDisponibleTrue();
    List<Voiture> findBySociete(Societe societe);
    List<Voiture> findBySocieteAndDisponibleTrue(Societe societe);
    
    @Query("SELECT v FROM Voiture v WHERE v.disponible = true AND v.id NOT IN " +
           "(SELECT r.voiture.id FROM Reservation r WHERE r.statut IN ('CONFIRMEE', 'EN_COURS') " +
           "AND ((r.dateDebut <= :dateFin AND r.dateFin >= :dateDebut)))")
    List<Voiture> findAvailableCars(@Param("dateDebut") LocalDate dateDebut, @Param("dateFin") LocalDate dateFin);
    
    @Query("SELECT v FROM Voiture v WHERE v.disponible = true AND " +
           "(:marque IS NULL OR LOWER(v.marque) LIKE LOWER(CONCAT('%', :marque, '%'))) AND " +
           "(:prixMin IS NULL OR v.prixParJour >= :prixMin) AND " +
           "(:prixMax IS NULL OR v.prixParJour <= :prixMax)")
    List<Voiture> findAvailableCarsWithFilters(@Param("marque") String marque, 
                                              @Param("prixMin") BigDecimal prixMin, 
                                              @Param("prixMax") BigDecimal prixMax);
}
