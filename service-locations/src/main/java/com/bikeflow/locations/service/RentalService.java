package com.bikeflow.locations.service;

import com.bikeflow.locations.dto.RentalCreateRequest;
import com.bikeflow.locations.dto.RentalFinishRequest;
import com.bikeflow.locations.dto.RentalResponse;
import com.bikeflow.locations.dto.RentalStatusUpdateRequest;
import com.bikeflow.locations.exception.BusinessException;
import com.bikeflow.locations.exception.ResourceNotFoundException;
import com.bikeflow.locations.model.Rental;
import com.bikeflow.locations.model.RentalStatus;
import com.bikeflow.locations.repository.RentalRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@Transactional
public class RentalService {

    /** Statuts pour lesquels le véhicule est considéré comme occupé (RG-04). */
    private static final Set<RentalStatus> ACTIVE = EnumSet.of(
            RentalStatus.RESERVEE, RentalStatus.EN_COURS, RentalStatus.EN_RETARD);

    private final RentalRepository repository;
    private final BigDecimal tarifParKm;

    public RentalService(RentalRepository repository,
                         @Value("${rental.tarif-par-km:2.00}") BigDecimal tarifParKm) {
        this.repository = repository;
        this.tarifParKm = tarifParKm;
    }

    /** BF-07 : réserver() -> location au statut RESERVEE. */
    public RentalResponse reserver(RentalCreateRequest request) {
        if (repository.existsByVehicleIdAndStatutIn(request.vehicleId(), ACTIVE)) {
            throw new BusinessException(
                    "Le véhicule " + request.vehicleId() + " est déjà associé à une location active");
        }
        Rental rental = Rental.reserver(request.vehicleId(), normalize(request.userEmail()), request.stationDepart());
        return RentalResponse.from(repository.save(rental));
    }

    /** BF-13 : consultation d'une location. */
    @Transactional(readOnly = true)
    public RentalResponse getById(Long id) {
        return RentalResponse.from(findOrThrow(id));
    }

    /** BF-13 : historique des locations d'un utilisateur (identifié par son email). */
    @Transactional(readOnly = true)
    public List<RentalResponse> getByUser(String userEmail) {
        return repository.findByUserEmailOrderByIdDesc(normalize(userEmail)).stream()
                .map(RentalResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RentalResponse> getAll() {
        return repository.findAll().stream().map(RentalResponse::from).toList();
    }

    /** BF-09 : demarrer() -> RESERVEE -> EN_COURS. */
    public RentalResponse demarrer(Long id) {
        Rental rental = findOrThrow(id);
        rental.demarrer();
        return RentalResponse.from(repository.save(rental));
    }

    /** BF-10 : terminer() -> TERMINEE, avec calcul du montant (RG-09). */
    public RentalResponse terminer(Long id, RentalFinishRequest request) {
        Rental rental = findOrThrow(id);
        rental.terminer(request.stationArrive(), request.kmParcourus(), computeAmount(request.kmParcourus()));
        return RentalResponse.from(repository.save(rental));
    }

    /**
     * Mise à jour manuelle du statut (PATCH). Transitions autorisées :
     * RESERVEE -> EN_COURS, EN_COURS -> EN_RETARD.
     * TERMINEE doit passer par /finish (stationArrive + km obligatoires).
     */
    public RentalResponse updateStatus(Long id, RentalStatusUpdateRequest request) {
        Rental rental = findOrThrow(id);
        RentalStatus target = request.statut();
        switch (target) {
            case EN_COURS -> rental.demarrer();
            case EN_RETARD -> rental.marquerEnRetard();
            case TERMINEE -> throw new BusinessException(
                    "Utiliser POST /api/rentals/{id}/finish pour terminer une location");
            default -> throw new BusinessException(
                    "Transition interdite : " + rental.getStatut() + " -> " + target);
        }
        return RentalResponse.from(repository.save(rental));
    }

    /** Grille tarifaire simple : km parcourus x tarif par km (à valider : point ouvert du cahier des charges). */
    private BigDecimal computeAmount(Double km) {
        return tarifParKm.multiply(BigDecimal.valueOf(km)).setScale(2, RoundingMode.HALF_UP);
    }

    private String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private Rental findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location introuvable : id=" + id));
    }
}
