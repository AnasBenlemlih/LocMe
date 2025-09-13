package com.locme.voiture.dto;

import com.locme.voiture.TypeCarburant;
import com.locme.voiture.TypeTransmission;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class VoitureDto {
    private Long id;

    @NotBlank
    @Size(max = 50)
    private String marque;

    @NotBlank
    @Size(max = 50)
    private String modele;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal prixParJour;

    private Boolean disponible = true;
    private Integer annee;
    private Long kilometrage;
    private TypeCarburant carburant;
    private TypeTransmission transmission;
    private Integer nombrePlaces;
    private String imageUrl;
    private String description;
    private Long societeId;
    private String societeNom;

    public VoitureDto() {}

    public VoitureDto(Long id, String marque, String modele, BigDecimal prixParJour, Boolean disponible, 
                     Long societeId, String societeNom) {
        this.id = id;
        this.marque = marque;
        this.modele = modele;
        this.prixParJour = prixParJour;
        this.disponible = disponible;
        this.societeId = societeId;
        this.societeNom = societeNom;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMarque() {
        return marque;
    }

    public void setMarque(String marque) {
        this.marque = marque;
    }

    public String getModele() {
        return modele;
    }

    public void setModele(String modele) {
        this.modele = modele;
    }

    public BigDecimal getPrixParJour() {
        return prixParJour;
    }

    public void setPrixParJour(BigDecimal prixParJour) {
        this.prixParJour = prixParJour;
    }

    public Boolean getDisponible() {
        return disponible;
    }

    public void setDisponible(Boolean disponible) {
        this.disponible = disponible;
    }

    public Integer getAnnee() {
        return annee;
    }

    public void setAnnee(Integer annee) {
        this.annee = annee;
    }

    public Long getKilometrage() {
        return kilometrage;
    }

    public void setKilometrage(Long kilometrage) {
        this.kilometrage = kilometrage;
    }

    public TypeCarburant getCarburant() {
        return carburant;
    }

    public void setCarburant(TypeCarburant carburant) {
        this.carburant = carburant;
    }

    public TypeTransmission getTransmission() {
        return transmission;
    }

    public void setTransmission(TypeTransmission transmission) {
        this.transmission = transmission;
    }

    public Integer getNombrePlaces() {
        return nombrePlaces;
    }

    public void setNombrePlaces(Integer nombrePlaces) {
        this.nombrePlaces = nombrePlaces;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getSocieteId() {
        return societeId;
    }

    public void setSocieteId(Long societeId) {
        this.societeId = societeId;
    }

    public String getSocieteNom() {
        return societeNom;
    }

    public void setSocieteNom(String societeNom) {
        this.societeNom = societeNom;
    }
}
