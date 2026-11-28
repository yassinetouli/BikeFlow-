package com.ecom.serviceflotte.controller;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import com.ecom.serviceflotte.dto.VehicleRequest;
import com.ecom.serviceflotte.dto.VehicleResponse;
<<<<<<< HEAD
import com.ecom.serviceflotte.idempotence.IdempotencyStore;
=======
>>>>>>> 7005120ac66d5f1e55e58bfcc267e308f96a6063
import com.ecom.serviceflotte.service.VehicleService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;

/**
 * Endpoints REST pour les vehicules.
 * Ce contrôleur demande simplement au service d'effectuer le travail,
 * il ne contient pas de logique metier.
 */
@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;
    private final IdempotencyStore idempotence;

    public VehicleController(VehicleService vehicleService, IdempotencyStore idempotence) {
        this.vehicleService = vehicleService;
        this.idempotence = idempotence;
    }

    @GetMapping
    public List<VehicleResponse> getAllVehicles() {
        return vehicleService.findAll();
    }

    @GetMapping("/{id}")
    public VehicleResponse getVehicle(@PathVariable Long id) {
        return vehicleService.findById(id);
    }

    /**
     * Creation idempotente (V4) : avec un en-tete `Idempotency-Key`, un rejeu de la
     * meme requete renvoie le vehicule deja cree au lieu de repondre 409 (immatriculation
     * deja utilisee) ou de creer un doublon.
     */
    @PostMapping
<<<<<<< HEAD
    public ResponseEntity<VehicleResponse> createVehicle(@Valid @RequestBody VehicleRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        Optional<VehicleResponse> deja = idempotence.lire(idempotencyKey, VehicleResponse.class);
        if (deja.isPresent()) {
            return reponseRejouee(deja.get());
        }
        VehicleResponse created = vehicleService.create(request);
        idempotence.enregistrer(idempotencyKey, created);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    /** Meme reponse que la creation initiale, avec l'entete qui signale le rejeu. */
    private ResponseEntity<VehicleResponse> reponseRejouee(VehicleResponse existant) {
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(existant.id()).toUri();
        return ResponseEntity.created(location)
                .header("Idempotent-Replay", "true")
                .body(existant);
=======
    public ResponseEntity<VehicleResponse> createVehicle(@Valid @RequestBody VehicleRequest request) {
        VehicleResponse created = vehicleService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
>>>>>>> 7005120ac66d5f1e55e58bfcc267e308f96a6063
    }

    @PutMapping("/{id}")
    public VehicleResponse updateVehicle(@PathVariable Long id, @Valid @RequestBody VehicleRequest request) {
        return vehicleService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteVehicle(@PathVariable Long id) {
        vehicleService.delete(id);
    }
}
