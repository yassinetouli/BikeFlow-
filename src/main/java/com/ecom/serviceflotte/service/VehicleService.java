package com.ecom.serviceflotte.service;

import java.util.List;

import com.ecom.serviceflotte.entity.Vehicle;
import com.ecom.serviceflotte.entity.VehicleStatus;
import com.ecom.serviceflotte.repository.VehicleRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Logique metier des vehicules : il utilise le repository et leve les erreurs
 * HTTP 404 (introuvable) et 409 (immatriculation deja utilisee).
 */
@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    public VehicleService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    public List<Vehicle> findAll() {
        return vehicleRepository.findAll();
    }

    public Vehicle findById(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> introuvable(id));
    }

    public Vehicle create(Vehicle vehicle) {
        // Un nouveau vehicule commence disponible si le client ne precise pas de statut.
        if (vehicle.getStatut() == null) {
            vehicle.setStatut(VehicleStatus.DISPONIBLE);
        }
        verifierImmatriculationUnique(vehicle.getImmatriculation(), null);
        return vehicleRepository.save(vehicle);
    }

    public Vehicle update(Long id, Vehicle nouvellesValeurs) {
        Vehicle vehicule = findById(id);
        // On accepte seulement une immatriculation finale qui est libre.
        verifierImmatriculationUnique(nouvellesValeurs.getImmatriculation(), vehicule.getImmatriculation());
        vehicule.setType(nouvellesValeurs.getType());
        vehicule.setImmatriculation(nouvellesValeurs.getImmatriculation());
        vehicule.setAutonomieKm(nouvellesValeurs.getAutonomieKm());
        if (nouvellesValeurs.getStatut() != null) {
            vehicule.setStatut(nouvellesValeurs.getStatut());
        }
        return vehicleRepository.save(vehicule);
    }

    public void delete(Long id) {
        if (!vehicleRepository.existsById(id)) {
            throw introuvable(id);
        }
        vehicleRepository.deleteById(id);
    }

    /**
     * Verifie qu'une immatriculation peut etre utilisee : elle ne doit
     * appartenir a aucun vehicule (ou garder la meme valeur en cas de mise a jour).
     */
    private void verifierImmatriculationUnique(String immatriculation, String valeurActuelle) {
        boolean inchangee = immatriculation != null && immatriculation.equals(valeurActuelle);
        if (!inchangee && vehicleRepository.existsByImmatriculation(immatriculation)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Immatriculation '" + immatriculation + "' deja utilisee par un autre vehicule");
        }
    }

    private ResponseStatusException introuvable(Long id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicule introuvable avec l'id : " + id);
    }
}
