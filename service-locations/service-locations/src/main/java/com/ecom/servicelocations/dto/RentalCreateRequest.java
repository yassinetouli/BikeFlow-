package com.ecom.servicelocations.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RentalCreateRequest(
        @NotNull(message = "vehicleId est obligatoire")
        @Positive(message = "vehicleId doit être positif")
        Long vehicleId,

        @NotBlank(message = "userEmail est obligatoire")
        @Email(message = "userEmail doit être une adresse email valide")
        String userEmail,

        @NotNull(message = "stationDepart est obligatoire")
        @Positive(message = "stationDepart doit être positif")
        Long stationDepart
) {
}
