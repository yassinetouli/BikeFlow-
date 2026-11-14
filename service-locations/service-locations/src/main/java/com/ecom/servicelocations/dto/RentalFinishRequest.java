package com.ecom.servicelocations.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record RentalFinishRequest(
        @NotNull(message = "stationArrive est obligatoire")
        @Positive(message = "stationArrive doit être positif")
        Long stationArrive,

        @NotNull(message = "kmParcourus est obligatoire")
        @PositiveOrZero(message = "kmParcourus ne peut pas être négatif")
        Double kmParcourus
) {
}
