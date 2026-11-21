package com.ecom.serviceutilisateurs.dto;

import com.ecom.serviceutilisateurs.entity.User;

public class UserResponseDTO {

    private Long id;
    private String nom;
    private String email;
    private String moyenPaiement;
    private boolean suspendu;
    private boolean eligibleLocation;

    public UserResponseDTO() {
    }

    public UserResponseDTO(Long id, String nom, String email, String moyenPaiement, boolean suspendu, boolean eligibleLocation) {
        this.id = id;
        this.nom = nom;
        this.email = email;
        this.moyenPaiement = moyenPaiement;
        this.suspendu = suspendu;
        this.eligibleLocation = eligibleLocation;
    }

    public static UserResponseDTO fromEntity(User user, boolean hasImpayes) {
        if (user == null) {
            return null;
        }
        boolean hasPaymentMethod = user.getMoyenPaiement() != null && !user.getMoyenPaiement().isBlank();
        boolean eligible = !user.isSuspendu() && hasPaymentMethod && !hasImpayes;

        return new UserResponseDTO(
                user.getId(),
                user.getNom(),
                user.getEmail(),
                user.getMoyenPaiement(),
                user.isSuspendu(),
                eligible
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public boolean isSuspendu() {
        return suspendu;
    }

    public void setSuspendu(boolean suspendu) {
        this.suspendu = suspendu;
    }

    public boolean isEligibleLocation() {
        return eligibleLocation;
    }

    public void setEligibleLocation(boolean eligibleLocation) {
        this.eligibleLocation = eligibleLocation;
    }
}
