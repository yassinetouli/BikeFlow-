package com.example.notif_service.Controller;

import com.example.notif_service.dto.Notif_EmailDTO;
import com.example.notif_service.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notifications")
public class NotificationController {
    private final NotificationService notificationService;
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
    @PostMapping("/email")
    public ResponseEntity<Void> envoyer(
            @Valid @RequestBody Notif_EmailDTO.NotificationEmailDto notification) {
        notificationService.envoyerEmail(notification);
        return ResponseEntity.noContent().build();
    }
}