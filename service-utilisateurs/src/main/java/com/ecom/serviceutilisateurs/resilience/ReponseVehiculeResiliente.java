package com.ecom.serviceutilisateurs.resilience;

import java.util.Map;

/**
 * Resultat d'un appel resilient vers service-flotte : il indique d'ou vient la
 * reponse, ce qui permet d'afficher en soutenance si l'on a servi la flotte,
 * le cache ou le fallback strict, et combien de tentatives ont ete faites.
 *
 * @param vehicule    le vehicule renvoye (reel, en cache ou de secours)
 * @param source      FLOTTE (appel direct), CACHE ou STRICT (fallback)
 * @param degrade     true si la reponse vient d'un fallback
 * @param etatCircuit etat du disjoncteur au moment de la reponse
 * @param tentatives  nombre d'appels reellement envoyes a service-flotte
 * @param message     explication lisible de ce qui s'est passe
 */
public record ReponseVehiculeResiliente(
        Map<String, Object> vehicule,
        String source,
        boolean degrade,
        String etatCircuit,
        int tentatives,
        String message) {
}
