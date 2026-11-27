package com.ecom.serviceflotte.dto;

import com.ecom.serviceflotte.model.Station;

/**
 * Reponse JSON des endpoints stations. Le contrat (id, nom, adresse, capacite,
 * placesLibres) reste identique a celui de la collection Postman service-flotte.
 */
public record StationResponse(
        Long id,
        String nom,
        String adresse,
        int capacite,
        int placesLibres
) {
    public static StationResponse from(Station station) {
        return new StationResponse(
                station.getId(), station.getNom(), station.getAdresse(),
                station.getCapacite(), station.getPlacesLibres());
    }
}