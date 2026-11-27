package com.ecom.servicelocations.service;

import com.ecom.servicelocations.client.FlotteClient;
import com.ecom.servicelocations.exception.BusinessException;
import com.ecom.servicelocations.exception.ResourceNotFoundException;
import com.ecom.servicelocations.exception.ServiceIndisponibleException;

import feign.FeignException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Validation inter-services (jalon V4) : avant d'ecrire une location,
 * service-locations verifie aupres de service-flotte que le vehicule existe
 * et qu'il est disponible.
 *
 * Traduction des reponses de la flotte :
 * - 404  -> ResourceNotFoundException (404 cote locations) ;
 * - autre erreur ou service injoignable -> ServiceIndisponibleException (503) ;
 * - statut different de DISPONIBLE -> BusinessException (409).
 */
@Service
public class VerificationVehicule {

    private static final Logger log = LoggerFactory.getLogger(VerificationVehicule.class);

    private final FlotteClient flotteClient;

    public VerificationVehicule(FlotteClient flotteClient) {
        this.flotteClient = flotteClient;
    }

    /** Renvoie le vehicule valide par service-flotte, ou leve une exception adaptee. */
    public Map<String, Object> verifierDisponibilite(Long vehiculeId) {
        Map<String, Object> vehicule;
        try {
            vehicule = flotteClient.getVehicleById(vehiculeId);
        } catch (FeignException ex) {
            if (ex.status() == 404) {
                throw new ResourceNotFoundException(
                        "Vehicule " + vehiculeId + " introuvable dans service-flotte");
            }
            log.warn("service-flotte injoignable pour le vehicule {} : {}", vehiculeId, ex.getMessage());
            throw new ServiceIndisponibleException(
                    "Validation inter-services impossible : service-flotte ne repond pas");
        } catch (Exception ex) {
            log.warn("service-flotte injoignable pour le vehicule {} : {}", vehiculeId, ex.getMessage());
            throw new ServiceIndisponibleException(
                    "Validation inter-services impossible : service-flotte ne repond pas");
        }

        Object statut = vehicule.get("statut");
        if (!"DISPONIBLE".equals(String.valueOf(statut))) {
            throw new BusinessException("Le vehicule " + vehiculeId
                    + " n'est pas disponible dans la flotte (statut " + statut + ")");
        }
        return vehicule;
    }
}
