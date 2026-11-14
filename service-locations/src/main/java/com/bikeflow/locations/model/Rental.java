package com.bikeflow.locations.model;

import com.bikeflow.locations.exception.BusinessException;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Une location : 1 utilisateur + 1 véhicule + 1 station de départ + 1 station d'arrivée (RG-01).
 * vehicleId, userEmail, stationDepart et stationArrive référencent des ressources d'AUTRES services :
 * pas de clé étrangère, chaque service gère ses propres données (BNF-02).
 * Méthodes métier conformes au diagramme de classes : reserver(), demarrer(), terminer().
 */
@Entity
@Table(name = "rentals")
public class Rental {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long vehicleId;

    @Column(nullable = false)
    private String userEmail;

    @Column(nullable = false)
    private Long stationDepart;

    /** Renseignée à la restitution. */
    private Long stationArrive;

    private LocalDateTime dateDebut;

    private LocalDateTime dateFin;

    private Double kmParcourus;

    @Column(precision = 10, scale = 2)
    private BigDecimal montant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RentalStatus statut;

    protected Rental() {
        // requis par JPA
    }

    /** reserver() : crée une location au statut RESERVEE. */
    public static Rental reserver(Long vehicleId, String userEmail, Long stationDepart) {
        Rental r = new Rental();
        r.vehicleId = vehicleId;
        r.userEmail = userEmail;
        r.stationDepart = stationDepart;
        r.statut = RentalStatus.RESERVEE;
        return r;
    }

    /** demarrer() : RESERVEE -> EN_COURS. */
    public void demarrer() {
        if (statut != RentalStatus.RESERVEE) {
            throw new BusinessException("Impossible de démarrer une location au statut " + statut);
        }
        this.statut = RentalStatus.EN_COURS;
        this.dateDebut = LocalDateTime.now();
    }

    /** terminer() : EN_COURS / EN_RETARD -> TERMINEE. */
    public void terminer(Long stationArrive, Double kmParcourus, BigDecimal montant) {
        if (statut != RentalStatus.EN_COURS && statut != RentalStatus.EN_RETARD) {
            throw new BusinessException("Impossible de terminer une location au statut " + statut);
        }
        this.stationArrive = stationArrive;
        this.kmParcourus = kmParcourus;
        this.montant = montant;
        this.dateFin = LocalDateTime.now();
        this.statut = RentalStatus.TERMINEE;
    }

    /** EN_COURS -> EN_RETARD. */
    public void marquerEnRetard() {
        if (statut != RentalStatus.EN_COURS) {
            throw new BusinessException("Transition interdite : " + statut + " -> " + RentalStatus.EN_RETARD);
        }
        this.statut = RentalStatus.EN_RETARD;
    }

    public Long getId() { return id; }
    public Long getVehicleId() { return vehicleId; }
    public String getUserEmail() { return userEmail; }
    public Long getStationDepart() { return stationDepart; }
    public Long getStationArrive() { return stationArrive; }
    public LocalDateTime getDateDebut() { return dateDebut; }
    public LocalDateTime getDateFin() { return dateFin; }
    public Double getKmParcourus() { return kmParcourus; }
    public BigDecimal getMontant() { return montant; }
    public RentalStatus getStatut() { return statut; }
}
