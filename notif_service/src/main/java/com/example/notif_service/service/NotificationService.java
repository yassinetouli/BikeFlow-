package com.example.notif_service.service;

import com.example.notif_service.dto.Notif_EmailDTO;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Service
public class NotificationService {
    private final JavaMailSender mailSender;
    public NotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    public void envoyerEmail(Notif_EmailDTO.NotificationEmailDto notificationEmailDto) {
        SimpleMailMessage message=new SimpleMailMessage();
        message.setFrom("noreply@ecom.com");
        message.setTo(notificationEmailDto.destinataire());
        message.setText(notificationEmailDto.contenu());
        message.setSubject(notificationEmailDto.objet());
        mailSender.send(message);
    }
}
