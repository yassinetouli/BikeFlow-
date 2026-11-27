package com.ecom.serviceutilisateurs.resilience;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache local des dernieres reponses de service-flotte (jalon V6).
 *
 * Il sert uniquement au fallback : si service-flotte est injoignable, on peut
 * encore renvoyer la derniere valeur connue du vehicule au lieu d'une erreur.
 * Le cache est en memoire (pas de base de donnees) : il se remplit a chaque
 * appel OpenFeign reussi et se vide au redemarrage du service.
 */
@Component
public class CacheVehicules {

    /** Une entree du cache : le vehicule tel que service-flotte l'a renvoye, et la date de capture. */
    public record Entree(Long vehiculeId, Map<String, Object> vehicule, Instant capture) {
    }

    private final Map<Long, Entree> entrees = new ConcurrentHashMap<>();

    /** Garde la reponse d'un appel reussi. */
    public void enregistrer(Long vehiculeId, Map<String, Object> vehicule) {
        if (vehicule != null) {
            entrees.put(vehiculeId, new Entree(vehiculeId, vehicule, Instant.now()));
        }
    }

    /** Derniere valeur connue pour ce vehicule (vide si elle n'a jamais ete chargee). */
    public Optional<Entree> lire(Long vehiculeId) {
        return Optional.ofNullable(entrees.get(vehiculeId));
    }

    public int taille() {
        return entrees.size();
    }

    public void vider() {
        entrees.clear();
    }

    /** Contenu du cache, pour la route de diagnostic du jalon V6. */
    public List<Map<String, Object>> contenu() {
        List<Map<String, Object>> liste = new ArrayList<>();
        for (Entree entree : entrees.values()) {
            Map<String, Object> element = new LinkedHashMap<>();
            element.put("vehiculeId", entree.vehiculeId());
            element.put("captureLe", entree.capture().toString());
            element.put("vehicule", entree.vehicule());
            liste.add(element);
        }
        return liste;
    }
}
