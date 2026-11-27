package com.ecom.serviceflotte.dto;

import com.ecom.serviceflotte.model.Vehicle;
import com.ecom.serviceflotte.model.VehicleStatus;
import com.ecom.serviceflotte.model.VehicleType;

/**
 * Reponse JSON des endpoints vehicules. Le contrat (id, type, immatriculation,
 * autonomieKm, statut) reste identique a celui de la collection Postman
 * service-flotte et a FlotteClient du service-utilisateurs.
 */
public record VehicleResponse(
        Long id,
        VehicleType type,
        String immatriculation,
        int autonomieKm,
        VehicleStatus statut
) {
    public static VehicleResponse from(Vehicle vehicle) {
        return new VehicleResponse(
                vehicle.getId(), vehicle.getType(), vehicle.getImmatriculation(),
                vehicle.getAutonomieKm(), vehicle.getStatut());
    }
}
