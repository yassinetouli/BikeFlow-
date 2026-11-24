package com.ecom.serviceflotte.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

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
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VehicleType type;

    /** Identifiant unique du vehicule (comme une immatriculation). */
    @NotBlank
    @Column(nullable = false, unique = true, length = 30)
    private String immatriculation;

    /** Autonomie restante estimee en kilometres. */
    @Min(0)
    private int autonomieKm;

    /** Statut actuel : DISPONIBLE, EN_LOCATION ou EN_MAINTENANCE. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VehicleStatus statut;

    public Vehicle() {
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
