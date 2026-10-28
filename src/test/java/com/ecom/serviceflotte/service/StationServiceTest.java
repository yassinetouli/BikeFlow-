package com.ecom.serviceflotte.service;

import java.util.Optional;

import com.ecom.serviceflotte.entity.Station;
import com.ecom.serviceflotte.repository.StationRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

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
        Station station = exemple(10, 11);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> stationService.create(station));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(stationRepository, never()).save(station);
    }

    @Test
    void create_withZeroCapacite_throws400() {
        Station station = exemple(0, 0);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> stationService.create(station));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void create_withNegativePlacesLibres_throws400() {
        Station station = exemple(10, -1);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> stationService.create(station));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void create_withValidPlaces_isAccepted() {
        Station station = exemple(10, 5);
        when(stationRepository.save(station)).thenReturn(station);

        Station resultat = stationService.create(station);

        assertEquals(5, resultat.getPlacesLibres());
    }

    @Test
    void update_withPlacesLibresAboveCapacite_throws400() {
        Station existente = exemple(10, 5);
        when(stationRepository.findById(1L)).thenReturn(Optional.of(existente));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> stationService.update(1L, exemple(10, 20)));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(stationRepository, never()).save(existente);
    }

    @Test
    void delete_withUnknownId_throws404() {
        when(stationRepository.existsById(99L)).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> stationService.delete(99L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    /** Fabrique une station d'exemple avec la capacite et le nombre de places libres donnes. */
    private Station exemple(int capacite, int placesLibres) {
        Station station = new Station();
        station.setNom("Station Test");
        station.setAdresse("1 rue du Test");
        station.setCapacite(capacite);
        station.setPlacesLibres(placesLibres);
        return station;
    }
}
