package com.ecom.serviceutilisateurs.dto;

import com.ecom.serviceutilisateurs.entity.Invoice;
import com.ecom.serviceutilisateurs.entity.InvoiceStatus;

import java.time.LocalDate;

public class InvoiceResponseDTO {

    private Long id;
    private Long rentalId;
    private Long userId;
    private Double montant;
    private LocalDate date;
    private LocalDate echeance;
    private InvoiceStatus statut;

    public InvoiceResponseDTO() {
    }

    public InvoiceResponseDTO(Long id, Long rentalId, Long userId, Double montant, LocalDate date, LocalDate echeance, InvoiceStatus statut) {
        this.id = id;
        this.rentalId = rentalId;
        this.userId = userId;
        this.montant = montant;
        this.date = date;
        this.echeance = echeance;
        this.statut = statut;
    }

    public static InvoiceResponseDTO fromEntity(Invoice invoice) {
        if (invoice == null) {
            return null;
        }
        return new InvoiceResponseDTO(
                invoice.getId(),
                invoice.getRentalId(),
                invoice.getUserId(),
                invoice.getMontant(),
                invoice.getDate(),
                invoice.getEcheance(),
                invoice.getStatut()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
