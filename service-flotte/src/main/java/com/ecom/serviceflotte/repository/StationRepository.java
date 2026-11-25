package com.ecom.serviceflotte.repository;

import com.ecom.serviceflotte.model.Station;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acces a la table des stations. JpaRepository fournit deja tout le CRUD
 * (save, findById, findAll, deleteById...), aucune methode supplementaire n'est utile.
 */
public interface StationRepository extends JpaRepository<Station, Long> {
}
