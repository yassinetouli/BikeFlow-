package com.ecom.serviceflotte.controller;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import com.ecom.serviceflotte.dto.StationRequest;
import com.ecom.serviceflotte.dto.StationResponse;
<<<<<<< HEAD
import com.ecom.serviceflotte.idempotence.IdempotencyStore;
=======
>>>>>>> 7005120ac66d5f1e55e58bfcc267e308f96a6063
import com.ecom.serviceflotte.service.StationService;

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
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;

/**
 * Endpoints REST pour les stations. Meme principe que VehicleController :
 * il recoit les requetes HTTP et delegue au service correspondant.
 */
@RestController
@RequestMapping("/api/stations")
public class StationController {

    private final StationService stationService;
    private final IdempotencyStore idempotence;

    public StationController(StationService stationService, IdempotencyStore idempotence) {
        this.stationService = stationService;
        this.idempotence = idempotence;
    }

    @GetMapping
    public List<StationResponse> getAllStations() {
        return stationService.findAll();
    }

    @GetMapping("/{id}")
    public StationResponse getStation(@PathVariable Long id) {
        return stationService.findById(id);
    }

    /**
     * Creation idempotente (V4) : un rejeu avec la meme `Idempotency-Key` renvoie
     * la station deja creee au lieu de creer une deuxieme station.
     */
    @PostMapping
<<<<<<< HEAD
    public ResponseEntity<StationResponse> createStation(@Valid @RequestBody StationRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        Optional<StationResponse> deja = idempotence.lire(idempotencyKey, StationResponse.class);
        if (deja.isPresent()) {
            return reponseRejouee(deja.get());
        }
        StationResponse created = stationService.create(request);
        idempotence.enregistrer(idempotencyKey, created);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    /** Meme reponse que la creation initiale, avec l'entete qui signale le rejeu. */
    private ResponseEntity<StationResponse> reponseRejouee(StationResponse existant) {
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(existant.id()).toUri();
        return ResponseEntity.created(location)
                .header("Idempotent-Replay", "true")
                .body(existant);
=======
    public ResponseEntity<StationResponse> createStation(@Valid @RequestBody StationRequest request) {
        StationResponse created = stationService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
>>>>>>> 7005120ac66d5f1e55e58bfcc267e308f96a6063
    }

    @PutMapping("/{id}")
    public StationResponse updateStation(@PathVariable Long id, @Valid @RequestBody StationRequest request) {
        return stationService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteStation(@PathVariable Long id) {
        stationService.delete(id);
    }
}
