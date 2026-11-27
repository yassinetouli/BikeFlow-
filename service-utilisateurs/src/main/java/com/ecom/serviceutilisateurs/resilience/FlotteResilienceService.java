package com.ecom.serviceutilisateurs.resilience;

import com.ecom.serviceutilisateurs.client.FlotteClient;

import feign.FeignException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Enveloppe resiliente autour de l'appel OpenFeign vers service-flotte (jalon V6).
 *
 * Trois mecanismes, dans cet ordre :
 * 1. RETRY : chaque appel est retente jusqu'a `tentatives-max` fois, avec une pause
 *    de `attente-ms` entre deux tentatives (couvre une panne reseau passagere).
 * 2. CIRCUIT BREAKER : au-dela de `seuil-echecs` echecs consecutifs, le circuit
 *    s'ouvre et plus aucun appel ne part vers service-flotte pendant
 *    `duree-ouverture-ms` (fail fast). Ensuite un appel d'essai teste le retour.
 * 3. FALLBACK : si l'appel echoue ou si le circuit est ouvert, on repond quand meme,
 *    soit avec la derniere valeur connue (mode CACHE), soit avec une valeur de
 *    secours fixe (mode STRICT).
 *
 * Un 404 n'est pas une panne : service-flotte a repondu, le vehicule n'existe pas.
 * Il n'est donc ni retente, ni compte comme un echec du disjoncteur.
 */
@Service
public class FlotteResilienceService {

    private static final Logger log = LoggerFactory.getLogger(FlotteResilienceService.class);

    private final FlotteClient flotteClient;
    private final DisjoncteurFlotte disjoncteur;
    private final CacheVehicules cache;
    private final int tentativesMax;
    private final long attenteMs;

    public FlotteResilienceService(
            FlotteClient flotteClient,
            DisjoncteurFlotte disjoncteur,
            CacheVehicules cache,
            @Value("${resilience.flotte.tentatives-max:3}") int tentativesMax,
            @Value("${resilience.flotte.attente-ms:200}") long attenteMs) {
        this.flotteClient = flotteClient;
        this.disjoncteur = disjoncteur;
        this.cache = cache;
        this.tentativesMax = tentativesMax;
        this.attenteMs = attenteMs;
    }

    /**
     * Recupere un vehicule chez service-flotte en appliquant retry, disjoncteur et fallback.
     *
     * @param vehiculeId identifiant du vehicule
     * @param mode       CACHE (derniere valeur connue) ou STRICT (valeur de secours fixe)
     */
    public ReponseVehiculeResiliente recupererVehicule(Long vehiculeId, ModeFallback mode) {
        int tentatives = 0;
        String motif = "ERREUR_FLOTTE";

        while (tentatives < tentativesMax) {
            // Le disjoncteur peut refuser l'appel : dans ce cas on ne perd pas de temps
            // a attendre un service qui vient deja d'echouer plusieurs fois.
            if (!disjoncteur.autoriserAppel()) {
                motif = "CIRCUIT_OUVERT";
                log.warn("Circuit OUVERT : appel vers service-flotte evite pour le vehicule {}", vehiculeId);
                break;
            }

            tentatives++;
            try {
                Map<String, Object> vehicule = flotteClient.getVehicleById(vehiculeId);
                disjoncteur.enregistrerSucces();
                cache.enregistrer(vehiculeId, vehicule);
                if (tentatives > 1) {
                    log.info("service-flotte joignable a la tentative {}/{}", tentatives, tentativesMax);
                }
                return new ReponseVehiculeResiliente(vehicule, "FLOTTE", false,
                        disjoncteur.etat().name(), tentatives,
                        "Appel OpenFeign vers service-flotte reussi a la tentative " + tentatives);
            } catch (FeignException.NotFound ex) {
                // Reponse metier valide : pas de retry, pas d'echec de disponibilite.
                disjoncteur.enregistrerSucces();
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Vehicule " + vehiculeId + " introuvable dans service-flotte");
            } catch (Exception ex) {
                disjoncteur.enregistrerEchec();
                motif = "ERREUR_FLOTTE";
                log.warn("Tentative {}/{} vers service-flotte en echec : {}",
                        tentatives, tentativesMax, ex.getMessage());
                if (tentatives < tentativesMax) {
                    pause(attenteMs);
                }
            }
        }

        return fallback(vehiculeId, mode, tentatives, motif);
    }

    /** Construit la reponse de secours : cache d'abord, valeur stricte sinon. */
    private ReponseVehiculeResiliente fallback(Long vehiculeId, ModeFallback mode, int tentatives, String motif) {
        String etat = disjoncteur.etat().name();

        if (mode == ModeFallback.CACHE) {
            Optional<CacheVehicules.Entree> entree = cache.lire(vehiculeId);
            if (entree.isPresent()) {
                log.info("Fallback CACHE pour le vehicule {} (capture du {})", vehiculeId, entree.get().capture());
                return new ReponseVehiculeResiliente(entree.get().vehicule(), "CACHE", true, etat, tentatives,
                        "service-flotte indisponible (" + motif
                                + ") : derniere valeur connue renvoyee depuis le cache");
            }
        }

        Map<String, Object> secours = new LinkedHashMap<>();
        secours.put("id", vehiculeId);
        secours.put("type", "INCONNU");
        secours.put("immatriculation", "INDISPONIBLE");
        secours.put("autonomieKm", 0);
        secours.put("statut", "INDISPONIBLE");
        log.info("Fallback STRICT pour le vehicule {} (mode strict : aucune donnee reelle renvoyee)", vehiculeId);
        return new ReponseVehiculeResiliente(secours, "STRICT", true, etat, tentatives,
                "service-flotte indisponible (" + motif
                        + ") : vehicule de secours strict, aucune donnee reelle");
    }

    private void pause(long millisecondes) {
        if (millisecondes <= 0) {
            return;
        }
        try {
            Thread.sleep(millisecondes);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
