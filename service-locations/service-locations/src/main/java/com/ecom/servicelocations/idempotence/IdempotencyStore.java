package com.ecom.servicelocations.idempotence;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Idempotence des ecritures (jalon V4).
 *
 * Le client envoie un en-tete `Idempotency-Key` sur POST /api/rentals. Si la
 * meme cle revient, la location deja creee est renvoyee telle quelle au lieu
 * d'en creer une deuxieme.
 */
@Component
public class IdempotencyStore {

    public record Entree(Object corps, Instant expiration) {
    }

    private final Map<String, Entree> entrees = new ConcurrentHashMap<>();
    private final long ttlSecondes;

    public IdempotencyStore(@Value("${idempotence.ttl-secondes:600}") long ttlSecondes) {
        this.ttlSecondes = ttlSecondes;
    }

    public synchronized <T> Optional<T> lire(String cle, Class<T> type) {
        if (cle == null || cle.isBlank()) {
            return Optional.empty();
        }
        Entree entree = entrees.get(cle);
        if (entree == null) {
            return Optional.empty();
        }
        if (Instant.now().isAfter(entree.expiration())) {
            entrees.remove(cle);
            return Optional.empty();
        }
        return type.isInstance(entree.corps()) ? Optional.of(type.cast(entree.corps())) : Optional.empty();
    }

    public synchronized void enregistrer(String cle, Object corps) {
        if (cle == null || cle.isBlank() || corps == null) {
            return;
        }
        entrees.put(cle, new Entree(corps, Instant.now().plusSeconds(ttlSecondes)));
    }

    public int taille() {
        return entrees.size();
    }

    public void vider() {
        entrees.clear();
    }
}
