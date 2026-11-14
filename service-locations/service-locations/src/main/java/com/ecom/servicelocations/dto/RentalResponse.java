package com.ecom.servicelocations.dto;

import com.ecom.servicelocations.model.Rental;
import com.ecom.servicelocations.model.RentalStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RentalResponse(
        Long id,
        Long vehicleId,
        String userEmail,
        Long stationDepart,
        Long stationArrive,
        LocalDateTime dateDebut,
        LocalDateTime dateFin,
        Double kmParcourus,
        BigDecimal montant,
        RentalStatus statut
) {
    public static RentalResponse from(Rental r) {
        return new RentalResponse(
                r.getId(), r.getVehicleId(), r.getUserEmail(),
                r.getStationDepart(), r.getStationArrive(),
                r.getDateDebut(), r.getDateFin(),
                r.getKmParcourus(), r.getMontant(), r.getStatut());
    }
}
