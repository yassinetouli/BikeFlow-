package com.ecom.serviceutilisateurs.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserRequestDTO {

    @NotBlank(message = "Le nom ne doit pas etre vide")
    @Size(min = 2, max = 100, message = "Le nom doit comporter entre 2 et 100 caracteres")
    private String nom;

    @NotBlank(message = "L'adresse email ne doit pas etre vide")
    @Email(message = "Le format de l'adresse email est invalide")
    private String email;

    @Size(max = 100, message = "Le moyen de paiement ne doit pas depasser 100 caracteres")
    private String moyenPaiement;

    private Boolean suspendu;

    public UserRequestDTO() {
    }

    public UserRequestDTO(String nom, String email, String moyenPaiement, Boolean suspendu) {
        this.nom = nom;
        this.email = email;
        this.moyenPaiement = moyenPaiement;
        this.suspendu = suspendu;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMoyenPaiement() {
        return moyenPaiement;
    }

    public void setMoyenPaiement(String moyenPaiement) {
        this.moyenPaiement = moyenPaiement;
    }

    public Boolean getSuspendu() {
        return suspendu;
    }

    public void setSuspendu(Boolean suspendu) {
        this.suspendu = suspendu;
    }
}
