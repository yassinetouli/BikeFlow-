package com.ecom.serviceutilisateurs.controller;

import com.ecom.serviceutilisateurs.dto.InvoiceResponseDTO;
import com.ecom.serviceutilisateurs.dto.UserEligibilityDTO;
import com.ecom.serviceutilisateurs.dto.UserRequestDTO;
import com.ecom.serviceutilisateurs.dto.UserResponseDTO;
import com.ecom.serviceutilisateurs.idempotence.IdempotencyStore;
import com.ecom.serviceutilisateurs.resilience.FlotteResilienceService;
import com.ecom.serviceutilisateurs.resilience.ModeFallback;
import com.ecom.serviceutilisateurs.resilience.ReponseVehiculeResiliente;
import com.ecom.serviceutilisateurs.service.InvoiceService;
import com.ecom.serviceutilisateurs.service.UserService;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final InvoiceService invoiceService;
    private final FlotteResilienceService flotteResilienceService;
    private final IdempotencyStore idempotence;

    public UserController(UserService userService, InvoiceService invoiceService,
                          FlotteResilienceService flotteResilienceService,
                          IdempotencyStore idempotence) {
        this.userService = userService;
        this.invoiceService = invoiceService;
        this.flotteResilienceService = flotteResilienceService;
        this.idempotence = idempotence;
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

    /**
     * Demonstration de la communication synchrone utilisateurs -> flotte (V4) et de la
     * resilience V6 : retry, circuit breaker et fallback.
     *
     * ?fallback=cache (defaut) renvoie la derniere valeur connue du vehicule,
     * ?fallback=strict renvoie un vehicule de secours fixe.
     */
    @GetMapping("/{userId}/demo-feign/{vehicleId}")
    public Map<String, Object> demoFeign(@PathVariable Long userId,
                                         @PathVariable Long vehicleId,
                                         @RequestParam(name = "fallback", defaultValue = "cache") String fallback) {
        UserResponseDTO user = userService.findById(userId);
        ModeFallback mode = ModeFallback.STRICT.name().equalsIgnoreCase(fallback)
                ? ModeFallback.STRICT
                : ModeFallback.CACHE;
        ReponseVehiculeResiliente reponse = flotteResilienceService.recupererVehicule(vehicleId, mode);

        Map<String, Object> corps = new LinkedHashMap<>();
        corps.put("user", user);
        corps.put("vehicle", reponse.vehicule());
        corps.put("feignStatus", reponse.degrade() ? "DEGRADED" : "SUCCESS");
        corps.put("source", reponse.source());
        corps.put("fallback", reponse.degrade() ? reponse.source() : "AUCUN");
        corps.put("etatCircuit", reponse.etatCircuit());
        corps.put("tentatives", reponse.tentatives());
        corps.put("message", reponse.message());
        return corps;
    }

    /**
     * Creation idempotente (V4) : avec un en-tete `Idempotency-Key`, un rejeu de la
     * meme requete renvoie l'utilisateur deja cree au lieu de repondre 409 (email
     * deja utilise).
     */
    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserRequestDTO request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        Optional<UserResponseDTO> deja = idempotence.lire(idempotencyKey, UserResponseDTO.class);
        if (deja.isPresent()) {
            return reponseRejouee(deja.get());
        }
        UserResponseDTO created = userService.create(request);
        idempotence.enregistrer(idempotencyKey, created);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    /** Meme reponse que la creation initiale, avec l'entete qui signale le rejeu. */
    private ResponseEntity<UserResponseDTO> reponseRejouee(UserResponseDTO existant) {
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(existant.getId()).toUri();
        return ResponseEntity.created(location)
                .header("Idempotent-Replay", "true")
                .body(existant);
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
