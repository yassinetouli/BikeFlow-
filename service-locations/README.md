# service-locations (service principal) — V1

Conforme au diagramme de classes (userEmail, stationDepart, stationArrive ; méthodes reserver / demarrer / terminer).
Gère le cycle de vie des locations : `RESERVEE → EN_COURS → TERMINEE` (ou `EN_RETARD`).
Port : **8082** · Base : **rental_db** (MySQL) · Swagger : http://localhost:8082/swagger-ui.html

## Prérequis
- Java 17, Maven (ou le Maven intégré à IntelliJ)
- MySQL 8 (la base `rental_db` est créée automatiquement au démarrage) :
  ```bash
  docker run -d --name rental-db -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=rental_db -p 3306:3306 mysql:8
  ```

## Lancer
```bash
mvn spring-boot:run
```
Variables optionnelles : `DB_URL`, `DB_USERNAME` (défaut root), `DB_PASSWORD` (défaut root).

## Tests
```bash
mvn test        # utilise H2 en mémoire (mode MySQL), aucune base à installer
```
Postman : importer `postman/service-locations-v1.postman_collection.json` et lancer la collection (Runner).

## Endpoints
| Méthode | URL | Description | Codes |
|---|---|---|---|
| POST | /api/rentals | Créer une réservation | 201, 400, 409 |
| GET | /api/rentals | Lister | 200 |
| GET | /api/rentals/{id} | Consulter | 200, 400, 404 |
| GET | /api/rentals/user/{userEmail} | Historique d'un utilisateur (par email) | 200 |
| POST | /api/rentals/{id}/start | Démarrer | 200, 404, 409 |
| POST | /api/rentals/{id}/finish | Terminer (calcul du montant) | 200, 400, 404, 409 |
| PATCH | /api/rentals/{id}/status | Changer le statut | 200, 400, 404, 409 |

## Règles de gestion appliquées en V1
- RG-04 : un véhicule n'a qu'une seule location active (RESERVEE / EN_COURS / EN_RETARD) → 409 sinon.
- RG-09 : montant = `kmParcourus × rental.tarif-par-km` (2.00 par défaut, grille à valider).
- Les vérifications auprès de service-flotte et service-utilisateurs arriveront en V4 (OpenFeign).
