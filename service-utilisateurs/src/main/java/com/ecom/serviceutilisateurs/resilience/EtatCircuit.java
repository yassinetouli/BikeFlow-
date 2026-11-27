package com.ecom.serviceutilisateurs.resilience;

/**
 * Les trois etats du disjoncteur (circuit breaker) du jalon V6.
 */
public enum EtatCircuit {

    /** Tout va bien : les appels vers service-flotte passent normalement. */
    FERME,

    /** Trop d'echecs consecutifs : les appels sont refuses et le fallback repond. */
    OUVERT,

    /** Delai d'ouverture ecoule : un seul appel d'essai est autorise pour tester le retour. */
    SEMI_OUVERT
}
