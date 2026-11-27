package com.ecom.servicelocations.exception;

/**
 * Leve quand un service distant (service-flotte) ne peut pas etre joint :
 * la validation inter-services est impossible, la location est refusee en 503.
 */
public class ServiceIndisponibleException extends RuntimeException {

    public ServiceIndisponibleException(String message) {
        super(message);
    }
}
