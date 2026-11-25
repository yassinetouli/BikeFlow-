package com.ecom.serviceflotte.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Station de prise en charge et de retour des vehicules.
 * Les controles de saisie sont faits dans le DTO (StationRequest) et les
 * regles metier dans StationService, comme dans le service-locations.
 */
@Entity
@Table(name = "stations")
public class Station {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 255)
    private String adresse;

    @Column(nullable = false)
    private int capacite;

    @Column(nullable = false)
    private int placesLibres;

    public Station() {
        // requis par JPA et par le service qui construit la station depuis le DTO
    }

    public Long getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public int getCapacite() {
        return capacite;
    }

    public void setCapacite(int capacite) {
        this.capacite = capacite;
    }

    public int getPlacesLibres() {
        return placesLibres;
    }

    public void setPlacesLibres(int placesLibres) {
        this.placesLibres = placesLibres;
    }
}
