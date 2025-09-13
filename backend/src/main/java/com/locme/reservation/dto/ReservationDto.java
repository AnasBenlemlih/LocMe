package com.locme.reservation.dto;

import com.locme.reservation.StatutReservation;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ReservationDto {
    private Long id;
    private Long voitureId;
    private String voitureMarque;
    private String voitureModele;
    private Long userId;
    private String userNom;

    @NotNull
    private LocalDate dateDebut;

    @NotNull
    private LocalDate dateFin;

    private StatutReservation statut;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal montant;

    private String commentaires;
    private String lieuPrise;
    private String lieuRetour;

    public ReservationDto() {}

    public ReservationDto(Long id, Long voitureId, String voitureMarque, String voitureModele, 
                         Long userId, String userNom, LocalDate dateDebut, LocalDate dateFin, 
                         StatutReservation statut, BigDecimal montant) {
        this.id = id;
        this.voitureId = voitureId;
        this.voitureMarque = voitureMarque;
        this.voitureModele = voitureModele;
        this.userId = userId;
        this.userNom = userNom;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.statut = statut;
        this.montant = montant;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVoitureId() {
        return voitureId;
    }

    public void setVoitureId(Long voitureId) {
        this.voitureId = voitureId;
    }

    public String getVoitureMarque() {
        return voitureMarque;
    }

    public void setVoitureMarque(String voitureMarque) {
        this.voitureMarque = voitureMarque;
    }

    public String getVoitureModele() {
        return voitureModele;
    }

    public void setVoitureModele(String voitureModele) {
        this.voitureModele = voitureModele;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserNom() {
        return userNom;
    }

    public void setUserNom(String userNom) {
        this.userNom = userNom;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    public StatutReservation getStatut() {
        return statut;
    }

    public void setStatut(StatutReservation statut) {
        this.statut = statut;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public String getCommentaires() {
        return commentaires;
    }

    public void setCommentaires(String commentaires) {
        this.commentaires = commentaires;
    }

    public String getLieuPrise() {
        return lieuPrise;
    }

    public void setLieuPrise(String lieuPrise) {
        this.lieuPrise = lieuPrise;
    }

    public String getLieuRetour() {
        return lieuRetour;
    }

    public void setLieuRetour(String lieuRetour) {
        this.lieuRetour = lieuRetour;
    }
}
