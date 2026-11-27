package com.ecom.serviceflotte.idempotence;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Idempotence des ecritures (jalon V4).
 *
 * Le client envoie un en-tete `Idempotency-Key` sur un POST. Si la meme cle
 * revient (reessai reseau, double clic, retry du client), le service renvoie
 * la reponse deja produite au lieu de creer un deuxieme enregistrement.
 *
 * Le stockage est en memoire avec une duree de vie (`idempotence.ttl-secondes`),
 * ce qui suffit pour couvrir les reessais ; une version persistante mettrait
 * simplement la table a la place de la Map.
 */
@Component
public class IdempotencyStore {

    /** Reponse memorisee pour une cle, avec sa date d'expiration. */
    public record Entree(Object corps, Instant expiration) {
    }

    private final Map<String, Entree> entrees = new ConcurrentHashMap<>();
    private final long ttlSecondes;

    public IdempotencyStore(@Value("${idempotence.ttl-secondes:600}") long ttlSecondes) {
        this.ttlSecondes = ttlSecondes;
    }

    /** Reponse deja produite pour cette cle, si elle existe encore. */
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

    /** Memorise la reponse pour cette cle (sans cle, rien n'est memorise). */
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
