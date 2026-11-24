package com.ecom.servicelocations.model;

/** Cycle de vie d'une location : RESERVEE -> EN_COURS -> TERMINEE (ou EN_RETARD). */
public enum RentalStatus {
    RESERVEE,
    EN_COURS,
    TERMINEE,
    EN_RETARD
}
