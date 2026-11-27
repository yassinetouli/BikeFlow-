package com.ecom.serviceflotte.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Corps recu par POST /api/stations et PUT /api/stations/{id}.
 * Memes controles que service-locations : la validation vit dans le DTO.
 */
public record StationRequest(
        @NotBlank(message = "nom est obligatoire")
        String nom,

        @NotBlank(message = "adresse est obligatoire")
        String adresse,

        @Min(value = 1, message = "capacite doit etre superieure a zero")
        int capacite,

        @Min(value = 0, message = "placesLibres ne peut pas etre negatif")
        int placesLibres
) {
}