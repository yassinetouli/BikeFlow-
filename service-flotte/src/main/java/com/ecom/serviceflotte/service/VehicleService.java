package com.ecom.serviceflotte.service;

import java.util.List;

import com.ecom.serviceflotte.dto.VehicleRequest;
import com.ecom.serviceflotte.dto.VehicleResponse;
import com.ecom.serviceflotte.exception.BusinessException;
import com.ecom.serviceflotte.exception.ResourceNotFoundException;
import com.ecom.serviceflotte.model.Vehicle;
import com.ecom.serviceflotte.model.VehicleStatus;
import com.ecom.serviceflotte.repository.VehicleRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Logique metier des vehicules : il utilise le repository et leve les erreurs
 * 404 (introuvable) et 409 (immatriculation deja utilisee).
 */
@Service
@Transactional
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    public VehicleService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @Transactional(readOnly = true)
    public List<VehicleResponse> findAll() {
        return vehicleRepository.findAll().stream().map(VehicleResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public VehicleResponse findById(Long id) {
        return VehicleResponse.from(findOrThrow(id));
    }

    public VehicleResponse create(VehicleRequest request) {
        verifierImmatriculationUnique(request.immatriculation(), null);
        Vehicle vehicle = new Vehicle();
        appliquer(vehicle, request);
        // Un nouveau vehicule commence disponible si le client ne precise pas de statut.
        vehicle.setStatut(request.statut() == null ? VehicleStatus.DISPONIBLE : request.statut());
        return VehicleResponse.from(vehicleRepository.save(vehicle));
    }

    public VehicleResponse update(Long id, VehicleRequest request) {
        Vehicle vehicle = findOrThrow(id);
        // On accepte seulement une immatriculation finale qui est libre.
        verifierImmatriculationUnique(request.immatriculation(), vehicle.getImmatriculation());
        appliquer(vehicle, request);
        if (request.statut() != null) {
            vehicle.setStatut(request.statut());
        }
        return VehicleResponse.from(vehicleRepository.save(vehicle));
    }

    public void delete(Long id) {
        if (!vehicleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Vehicule introuvable avec l'id : " + id);
        }
        vehicleRepository.deleteById(id);
    }

    /** Copie les valeurs du DTO vers l'entite (creation et mise a jour). */
    private void appliquer(Vehicle vehicle, VehicleRequest request) {
        vehicle.setType(request.type());
        vehicle.setImmatriculation(request.immatriculation());
        vehicle.setAutonomieKm(request.autonomieKm());
    }

    /**
     * Verifie qu'une immatriculation peut etre utilisee : elle ne doit
     * appartenir a aucun vehicule (ou garder la meme valeur en cas de mise a jour).
     */
    private void verifierImmatriculationUnique(String immatriculation, String valeurActuelle) {
        boolean inchangee = immatriculation != null && immatriculation.equals(valeurActuelle);
        if (!inchangee && vehicleRepository.existsByImmatriculation(immatriculation)) {
            throw new BusinessException(
                    "Immatriculation '" + immatriculation + "' deja utilisee par un autre vehicule");
        }
    }

    private Vehicle findOrThrow(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicule introuvable avec l'id : " + id));
    }
}
