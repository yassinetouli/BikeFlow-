
package com.ecom.serviceutilisateurs.messaging;

import com.ecom.serviceutilisateurs.config.Rabbitconfig;
import com.ecom.serviceutilisateurs.dto.Notif_EmailDTO;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationPublisher {

    private final RabbitTemplate rabbitTemplate;

    public NotificationPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void envoyer(
            Notif_EmailDTO.NotificationEmailDto notification) {

        try {
            rabbitTemplate.convertAndSend(
                    "",
                    Rabbitconfig.QUEUE_EMAIL,
                    notification
            );

            System.out.println("Message envoyé à RabbitMQ");

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
