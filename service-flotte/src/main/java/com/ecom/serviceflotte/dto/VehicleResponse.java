package com.ecom.serviceflotte.dto;

import com.ecom.serviceflotte.model.VehicleStatus;
import com.ecom.serviceflotte.model.VehicleType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Corps recu par POST /api/vehicles et PUT /api/vehicles/{id}.
 * Le statut reste optionnel : a la creation le service applique DISPONIBLE.
 */
public record VehicleRequest(
        @NotNull(message = "type est obligatoire")
        VehicleType type,

        @NotBlank(message = "immatriculation est obligatoire")
        String immatriculation,

        @Min(value = 0, message = "autonomieKm ne peut pas etre negatif")
        int autonomieKm,

        VehicleStatus statut
) {
}