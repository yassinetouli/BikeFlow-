package com.ecom.serviceutilisateurs.controller;

import com.ecom.serviceutilisateurs.client.FlotteClient;
import com.ecom.serviceutilisateurs.dto.InvoiceResponseDTO;
import com.ecom.serviceutilisateurs.dto.UserEligibilityDTO;
import com.ecom.serviceutilisateurs.dto.UserRequestDTO;
import com.ecom.serviceutilisateurs.dto.UserResponseDTO;
import com.ecom.serviceutilisateurs.service.InvoiceService;
import com.ecom.serviceutilisateurs.service.UserService;
import jakarta.validation.Valid;
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

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final InvoiceService invoiceService;
    private final FlotteClient flotteClient;

    public UserController(UserService userService, InvoiceService invoiceService, FlotteClient flotteClient) {
        this.userService = userService;
        this.invoiceService = invoiceService;
        this.flotteClient = flotteClient;
    }

    @GetMapping
    public List<UserResponseDTO> getAllUsers() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public UserResponseDTO getUserById(@PathVariable Long id) {
        return userService.findById(id);
    }

    @GetMapping("/{id}/exists")
    public boolean existsUserById(@PathVariable Long id) {
        return userService.existsById(id);
    }

    @GetMapping("/{id}/eligibilite")
    public UserEligibilityDTO checkEligibility(@PathVariable Long id) {
        return userService.checkEligibility(id);
    }

    @GetMapping("/{id}/invoices")
    public List<InvoiceResponseDTO> getUserInvoices(@PathVariable Long id) {
        return invoiceService.findByUserId(id);
    }

    @GetMapping("/{id}/has-impayes")
    public boolean hasImpayes(@PathVariable Long id) {
        return userService.hasImpayes(id);
    }

    @GetMapping("/{userId}/demo-feign/{vehicleId}")
    public Map<String, Object> demoFeign(@PathVariable Long userId, @PathVariable Long vehicleId) {
        UserResponseDTO user = userService.findById(userId);
        Map<String, Object> vehicle = flotteClient.getVehicleById(vehicleId);
        return Map.of(
                "user", user,
                "vehicle", vehicle,
                "feignStatus", "SUCCESS"
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDTO createUser(@Valid @RequestBody UserRequestDTO request) {
        return userService.create(request);
    }

    @PutMapping("/{id}")
    public UserResponseDTO updateUser(@PathVariable Long id, @Valid @RequestBody UserRequestDTO request) {
        return userService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        userService.delete(id);
    }
}
