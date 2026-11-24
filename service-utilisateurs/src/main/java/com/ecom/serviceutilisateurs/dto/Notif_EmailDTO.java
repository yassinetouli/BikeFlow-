package com.ecom.serviceutilisateurs.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class Notif_EmailDTO {
    public record NotificationEmailDto(
            @NotBlank @Email String destinataire,
            @NotBlank String objet,
            @NotBlank String contenu) {
    }
}
