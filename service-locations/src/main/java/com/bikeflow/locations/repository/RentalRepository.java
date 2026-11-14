package com.bikeflow.locations.repository;

import com.bikeflow.locations.model.Rental;
import com.bikeflow.locations.model.RentalStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface RentalRepository extends JpaRepository<Rental, Long> {

    List<Rental> findByUserEmailOrderByIdDesc(String userEmail);

    /** RG-04 : un véhicule ne peut avoir qu'une seule location active à la fois. */
    boolean existsByVehicleIdAndStatutIn(Long vehicleId, Collection<RentalStatus> statuts);
}
