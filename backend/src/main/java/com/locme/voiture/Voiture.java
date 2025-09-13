package com.locme.voiture;

import com.locme.societe.Societe;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "voitures")
public class Voiture {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 50)
    @Column(name = "marque", nullable = false)
    private String marque;

    @NotBlank
    @Size(max = 50)
    @Column(name = "modele", nullable = false)
    private String modele;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Column(name = "prix_par_jour", nullable = false, precision = 10, scale = 2)
    private BigDecimal prixParJour;

    @Column(name = "disponible", nullable = false)
    private Boolean disponible = true;

    @Column(name = "annee")
    private Integer annee;

    @Column(name = "kilometrage")
    private Long kilometrage;

    @Column(name = "carburant")
    @Enumerated(EnumType.STRING)
    private TypeCarburant carburant;

    @Column(name = "transmission")
    @Enumerated(EnumType.STRING)
    private TypeTransmission transmission;

    @Column(name = "nombre_places")
    private Integer nombrePlaces;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "societe_id", nullable = false)
    private Societe societe;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Constructeurs
    public Voiture() {}

    public Voiture(String marque, String modele, BigDecimal prixParJour, Societe societe) {
        this.marque = marque;
        this.modele = modele;
        this.prixParJour = prixParJour;
        this.societe = societe;
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

    public Societe getSociete() {
        return societe;
    }

    public void setSociete(Societe societe) {
        this.societe = societe;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
