package com.ecom.serviceutilisateurs.dto;

public class UserEligibilityDTO {

    private Long userId;
    private boolean eligible;
    private boolean suspendu;
    private boolean moyenPaiementValide;
    private boolean impayesPresents;
    private String motif;

    public UserEligibilityDTO() {
    }

    public UserEligibilityDTO(Long userId, boolean eligible, boolean suspendu, boolean moyenPaiementValide, boolean impayesPresents, String motif) {
        this.userId = userId;
        this.eligible = eligible;
        this.suspendu = suspendu;
        this.moyenPaiementValide = moyenPaiementValide;
        this.impayesPresents = impayesPresents;
        this.motif = motif;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public boolean isEligible() {
        return eligible;
    }

    public void setEligible(boolean eligible) {
        this.eligible = eligible;
    }

    public boolean isSuspendu() {
        return suspendu;
    }

    public void setSuspendu(boolean suspendu) {
        this.suspendu = suspendu;
    }

    public boolean isMoyenPaiementValide() {
        return moyenPaiementValide;
    }

    public void setMoyenPaiementValide(boolean moyenPaiementValide) {
        this.moyenPaiementValide = moyenPaiementValide;
    }

    public boolean isImpayesPresents() {
        return impayesPresents;
    }

    public void setImpayesPresents(boolean impayesPresents) {
        this.impayesPresents = impayesPresents;
    }

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }
}
