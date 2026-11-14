package com.bikeflow.locations.dto;

import com.bikeflow.locations.model.RentalStatus;
import jakarta.validation.constraints.NotNull;

public record RentalStatusUpdateRequest(
        @NotNull(message = "statut est obligatoire")
        RentalStatus statut
) {
}
