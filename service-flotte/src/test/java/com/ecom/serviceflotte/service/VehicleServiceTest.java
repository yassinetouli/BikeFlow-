package com.ecom.serviceflotte.service;

import java.util.Optional;

import com.ecom.serviceflotte.entity.Vehicle;
import com.ecom.serviceflotte.entity.VehicleStatus;
import com.ecom.serviceflotte.entity.VehicleType;
import com.ecom.serviceflotte.repository.VehicleRepository;

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
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Tests unitaires du service vehicule : le repository est simule (Mockito),
 * aucun acces a MySQL n'est necessaire.
 */
@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private VehicleService vehicleService;

    @Test
    void create_withoutStatut_setsDisponible() {
        Vehicle v = exemple("VELO-001");
        when(vehicleRepository.existsByImmatriculation("VELO-001")).thenReturn(false);
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Vehicle resultat = vehicleService.create(v);

        assertEquals(VehicleStatus.DISPONIBLE, resultat.getStatut());
    }

    @Test
    void create_withDuplicateImmatriculation_throws409() {
        when(vehicleRepository.existsByImmatriculation("VELO-001")).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> vehicleService.create(exemple("VELO-001")));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void findById_withUnknownId_throws404() {
        when(vehicleRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> vehicleService.findById(99L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void update_toAlreadyUsedImmatriculation_throws409() {
        Vehicle existant = exemple("VELO-001");
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(existant));
        when(vehicleRepository.existsByImmatriculation("VELO-002")).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> vehicleService.update(1L, exemple("VELO-002")));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    @Test
    void update_toSameImmatriculation_isAccepted() {
        Vehicle existant = exemple("VELO-001");
        existant.setAutonomieKm(30);
        Vehicle nouvellesValeurs = exemple("VELO-001");
        nouvellesValeurs.setAutonomieKm(30);
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(existant));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Vehicle modifie = vehicleService.update(1L, nouvellesValeurs);

        assertEquals("VELO-001", modifie.getImmatriculation());
        assertEquals(30, modifie.getAutonomieKm());
    }

    @Test
    void delete_withUnknownId_throws404() {
        when(vehicleRepository.existsById(99L)).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> vehicleService.delete(99L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    /** Fabrique un vehicule d'exemple, sans statut puisqu'on teste le defaut. */
    private Vehicle exemple(String immatriculation) {
        Vehicle vehicule = new Vehicle();
        vehicule.setType(VehicleType.VELO);
        vehicule.setImmatriculation(immatriculation);
        vehicule.setAutonomieKm(50);
        return vehicule;
    }
}
