package com.ecom.servicelocations.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * Client OpenFeign vers service-flotte (jalon V4).
 *
 * Avant de creer une location, service-locations demande a service-flotte si le
 * vehicule existe et s'il est disponible : c'est la validation inter-services
 * sur le POST /api/rentals.
 */
@FeignClient(name = "service-flotte", url = "${service-flotte.url:http://localhost:8083}")
public interface FlotteClient {

    @GetMapping("/api/vehicles/{id}")
    Map<String, Object> getVehicleById(@PathVariable("id") Long id);
}
