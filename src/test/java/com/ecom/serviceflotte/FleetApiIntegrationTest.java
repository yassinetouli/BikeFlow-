package com.ecom.serviceflotte;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests d'integration REST : l'application demarre reellement avec MySQL,
 * puis chaque requete traverse le controller, le service et la base de donnees.
 * Chaque test cree ses donnees avec un identifiant unique, puis supprime ce
 * qu'il a cree pour garder la base de developpement propre.
 */
@SpringBootTest(properties = "eureka.client.enabled=false")
@AutoConfigureMockMvc
class FleetApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void stationLifecycle_createReadUpdateListDelete() throws Exception {
        String nom = "Station Test " + unique();
        String requete = "{"
                + "\"nom\":\"" + nom + "\","
                + "\"adresse\":\"1 rue du Test\","
                + "\"capacite\":10,"
                + "\"placesLibres\":5"
                + "}";

        // 1. creation -> 201 Created
        MvcResult creation = mockMvc.perform(post("/api/stations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requete))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();
        long id = extractionId(creation.getResponse().getContentAsString());

        // 2. lecture par id -> 200 OK avec les bonnes valeurs
        mockMvc.perform(get("/api/stations/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value(nom))
                .andExpect(jsonPath("$.capacite").value(10))
                .andExpect(jsonPath("$.placesLibres").value(5));

        // 3. liste -> contient au moins la station creee
        MvcResult liste = mockMvc.perform(get("/api/stations"))
                .andExpect(status().isOk())
                .andReturn();
        assertTrue(liste.getResponse().getContentAsString().contains(nom));

        // 4. mise a jour -> 200 OK avec la nouvelle valeur
        mockMvc.perform(put("/api/stations/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requete.replace("\"placesLibres\":5", "\"placesLibres\":8")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.placesLibres").value(8));

        // 11. suppression -> 204 No Content, puis la station n'existe plus
        mockMvc.perform(delete("/api/stations/" + id))
                .andExpect(status().isNoContent());

        // 12. ressource inexistante -> 404 Not Found
        mockMvc.perform(get("/api/stations/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void stationInvalidValues_rejectedWith400() throws Exception {
        // 5. placesLibres superieur a capacite -> 400 Bad Request
        mockMvc.perform(post("/api/stations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"
                                + "\"nom\":\"Station Invalide\"," // nom unique non necessaire : jamais sauvegarde
                                + "\"adresse\":\"1 rue du Test\","
                                + "\"capacite\":10,"
                                + "\"placesLibres\":20"
                                + "}"))
                .andExpect(status().isBadRequest());

        // 5 (suite). capacite nulle -> 400 Bad Request
        mockMvc.perform(post("/api/stations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"
                                + "\"nom\":\"Station Invalide\","
                                + "\"adresse\":\"1 rue du Test\","
                                + "\"capacite\":0,"
                                + "\"placesLibres\":0"
                                + "}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void vehicleLifecycle_createReadUpdateListDelete() throws Exception {
        String immatriculation = "TEST-" + unique();
        String requete = "{"
                + "\"type\":\"VELO\","
                + "\"immatriculation\":\"" + immatriculation + "\","
                + "\"autonomieKm\":40"
                + "}";

        // 6. creation -> 201 Created, statut par defaut DISPONIBLE
        MvcResult creation = mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requete))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statut").value("DISPONIBLE"))
                .andReturn();
        long id = extractionId(creation.getResponse().getContentAsString());

        // 7. lecture par id -> 200 OK
        mockMvc.perform(get("/api/vehicles/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.immatriculation").value(immatriculation))
                .andExpect(jsonPath("$.type").value("VELO"));

        // 8. liste -> contient au moins le vehicule cree
        MvcResult liste = mockMvc.perform(get("/api/vehicles"))
                .andExpect(status().isOk())
                .andReturn();
        assertTrue(liste.getResponse().getContentAsString().contains(immatriculation));

        // 9. mise a jour -> 200 OK
        mockMvc.perform(put("/api/vehicles/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requete.replace("\"autonomieKm\":40", "\"autonomieKm\":12")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.autonomieKm").value(12));

        // 11. suppression -> 204 No Content, puis 404 Not Found
        mockMvc.perform(delete("/api/vehicles/" + id))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/vehicles/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void vehicleDuplicateImmatriculation_rejectedWith409() throws Exception {
        String immatriculation = "TEST-" + unique();
        String requete = "{"
                + "\"type\":\"VELO\","
                + "\"immatriculation\":\"" + immatriculation + "\","
                + "\"autonomieKm\":40"
                + "}";

        // creation du vehicule de reference
        MvcResult creation = mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requete))
                .andExpect(status().isCreated())
                .andReturn();
        long id = extractionId(creation.getResponse().getContentAsString());

        // 10. nouvelle creation avec la meme immatriculation -> 409 Conflict
        mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requete))
                .andExpect(status().isConflict());

        // suppression du vehicule de reference pour garder la base propre
        mockMvc.perform(delete("/api/vehicles/" + id))
                .andExpect(status().isNoContent());
    }

    /** Lit le champ id dans une reponse JSON de creation. */
    private long extractionId(String json) throws Exception {
        JsonNode noeud = objectMapper.readTree(json);
        return noeud.get("id").asLong();
    }

    /** Suffixe unique pour que chaque execution de test reste independante. */
    private String unique() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
