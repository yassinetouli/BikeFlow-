package com.ecom.servicelocations.controller;

import com.ecom.servicelocations.dto.RentalCreateRequest;
import com.ecom.servicelocations.dto.RentalFinishRequest;
import com.ecom.servicelocations.dto.RentalResponse;
import com.ecom.servicelocations.dto.RentalStatusUpdateRequest;
import com.ecom.servicelocations.service.RentalService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/rentals")
public class RentalController {

    private final RentalService service;

    public RentalController(RentalService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<RentalResponse> create(@Valid @RequestBody RentalCreateRequest request) {
        RentalResponse created = service.reserver(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
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
