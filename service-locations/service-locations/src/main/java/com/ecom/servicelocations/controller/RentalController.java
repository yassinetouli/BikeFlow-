package com.ecom.servicelocations.controller;

import com.ecom.servicelocations.dto.RentalCreateRequest;
import com.ecom.servicelocations.dto.RentalFinishRequest;
import com.ecom.servicelocations.dto.RentalResponse;
import com.ecom.servicelocations.dto.RentalStatusUpdateRequest;
import com.ecom.servicelocations.idempotence.IdempotencyStore;
import com.ecom.servicelocations.service.RentalService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/rentals")
public class RentalController {

    private final RentalService service;
    private final IdempotencyStore idempotence;

    public RentalController(RentalService service, IdempotencyStore idempotence) {
        this.service = service;
        this.idempotence = idempotence;
    }

    /**
     * Creation d'une location (validation inter-services du vehicule dans le service)
     * et idempotence des ecritures (V4) : si le client rejoue la requete avec la meme
     * `Idempotency-Key`, la location deja creee est renvoyee au lieu d'une deuxieme.
     */
    @PostMapping
    public ResponseEntity<RentalResponse> create(@Valid @RequestBody RentalCreateRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        Optional<RentalResponse> deja = idempotence.lire(idempotencyKey, RentalResponse.class);
        if (deja.isPresent()) {
            return reponseRejouee(deja.get());
        }
        RentalResponse created = service.reserver(request);
        idempotence.enregistrer(idempotencyKey, created);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    /** Meme reponse que la creation initiale, avec l'entete qui signale le rejeu. */
    private ResponseEntity<RentalResponse> reponseRejouee(RentalResponse existant) {
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(existant.id()).toUri();
        return ResponseEntity.created(location)
                .header("Idempotent-Replay", "true")
                .body(existant);
    }

    @GetMapping
    public List<RentalResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public RentalResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping("/user/{userEmail}")
    public List<RentalResponse> getByUser(@PathVariable String userEmail) {
        return service.getByUser(userEmail);
    }

    @PostMapping("/{id}/start")
    public RentalResponse start(@PathVariable Long id) {
        return service.demarrer(id);
    }

    @PostMapping("/{id}/finish")
    public RentalResponse finish(@PathVariable Long id, @Valid @RequestBody RentalFinishRequest request) {
        return service.terminer(id, request);
    }

    @PatchMapping("/{id}/status")
    public RentalResponse updateStatus(@PathVariable Long id, @Valid @RequestBody RentalStatusUpdateRequest request) {
        return service.updateStatus(id, request);
    }
}
