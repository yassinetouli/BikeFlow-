package com.ecom.serviceflotte.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Station de prise en charge et de retour des vehicules.
 */
@Entity
@Table(name = "stations")
public class Station {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String nom;

    @NotBlank
    private String adresse;

    @Min(1)
    private int capacite;

    @Min(0)
    private int placesLibres;

    public Station() {
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
