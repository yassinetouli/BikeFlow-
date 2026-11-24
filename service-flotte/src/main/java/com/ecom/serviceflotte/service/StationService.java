package com.ecom.serviceflotte.service;

import java.util.List;

import com.ecom.serviceflotte.entity.Station;
import com.ecom.serviceflotte.repository.StationRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Logique metier des stations : il verifie que la capacite est positive
 * et que le nombre de places libres reste entre 0 et la capacite.
 */
@Service
public class StationService {

    private final StationRepository stationRepository;

    public StationService(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    public List<Station> findAll() {
        return stationRepository.findAll();
    }

    public Station findById(Long id) {
        return stationRepository.findById(id)
                .orElseThrow(() -> introuvable(id));
    }

    public Station create(Station station) {
        verifierPlaces(station);
        return stationRepository.save(station);
    }

    public Station update(Long id, Station nouvellesValeurs) {
        Station station = findById(id);
        verifierPlaces(nouvellesValeurs);
        station.setNom(nouvellesValeurs.getNom());
        station.setAdresse(nouvellesValeurs.getAdresse());
        station.setCapacite(nouvellesValeurs.getCapacite());
        station.setPlacesLibres(nouvellesValeurs.getPlacesLibres());
        return stationRepository.save(station);
    }

    public void delete(Long id) {
        if (!stationRepository.existsById(id)) {
            throw introuvable(id);
        }
        stationRepository.deleteById(id);
    }

    /**
     * Regles metier sur les places :
     * - capacite superieure a zero (un @Min(1) le verifie aussi a l'entree de l'API),
     * - placesLibres jamais negatif (un @Min(0) le verifie aussi),
     * - placesLibres jamais superieur a capacite : verifie dans le service
     *   car c'est une regle entre deux champs de la meme entite.
     */
    private void verifierPlaces(Station station) {
        if (station.getCapacite() < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La capacite doit etre superieure a zero");
        }
        if (station.getPlacesLibres() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "placesLibres ne peut pas etre negatif");
        }
        if (station.getPlacesLibres() > station.getCapacite()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "placesLibres (" + station.getPlacesLibres() + ") ne peut pas depasser capacite ("
                            + station.getCapacite() + ")");
        }
    }

    private ResponseStatusException introuvable(Long id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Station introuvable avec l'id : " + id);
    }
}
