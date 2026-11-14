package com.ecom.servicelocations.exception;

/** Règle de gestion violée -> HTTP 409. */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
