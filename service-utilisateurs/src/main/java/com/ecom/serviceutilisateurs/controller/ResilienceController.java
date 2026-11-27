package com.ecom.serviceutilisateurs.controller;

import com.ecom.serviceutilisateurs.resilience.CacheVehicules;
import com.ecom.serviceutilisateurs.resilience.DisjoncteurFlotte;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Routes de demonstration du jalon V6 : elles exposent l'etat du disjoncteur
 * et le contenu du cache pour pouvoir les montrer au tableau pendant la soutenance.
 * Elles passent par la Gateway car elles sont sous /api/users/**.
 */
@RestController
@RequestMapping("/api/users/resilience")
public class ResilienceController {

    private final DisjoncteurFlotte disjoncteur;
    private final CacheVehicules cache;

    public ResilienceController(DisjoncteurFlotte disjoncteur, CacheVehicules cache) {
        this.disjoncteur = disjoncteur;
        this.cache = cache;
    }

    @GetMapping("/etat")
    public Map<String, Object> etat() {
        Map<String, Object> corps = new LinkedHashMap<>();
        corps.put("etatCircuit", disjoncteur.etat().name());
        corps.put("echecsConsecutifs", disjoncteur.echecsConsecutifs());
        corps.put("seuilEchecs", disjoncteur.seuilEchecs());
        corps.put("dureeOuvertureMs", disjoncteur.dureeOuvertureMs());
        corps.put("resteAvantSemiOuvertMs", disjoncteur.resteAvantSemiOuvertMs());
        corps.put("appels", disjoncteur.totalAppels());
        corps.put("succes", disjoncteur.totalSucces());
        corps.put("echecs", disjoncteur.totalEchecs());
        corps.put("refus", disjoncteur.totalRefus());
        corps.put("cacheTaille", cache.taille());
        corps.put("cache", cache.contenu());
        return corps;
    }

    @PostMapping("/reinitialiser")
    public Map<String, String> reinitialiser() {
        disjoncteur.reinitialiser();
        cache.vider();
        return Map.of("message", "Disjoncteur et cache remis a zero");
    }
}
