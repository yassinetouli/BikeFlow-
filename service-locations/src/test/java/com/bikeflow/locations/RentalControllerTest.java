package com.bikeflow.locations;

import com.bikeflow.locations.repository.RentalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class RentalControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    RentalRepository repository;

    @BeforeEach
    void clean() {
        repository.deleteAll();
    }

    private static final String CREATE_BODY = """
            {"vehicleId": 1, "userEmail": "salma@bikeflow.com", "stationDepart": 100}""";

    private long createRental() throws Exception {
        String json = mvc.perform(post("/api/rentals").contentType(MediaType.APPLICATION_JSON).content(CREATE_BODY))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return Long.parseLong(json.replaceAll(".*\"id\":(\\d+).*", "$1"));
    }

    // ---------- cas valides ----------

    @Test
    void create_returns201_andStatusReservee() throws Exception {
        mvc.perform(post("/api/rentals").contentType(MediaType.APPLICATION_JSON).content(CREATE_BODY))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.statut").value("RESERVEE"))
                .andExpect(jsonPath("$.vehicleId").value(1))
                .andExpect(jsonPath("$.userEmail").value("salma@bikeflow.com"));
    }

    @Test
    void fullLifecycle_reservee_to_terminee_withAmount() throws Exception {
        long id = createRental();

        mvc.perform(post("/api/rentals/{id}/start", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("EN_COURS"))
                .andExpect(jsonPath("$.dateDebut").exists());

        mvc.perform(post("/api/rentals/{id}/finish", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stationArrive\": 200, \"kmParcourus\": 12.5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("TERMINEE"))
                .andExpect(jsonPath("$.montant").value(25.0))
                .andExpect(jsonPath("$.stationArrive").value(200));
    }

    @Test
    void getByUser_returnsHistory() throws Exception {
        createRental();
        mvc.perform(get("/api/rentals/user/{userEmail}", "Salma@BikeFlow.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    // ---------- erreurs de validation (400) ----------

    @Test
    void create_withMissingFields_returns400() throws Exception {
        mvc.perform(post("/api/rentals").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details", hasSize(3)));
    }

    @Test
    void create_withInvalidEmail_returns400() throws Exception {
        mvc.perform(post("/api/rentals").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"vehicleId\": 1, \"userEmail\": \"pas-un-email\", \"stationDepart\": 100}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void finish_withNegativeKm_returns400() throws Exception {
        long id = createRental();
        mvc.perform(post("/api/rentals/{id}/start", id)).andExpect(status().isOk());
        mvc.perform(post("/api/rentals/{id}/finish", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stationArrive\": 200, \"kmParcourus\": -3}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getById_withNonNumericId_returns400() throws Exception {
        mvc.perform(get("/api/rentals/abc")).andExpect(status().isBadRequest());
    }

    @Test
    void patchStatus_invalidValue_returns400() throws Exception {
        long id = createRental();
        mvc.perform(patch("/api/rentals/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"statut\": \"BIDON\"}"))
                .andExpect(status().isBadRequest());
    }

    // ---------- ressources inexistantes (404) ----------

    @Test
    void getById_unknown_returns404() throws Exception {
        mvc.perform(get("/api/rentals/{id}", 9999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Location introuvable : id=9999"));
    }

    @Test
    void start_unknown_returns404() throws Exception {
        mvc.perform(post("/api/rentals/{id}/start", 9999)).andExpect(status().isNotFound());
    }

    // ---------- règles de gestion (409) ----------

    @Test
    void create_sameVehicleTwice_returns409() throws Exception {
        createRental();
        mvc.perform(post("/api/rentals").contentType(MediaType.APPLICATION_JSON).content(CREATE_BODY))
                .andExpect(status().isConflict());
    }

    @Test
    void finish_beforeStart_returns409() throws Exception {
        long id = createRental();
        mvc.perform(post("/api/rentals/{id}/finish", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stationArrive\": 200, \"kmParcourus\": 5}"))
                .andExpect(status().isConflict());
    }

    @Test
    void start_twice_returns409() throws Exception {
        long id = createRental();
        mvc.perform(post("/api/rentals/{id}/start", id)).andExpect(status().isOk());
        mvc.perform(post("/api/rentals/{id}/start", id)).andExpect(status().isConflict());
    }

    @Test
    void patchStatus_toTerminee_returns409() throws Exception {
        long id = createRental();
        mvc.perform(patch("/api/rentals/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"statut\": \"TERMINEE\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void patchStatus_enCoursToEnRetard_ok() throws Exception {
        long id = createRental();
        mvc.perform(post("/api/rentals/{id}/start", id)).andExpect(status().isOk());
        mvc.perform(patch("/api/rentals/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"statut\": \"EN_RETARD\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("EN_RETARD"));
    }
}
