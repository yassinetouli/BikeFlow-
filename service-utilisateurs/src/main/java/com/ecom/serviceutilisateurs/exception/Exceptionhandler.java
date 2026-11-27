package com.ecom.serviceutilisateurs.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class Exceptionhandler {
    @ExceptionHandler(org.springframework.amqp.AmqpException.class)
    public ResponseEntity<String> rabbit(
            org.springframework.amqp.AmqpException ex) {
        return ResponseEntity.status(503)
                .body("Impossible de transmettre la demande de notification");
    }
}
