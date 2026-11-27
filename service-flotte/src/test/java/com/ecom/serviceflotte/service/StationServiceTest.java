package com.ecom.serviceflotte.service;

import java.util.Optional;

import com.ecom.serviceflotte.dto.StationRequest;
import com.ecom.serviceflotte.dto.StationResponse;
import com.ecom.serviceflotte.exception.BadRequestException;
import com.ecom.serviceflotte.exception.ResourceNotFoundException;
import com.ecom.serviceflotte.model.Station;
import com.ecom.serviceflotte.repository.StationRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests unitaires du service station avec un repository simule (Mockito).
 * On verifie surtout les regles metiers sur la capacite et les places libres.
 */
@ExtendWith(MockitoExtension.class)
class StationServiceTest {

    @Mock
    private StationRepository stationRepository;

    @InjectMocks
    private StationService stationService;

    @Test
    void create_withPlacesLibresAboveCapacite_throws400() {
        StationRequest requete = exemple(10, 11);

        assertThrows(BadRequestException.class, () -> stationService.create(requete));
        verify(stationRepository, never()).save(any(Station.class));
    }

    @Test
    void create_withZeroCapacite_throws400() {
        assertThrows(BadRequestException.class, () -> stationService.create(exemple(0, 0)));
    }

    @Test
    void create_withNegativePlacesLibres_throws400() {
        assertThrows(BadRequestException.class, () -> stationService.create(exemple(10, -1)));
    }

    @Test
    void create_withValidPlaces_isAccepted() {
        when(stationRepository.save(any(Station.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StationResponse resultat = stationService.create(exemple(10, 5));

        assertEquals("Station Test", resultat.nom());
        assertEquals(10, resultat.capacite());
        assertEquals(5, resultat.placesLibres());
    }

    @Test
    void update_withPlacesLibresAboveCapacite_throws400() {
        Station existente = entite(10, 5);
        when(stationRepository.findById(1L)).thenReturn(Optional.of(existente));

        assertThrows(BadRequestException.class, () -> stationService.update(1L, exemple(10, 20)));
        verify(stationRepository, never()).save(existente);
    }

    @Test
    void update_withValidValues_returnsUpdatedStation() {
        Station existente = entite(10, 5);
        when(stationRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(stationRepository.save(any(Station.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StationResponse resultat = stationService.update(1L, exemple(12, 8));

        assertEquals(12, resultat.capacite());
        assertEquals(8, resultat.placesLibres());
    }

    @Test
    void delete_withUnknownId_throws404() {
        when(stationRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> stationService.delete(99L));
    }

    /** Fabrique la requete d'exemple avec la capacite et le nombre de places libres donnes. */
    private StationRequest exemple(int capacite, int placesLibres) {
        return new StationRequest("Station Test", "1 rue du Test", capacite, placesLibres);
    }

    /** Fabrique une station deja enregistree, telle que la retourne le repository. */
    private Station entite(int capacite, int placesLibres) {
        Station station = new Station();
        station.setNom("Station Test");
        station.setAdresse("1 rue du Test");
        station.setCapacite(capacite);
        station.setPlacesLibres(placesLibres);
        return station;
    }
}
