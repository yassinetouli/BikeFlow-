package com.ecom.serviceflotte.service;

import java.util.Optional;

import com.ecom.serviceflotte.dto.VehicleRequest;
import com.ecom.serviceflotte.dto.VehicleResponse;
import com.ecom.serviceflotte.exception.BusinessException;
import com.ecom.serviceflotte.exception.ResourceNotFoundException;
import com.ecom.serviceflotte.model.Vehicle;
import com.ecom.serviceflotte.model.VehicleStatus;
import com.ecom.serviceflotte.model.VehicleType;
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
        when(vehicleRepository.existsByImmatriculation("VELO-001")).thenReturn(false);
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VehicleResponse resultat = vehicleService.create(exemple("VELO-001"));

        assertEquals(VehicleStatus.DISPONIBLE, resultat.statut());
        assertEquals(VehicleType.VELO, resultat.type());
    }

    @Test
    void create_withDuplicateImmatriculation_throws409() {
        when(vehicleRepository.existsByImmatriculation("VELO-001")).thenReturn(true);

        assertThrows(BusinessException.class, () -> vehicleService.create(exemple("VELO-001")));
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void findById_withUnknownId_throws404() {
        when(vehicleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> vehicleService.findById(99L));
    }

    @Test
    void update_toAlreadyUsedImmatriculation_throws409() {
        Vehicle existant = entite("VELO-001");
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(existant));
        when(vehicleRepository.existsByImmatriculation("VELO-002")).thenReturn(true);

        assertThrows(BusinessException.class, () -> vehicleService.update(1L, exemple("VELO-002")));
    }

    @Test
    void update_toSameImmatriculation_isAccepted() {
        Vehicle existant = entite("VELO-001");
        existant.setAutonomieKm(30);
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(existant));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VehicleResponse modifie = vehicleService.update(1L, exemple("VELO-001"));

        assertEquals("VELO-001", modifie.immatriculation());
        assertEquals(30, modifie.autonomieKm());
        assertEquals(VehicleStatus.DISPONIBLE, modifie.statut());
    }

    @Test
    void delete_withUnknownId_throws404() {
        when(vehicleRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> vehicleService.delete(99L));
    }

    /** Fabrique une requete de vehicule, sans statut puisqu'on teste le defaut. */
    private VehicleRequest exemple(String immatriculation) {
        return new VehicleRequest(VehicleType.VELO, immatriculation, 30, null);
    }

    /** Fabrique un vehicule deja enregistre, tel que la retourne le repository. */
    private Vehicle entite(String immatriculation) {
        Vehicle vehicule = new Vehicle();
        vehicule.setType(VehicleType.VELO);
        vehicule.setImmatriculation(immatriculation);
        vehicule.setAutonomieKm(50);
        vehicule.setStatut(VehicleStatus.DISPONIBLE);
        return vehicule;
    }
}
