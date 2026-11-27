package com.ecom.serviceutilisateurs.resilience;

import com.ecom.serviceutilisateurs.client.FlotteClient;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests unitaires du jalon V6 : retry, disjoncteur et les deux fallbacks.
 * Aucun serveur n'est demarre : FlotteClient est remplace par un faux client
 * que l'on met en panne a volonte.
 */
class FlotteResilienceServiceTest {

    /** Faux service-flotte : repond normalement, ou leve une exception quand on le met en panne. */
    private static class FauxFlotteClient implements FlotteClient {

        private volatile boolean enPanne = false;
        private final AtomicInteger appels = new AtomicInteger();

        @Override
        public Map<String, Object> getVehicleById(Long id) {
            appels.incrementAndGet();
            if (enPanne) {
                throw new RuntimeException("Connection refused");
            }
            Map<String, Object> vehicule = new HashMap<>();
            vehicule.put("id", id);
            vehicule.put("type", "VELO");
            vehicule.put("immatriculation", "AB-123-CD");
            vehicule.put("autonomieKm", 40);
            vehicule.put("statut", "DISPONIBLE");
            return vehicule;
        }

        void mettreEnPanne(boolean panne) {
            this.enPanne = panne;
        }

        int nombreAppels() {
            return appels.get();
        }
    }

    private FlotteResilienceService service(FauxFlotteClient client, DisjoncteurFlotte disjoncteur, CacheVehicules cache) {
        // 3 tentatives, 0 ms d'attente pour que les tests restent instantanes
        return new FlotteResilienceService(client, disjoncteur, cache, 3, 0L);
    }

    @Test
    void appelNominal_neFaitQuUneTentativeEtRemplitLeCache() {
        FauxFlotteClient client = new FauxFlotteClient();
        DisjoncteurFlotte disjoncteur = new DisjoncteurFlotte(3, 15000L, 1);
        CacheVehicules cache = new CacheVehicules();

        ReponseVehiculeResiliente reponse = service(client, disjoncteur, cache)
                .recupererVehicule(1L, ModeFallback.CACHE);

        assertEquals("FLOTTE", reponse.source());
        assertFalse(reponse.degrade());
        assertEquals(1, reponse.tentatives());
        assertEquals("FERME", reponse.etatCircuit());
        assertEquals(1, client.nombreAppels());
        assertEquals(1, cache.taille());
    }

    @Test
    void retry_troisTentativesPuisFallbackStrictEtCircuitOuvert() {
        FauxFlotteClient client = new FauxFlotteClient();
        client.mettreEnPanne(true);
        DisjoncteurFlotte disjoncteur = new DisjoncteurFlotte(3, 15000L, 1);
        CacheVehicules cache = new CacheVehicules();

        ReponseVehiculeResiliente reponse = service(client, disjoncteur, cache)
                .recupererVehicule(2L, ModeFallback.CACHE);

        // 1. Le retry a bien envoye 3 requetes
        assertEquals(3, client.nombreAppels());
        assertEquals(3, reponse.tentatives());
        // 2. Sans valeur en cache, le fallback est strict
        assertEquals("STRICT", reponse.source());
        assertTrue(reponse.degrade());
        assertEquals("INDISPONIBLE", reponse.vehicule().get("statut"));
        // 3. Les 3 echecs ont ouvert le circuit
        assertEquals("OUVERT", reponse.etatCircuit());
    }

    @Test
    void circuitOuvert_appelSuivantEchoueViteSansContacterLaFlotte() {
        FauxFlotteClient client = new FauxFlotteClient();
        client.mettreEnPanne(true);
        DisjoncteurFlotte disjoncteur = new DisjoncteurFlotte(3, 15000L, 1);
        CacheVehicules cache = new CacheVehicules();
        FlotteResilienceService service = service(client, disjoncteur, cache);

        service.recupererVehicule(3L, ModeFallback.CACHE);
        int appelsApresOuverture = client.nombreAppels();

        ReponseVehiculeResiliente second = service.recupererVehicule(3L, ModeFallback.CACHE);

        // Aucune nouvelle requete : le disjoncteur refuse l'appel (fail fast)
        assertEquals(appelsApresOuverture, client.nombreAppels());
        assertEquals(0, second.tentatives());
        assertTrue(second.message().contains("CIRCUIT_OUVERT"));
    }

    @Test
    void fallbackCache_renvoieLaDerniereValeurConnue() {
        FauxFlotteClient client = new FauxFlotteClient();
        DisjoncteurFlotte disjoncteur = new DisjoncteurFlotte(3, 15000L, 1);
        CacheVehicules cache = new CacheVehicules();
        FlotteResilienceService service = service(client, disjoncteur, cache);

        // 1. Un appel reussi met le vehicule 4 en cache
        service.recupererVehicule(4L, ModeFallback.CACHE);

        // 2. service-flotte tombe
        client.mettreEnPanne(true);

        // 3. Le fallback CACHE renvoie la valeur memorisee, pas une erreur
        ReponseVehiculeResiliente reponse = service.recupererVehicule(4L, ModeFallback.CACHE);

        assertEquals("CACHE", reponse.source());
        assertTrue(reponse.degrade());
        assertEquals("AB-123-CD", reponse.vehicule().get("immatriculation"));
    }

    @Test
    void apresRetourDuService_leCircuitSeReferme() {
        FauxFlotteClient client = new FauxFlotteClient();
        client.mettreEnPanne(true);
        // Duree d'ouverture nulle : le circuit passe directement en SEMI_OUVERT
        DisjoncteurFlotte disjoncteur = new DisjoncteurFlotte(3, 0L, 1);
        CacheVehicules cache = new CacheVehicules();
        FlotteResilienceService service = service(client, disjoncteur, cache);

        service.recupererVehicule(5L, ModeFallback.CACHE);
        assertEquals(EtatCircuit.SEMI_OUVERT, disjoncteur.etat());

        client.mettreEnPanne(false);
        ReponseVehiculeResiliente reponse = service.recupererVehicule(5L, ModeFallback.CACHE);

        assertEquals("FLOTTE", reponse.source());
        assertEquals("FERME", reponse.etatCircuit());
    }
}
