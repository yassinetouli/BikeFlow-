
package com.example.notif_service.messaging;

import com.example.notif_service.dto.Notif_EmailDTO;
import com.example.notif_service.service.NotificationService;
import com.example.notif_service.config.RabbitConfig;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class NotificationConsumer {

    private final NotificationService notificationService;
    private final Validator validator;

    public NotificationConsumer(
            NotificationService notificationService,
            Validator validator) {
        this.notificationService = notificationService;
        this.validator = validator;
    }

    @RabbitListener(queues = RabbitConfig.QUEUE_EMAIL)
    public void recevoir(
            Notif_EmailDTO.NotificationEmailDto notification) {

        Set<ConstraintViolation<Notif_EmailDTO.NotificationEmailDto>>
                erreurs = validator.validate(notification);

        if (!erreurs.isEmpty()) {
            throw new ConstraintViolationException(erreurs);
        }

        notificationService.envoyerEmail(notification);
    }
}
