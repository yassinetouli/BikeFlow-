package com.ecom.serviceutilisateurs.resilience;

/**
 * Les deux modes de fallback demandes par la fiche d'evaluation (V6) :
 * - STRICT : on renvoie une valeur de secours fixe, sans aucune donnee reelle ;
 * - CACHE  : on renvoie la derniere valeur connue du vehicule, gardee en memoire.
 */
public enum ModeFallback {
    STRICT,
    CACHE
}
