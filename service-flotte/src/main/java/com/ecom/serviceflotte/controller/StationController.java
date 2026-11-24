package com.ecom.serviceflotte.controller;

import java.util.List;

import com.ecom.serviceflotte.entity.Station;
import com.ecom.serviceflotte.service.StationService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/**
 * Endpoints REST pour les stations. Meme principe que VehicleController :
 * il recoit les requetes HTTP et delegue au service correspondant.
 */
@RestController
@RequestMapping("/api/stations")
public class StationController {

    private final StationService stationService;

    public StationController(StationService stationService) {
        this.stationService = stationService;
    }

    @GetMapping
    public List<Station> getAllStations() {
        return stationService.findAll();
    }

    @GetMapping("/{id}")
    public Station getStation(@PathVariable Long id) {
        return stationService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Station createStation(@Valid @RequestBody Station station) {
        return stationService.create(station);
    }

    @PutMapping("/{id}")
    public Station updateStation(@PathVariable Long id, @Valid @RequestBody Station station) {
        return stationService.update(id, station);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteStation(@PathVariable Long id) {
        stationService.delete(id);
    }
}
