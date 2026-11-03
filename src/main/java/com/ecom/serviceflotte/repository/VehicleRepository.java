package com.ecom.serviceflotte.repository;

import java.util.Optional;

import com.ecom.serviceflotte.entity.Vehicle;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acces a la table des vehicules. Spring Data JPA genere automatiquement
 * le code SQL des methodes declarees ici (dont findByImmatriculation).
 */
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    /** Retrouve un vehicule par son immatriculation (retourne vide si absent). */
    Optional<Vehicle> findByImmatriculation(String immatriculation);

    /** Repond true si une immatriculation est deja utilisee par un autre vehicule. */
    boolean existsByImmatriculation(String immatriculation);
}
