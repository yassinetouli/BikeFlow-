package com.ecom.serviceflotte.exception;

/** Regle de gestion violee (ex : immatriculation deja utilisee) -> HTTP 409. */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
