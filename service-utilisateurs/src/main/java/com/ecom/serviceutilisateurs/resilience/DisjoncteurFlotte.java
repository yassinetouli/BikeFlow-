package com.ecom.serviceutilisateurs.resilience;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Disjoncteur (circuit breaker) des appels vers service-flotte, jalon V6.
 *
 * Principe, sans dependance externe :
 * - FERME       : les appels passent ; chaque echec incremente le compteur d'echecs
 *                 consecutifs, chaque succes le remet a zero.
 * - OUVERT      : des que le compteur atteint `seuil-echecs`, on n'appelle plus
 *                 service-flotte pendant `duree-ouverture-ms` (echec rapide = fail fast).
 * - SEMI_OUVERT : le delai ecoule, `essais-semi-ouvert` appels d'essai sont autorises.
 *                 Un succes referme le circuit, un echec le rouvre immediatement.
 *
 * Chaque tentative du retry est comptee comme un echec : trois tentatives ratees
 * ouvrent donc le circuit des le premier appel en panne, ce qui rend la
 * demonstration en soutenance immediate.
 */
@Component
public class DisjoncteurFlotte {

    private static final Logger log = LoggerFactory.getLogger(DisjoncteurFlotte.class);

    private final int seuilEchecs;
    private final long dureeOuvertureMs;
    private final int essaisSemiOuvertAutorises;

    private EtatCircuit etat = EtatCircuit.FERME;
    private int echecsConsecutifs = 0;
    private long ouvertureA = 0L;
    private int essaisSemiOuvert = 0;

    // Compteurs de demonstration, affiches par GET /api/users/resilience/etat
    private long totalAppels = 0L;
    private long totalSucces = 0L;
    private long totalEchecs = 0L;
    private long totalRefus = 0L;

    public DisjoncteurFlotte(
            @Value("${resilience.flotte.seuil-echecs:3}") int seuilEchecs,
            @Value("${resilience.flotte.duree-ouverture-ms:15000}") long dureeOuvertureMs,
            @Value("${resilience.flotte.essais-semi-ouvert:1}") int essaisSemiOuvertAutorises) {
        this.seuilEchecs = seuilEchecs;
        this.dureeOuvertureMs = dureeOuvertureMs;
        this.essaisSemiOuvertAutorises = essaisSemiOuvertAutorises;
    }

    /** Demande l'autorisation de partir vers service-flotte (compte comme un appel). */
    public synchronized boolean autoriserAppel() {
        totalAppels++;
        EtatCircuit courant = etatActuel();

        if (courant == EtatCircuit.FERME) {
            return true;
        }
        if (courant == EtatCircuit.OUVERT) {
            totalRefus++;
            return false;
        }
        // SEMI_OUVERT : on limite le nombre d'appels d'essai
        if (essaisSemiOuvert < essaisSemiOuvertAutorises) {
            essaisSemiOuvert++;
            return true;
        }
        totalRefus++;
        return false;
    }

    /** Appel reussi : le circuit se referme et le compteur d'echecs repart de zero. */
    public synchronized void enregistrerSucces() {
        totalSucces++;
        if (etat != EtatCircuit.FERME) {
            log.info("service-flotte repond de nouveau : circuit referme (FERME)");
        }
        etat = EtatCircuit.FERME;
        echecsConsecutifs = 0;
        essaisSemiOuvert = 0;
        ouvertureA = 0L;
    }

    /** Appel en echec : on incremente le compteur et on ouvre le circuit si le seuil est atteint. */
    public synchronized void enregistrerEchec() {
        totalEchecs++;
        echecsConsecutifs++;

        if (etat == EtatCircuit.SEMI_OUVERT) {
            ouvrir("echec de l'appel d'essai en SEMI_OUVERT");
            return;
        }
        if (echecsConsecutifs >= seuilEchecs) {
            ouvrir(echecsConsecutifs + " echecs consecutifs (seuil " + seuilEchecs + ")");
        }
    }

    /** Etat courant, en appliquant la transition OUVERT -> SEMI_OUVERT si le delai est ecoule. */
    public synchronized EtatCircuit etat() {
        return etatActuel();
    }

    /** Remet le disjoncteur a zero (utilise par la route de demonstration). */
    public synchronized void reinitialiser() {
        etat = EtatCircuit.FERME;
        echecsConsecutifs = 0;
        essaisSemiOuvert = 0;
        ouvertureA = 0L;
        totalAppels = 0L;
        totalSucces = 0L;
        totalEchecs = 0L;
        totalRefus = 0L;
        log.info("Disjoncteur reinitialise : circuit FERME");
    }

    public int echecsConsecutifs() {
        return echecsConsecutifs;
    }

    public int seuilEchecs() {
        return seuilEchecs;
    }

    public long dureeOuvertureMs() {
        return dureeOuvertureMs;
    }

    /** Millisecondes restantes avant l'appel d'essai (0 si le circuit n'est pas ouvert). */
    public synchronized long resteAvantSemiOuvertMs() {
        if (etat != EtatCircuit.OUVERT) {
            return 0L;
        }
        long ecoule = System.currentTimeMillis() - ouvertureA;
        return Math.max(0L, dureeOuvertureMs - ecoule);
    }

    public long totalAppels() {
        return totalAppels;
    }

    public long totalSucces() {
        return totalSucces;
    }

    public long totalEchecs() {
        return totalEchecs;
    }

    public long totalRefus() {
        return totalRefus;
    }

    private EtatCircuit etatActuel() {
        if (etat == EtatCircuit.OUVERT && System.currentTimeMillis() - ouvertureA >= dureeOuvertureMs) {
            etat = EtatCircuit.SEMI_OUVERT;
            essaisSemiOuvert = 0;
            log.info("Delai d'ouverture ecoule : circuit SEMI_OUVERT, un appel d'essai va tester service-flotte");
        }
        return etat;
    }

    private void ouvrir(String motif) {
        etat = EtatCircuit.OUVERT;
        ouvertureA = System.currentTimeMillis();
        essaisSemiOuvert = 0;
        log.warn("Circuit OUVERT ({}) : appels refuses vers service-flotte pendant {} ms", motif, dureeOuvertureMs);
    }
}
