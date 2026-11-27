package com.example.notif_service.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class NotificationExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(NotificationExceptionHandler.class);

    @ExceptionHandler(MailException.class)
    public ResponseEntity<String> erreurSmtp(MailException ex) {
        logger.error("Échec de l’envoi de l’email via le serveur SMTP", ex);
        return ResponseEntity.status(503)
                .body("Impossible de transmettre l’email au serveur SMTP");
    }
}