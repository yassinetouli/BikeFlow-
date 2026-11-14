package com.bikeflow.locations.exception;

/** Ressource inexistante -> HTTP 404. */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
