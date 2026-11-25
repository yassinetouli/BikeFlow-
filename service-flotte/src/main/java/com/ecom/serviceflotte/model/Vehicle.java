package com.ecom.serviceflotte.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Vehicule partage (velo ou trottinette) disponible dans la flotte.
 */
@Entity
@Table(name = "vehicles",
        uniqueConstraints = @UniqueConstraint(name = "uk_vehicle_immatriculation", columnNames = "immatriculation"))
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Type de vehicule : VELO ou TROTTINETTE. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VehicleType type;

    /** Identifiant unique du vehicule (comme une immatriculation). */
    @Column(nullable = false, unique = true, length = 30)
    private String immatriculation;

    /** Autonomie restante estimee en kilometres. */
    @Column(nullable = false)
    private int autonomieKm;

    /** Statut actuel : DISPONIBLE, EN_LOCATION ou EN_MAINTENANCE. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VehicleStatus statut;

    public Vehicle() {
        // requis par JPA et par le service qui construit le vehicule depuis le DTO
    }

    public Long getId() {
        return id;
    }

    public VehicleType getType() {
        return type;
    }

    public void setType(VehicleType type) {
        this.type = type;
    }

    public String getImmatriculation() {
        return immatriculation;
    }

    public void setImmatriculation(String immatriculation) {
        this.immatriculation = immatriculation;
    }

    public int getAutonomieKm() {
        return autonomieKm;
    }

    public void setAutonomieKm(int autonomieKm) {
        this.autonomieKm = autonomieKm;
    }

    public VehicleStatus getStatut() {
        return statut;
    }

    public void setStatut(VehicleStatus statut) {
        this.statut = statut;
    }
}
