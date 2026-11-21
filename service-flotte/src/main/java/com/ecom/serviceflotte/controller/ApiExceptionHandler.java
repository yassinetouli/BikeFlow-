package com.ecom.serviceflotte.controller;

import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

/**
 * Centralise les reponses d'erreur JSON de l'API, en un seul endroit,
 * pour ne jamais renvoyer de pile d'exceptions Java au client.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    /** 400 : le corps JSON n'est pas valide (@Valid a echoue). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return erreur(HttpStatus.BAD_REQUEST, message);
    }

    /** 400 : JSON illisible ou valeur d'enum inconnue (ex: statut inexistant). */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleUnreadable(HttpMessageNotReadableException ex) {
        return erreur(HttpStatus.BAD_REQUEST, "Corps de la requete invalide (JSON ou valeur d'enum incorrecte)");
    }

    /** 404 (introuvable), 409 (conflit), 400 (regle metier) leves par les services. */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(ResponseStatusException ex) {
        return erreur(HttpStatus.resolve(ex.getStatusCode().value()), ex.getReason());
    }

    /** 409 : la base de donnees a refuse l'ecriture (ex: index unique immatriculation). */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleIntegrity(DataIntegrityViolationException ex) {
        return erreur(HttpStatus.CONFLICT, "La requete viole une contrainte de la base de donnees");
    }

    private ResponseEntity<Map<String, Object>> erreur(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("status", status.value(), "message", message));
    }
}
