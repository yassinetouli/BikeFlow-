package com.ecom.servicelocations.dto;

import com.ecom.servicelocations.model.RentalStatus;
import jakarta.validation.constraints.NotNull;

public record RentalStatusUpdateRequest(
        @NotNull(message = "statut est obligatoire")
        RentalStatus statut
) {
}
