package com.ecom.serviceutilisateurs.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "service-flotte", url = "${service-flotte.url:http://localhost:8083}")
public interface FlotteClient {

    @GetMapping("/api/vehicles/{id}")
    Map<String, Object> getVehicleById(@PathVariable("id") Long id);
}
