package com.ecom.serviceflotte.exception;

/**
 * Donnee metier incoherente (ex : placesLibres > capacite) -> HTTP 400.
 * La collection Postman service-flotte attend 400 pour ces cas.
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
