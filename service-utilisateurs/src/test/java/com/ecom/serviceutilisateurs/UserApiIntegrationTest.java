package com.ecom.serviceutilisateurs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "eureka.client.enabled=false")
@AutoConfigureMockMvc
class UserApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void userLifecycle_createReadUpdateListDelete() throws Exception {
        String email = "test." + unique() + "@example.com";
        String requete = "{"
                + "\"nom\":\"Mounir Ghalem\","
                + "\"email\":\"" + email + "\","
                + "\"moyenPaiement\":\"CB_VISA_4242\","
                + "\"suspendu\":false"
                + "}";

        // 1. Creation -> 201 Created
        MvcResult creation = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requete))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.eligibleLocation").value(true))
                .andReturn();
        long id = extractionId(creation.getResponse().getContentAsString());

        // 2. Lecture par id -> 200 OK
        mockMvc.perform(get("/api/users/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value("Mounir Ghalem"))
                .andExpect(jsonPath("$.email").value(email));

        // 3. Verification existence /exists -> 200 OK (true)
        mockMvc.perform(get("/api/users/" + id + "/exists"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        // 4. Verification eligibilite -> 200 OK
        mockMvc.perform(get("/api/users/" + id + "/eligibilite"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eligible").value(true))
                .andExpect(jsonPath("$.suspendu").value(false));

        // 5. Liste -> contient l'utilisateur cree
        MvcResult liste = mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andReturn();
        assertTrue(liste.getResponse().getContentAsString().contains(email));

        // 6. Mise a jour -> 200 OK
        String requeteMaj = requete.replace("\"CB_VISA_4242\"", "\"MASTERCARD_5555\"");
        mockMvc.perform(put("/api/users/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requeteMaj))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.moyenPaiement").value("MASTERCARD_5555"));

        // 7. Doublon email -> 409 Conflict
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requete))
                .andExpect(status().isConflict());

        // 8. Suppression -> 204 No Content
        mockMvc.perform(delete("/api/users/" + id))
                .andExpect(status().isNoContent());

        // 9. Lecture apres suppression -> 404 Not Found
        mockMvc.perform(get("/api/users/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void userInvalidData_rejectedWith400() throws Exception {
        String requeteInvalide = "{"
                + "\"nom\":\"\","
                + "\"email\":\"email-invalide\""
                + "}";

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requeteInvalide))
                .andExpect(status().isBadRequest());
    }

    private long extractionId(String json) throws Exception {
        JsonNode noeud = objectMapper.readTree(json);
        return noeud.get("id").asLong();
    }

    private String unique() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
