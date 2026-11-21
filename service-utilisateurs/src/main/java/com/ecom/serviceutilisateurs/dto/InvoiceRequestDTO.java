package com.ecom.serviceutilisateurs.dto;

import com.ecom.serviceutilisateurs.entity.InvoiceStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class InvoiceRequestDTO {

    @NotNull(message = "L'identifiant de location est obligatoire")
    private Long rentalId;

    @NotNull(message = "L'identifiant de l'utilisateur est obligatoire")
    private Long userId;

    @NotNull(message = "Le montant est obligatoire")
    @Min(value = 0, message = "Le montant doit etre positif ou nul")
    private Double montant;

    private LocalDate date;

    private LocalDate echeance;

    private InvoiceStatus statut;

    public InvoiceRequestDTO() {
    }

    public InvoiceRequestDTO(Long rentalId, Long userId, Double montant, LocalDate date, LocalDate echeance, InvoiceStatus statut) {
        this.rentalId = rentalId;
        this.userId = userId;
        this.montant = montant;
        this.date = date;
        this.echeance = echeance;
        this.statut = statut;
    }

    public Long getRentalId() {
        return rentalId;
    }

    public void setRentalId(Long rentalId) {
        this.rentalId = rentalId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Double getMontant() {
        return montant;
    }

    public void setMontant(Double montant) {
        this.montant = montant;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalDate getEcheance() {
        return echeance;
    }

    public void setEcheance(LocalDate echeance) {
        this.echeance = echeance;
    }

    public InvoiceStatus getStatut() {
        return statut;
    }

    public void setStatut(InvoiceStatus statut) {
        this.statut = statut;
    }
}
