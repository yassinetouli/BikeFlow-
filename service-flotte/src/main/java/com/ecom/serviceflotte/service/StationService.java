package com.ecom.serviceflotte.service;

import java.util.List;

import com.ecom.serviceflotte.dto.StationRequest;
import com.ecom.serviceflotte.dto.StationResponse;
import com.ecom.serviceflotte.exception.BadRequestException;
import com.ecom.serviceflotte.exception.ResourceNotFoundException;
import com.ecom.serviceflotte.model.Station;
import com.ecom.serviceflotte.repository.StationRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Logique metier des stations : il verifie que la capacite est positive
 * et que le nombre de places libres reste entre 0 et la capacite.
 */
@Service
@Transactional
public class StationService {

    private final StationRepository stationRepository;

    public StationService(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    @Transactional(readOnly = true)
    public List<StationResponse> findAll() {
        return stationRepository.findAll().stream().map(StationResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public StationResponse findById(Long id) {
        return StationResponse.from(findOrThrow(id));
    }

    public StationResponse create(StationRequest request) {
        verifierPlaces(request);
        Station station = new Station();
        appliquer(station, request);
        return StationResponse.from(stationRepository.save(station));
    }

    public StationResponse update(Long id, StationRequest request) {
        Station station = findOrThrow(id);
        verifierPlaces(request);
        appliquer(station, request);
        return StationResponse.from(stationRepository.save(station));
    }

    public void delete(Long id) {
        if (!stationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Station introuvable avec l'id : " + id);
        }
        stationRepository.deleteById(id);
    }

    /** Copie les valeurs du DTO vers l'entite (creation et mise a jour). */
    private void appliquer(Station station, StationRequest request) {
        station.setNom(request.nom());
        station.setAdresse(request.adresse());
        station.setCapacite(request.capacite());
        station.setPlacesLibres(request.placesLibres());
    }

    /**
     * Regles metier sur les places :
     * - capacite superieure a zero (un @Min(1) le verifie aussi a l'entree de l'API),
     * - placesLibres jamais negatif (un @Min(0) le verifie aussi),
     * - placesLibres jamais superieur a capacite : verifie dans le service
     *   car c'est une regle entre deux champs du meme DTO.
     */
    private void verifierPlaces(StationRequest request) {
        if (request.capacite() < 1) {
            throw new BadRequestException("La capacite doit etre superieure a zero");
        }
        if (request.placesLibres() < 0) {
            throw new BadRequestException("placesLibres ne peut pas etre negatif");
        }
        if (request.placesLibres() > request.capacite()) {
            throw new BadRequestException("placesLibres (" + request.placesLibres()
                    + ") ne peut pas depasser capacite (" + request.capacite() + ")");
        }
    }

    private Station findOrThrow(Long id) {
        return stationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Station introuvable avec l'id : " + id));
    }
}
