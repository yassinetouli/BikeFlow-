package com.bikeflow.locations.controller;

import com.bikeflow.locations.dto.RentalCreateRequest;
import com.bikeflow.locations.dto.RentalFinishRequest;
import com.bikeflow.locations.dto.RentalResponse;
import com.bikeflow.locations.dto.RentalStatusUpdateRequest;
import com.bikeflow.locations.service.RentalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/rentals")
@Tag(name = "Locations", description = "Gestion du cycle de vie des locations")
public class RentalController {

    private final RentalService service;

    public RentalController(RentalService service) {
        this.service = service;
    }

    @Operation(summary = "Créer une réservation (statut RESERVEE)")
    @PostMapping
    public ResponseEntity<RentalResponse> create(@Valid @RequestBody RentalCreateRequest request) {
        RentalResponse created = service.reserver(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @Operation(summary = "Lister toutes les locations")
    @GetMapping
    public List<RentalResponse> getAll() {
        return service.getAll();
    }

    @Operation(summary = "Consulter une location")
    @GetMapping("/{id}")
    public RentalResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @Operation(summary = "Historique des locations d'un utilisateur (par email)")
    @GetMapping("/user/{userEmail}")
    public List<RentalResponse> getByUser(@PathVariable String userEmail) {
        return service.getByUser(userEmail);
    }

    @Operation(summary = "Démarrer une location (RESERVEE -> EN_COURS)")
    @PostMapping("/{id}/start")
    public RentalResponse start(@PathVariable Long id) {
        return service.demarrer(id);
    }

    @Operation(summary = "Terminer une location (calcul du montant)")
    @PostMapping("/{id}/finish")
    public RentalResponse finish(@PathVariable Long id, @Valid @RequestBody RentalFinishRequest request) {
        return service.terminer(id, request);
    }

    @Operation(summary = "Mettre à jour le statut (RESERVEE->EN_COURS, EN_COURS->EN_RETARD)")
    @PatchMapping("/{id}/status")
    public RentalResponse updateStatus(@PathVariable Long id, @Valid @RequestBody RentalStatusUpdateRequest request) {
        return service.updateStatus(id, request);
    }
}
