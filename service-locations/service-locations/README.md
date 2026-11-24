# service-locations (service principal) — V1 + V2

Gère le cycle de vie des locations : `RESERVEE → EN_COURS → TERMINEE` (ou `EN_RETARD`).
Port **8082** · Base **bikeflow_locations** (MySQL) · Nom Eureka : **service-locations**

## Ports de l'architecture
| Application | Port |
|---|---|
| Eureka Server | 8761 |
| API Gateway | 8080 |
| service-utilisateurs | 8081 |
| service-locations | 8082 |
| service-flotte | 8083 |
| MySQL / phpMyAdmin | 3306 / 8085 |

## Démarrage (ordre important)
1. MySQL : `docker compose up -d` (à la racine du dépôt, crée `bikeflow_locations`)
2. Eureka Server (8761)
3. `service-locations` : `./mvnw spring-boot:run` (Windows : `mvnw.cmd spring-boot:run`)
4. API Gateway (8080)

Vérifier l'enregistrement : http://localhost:8761 → `SERVICE-LOCATIONS` doit apparaître au statut UP.
Santé du service : http://localhost:8082/actuator/health

## Accès via la Gateway
`http://localhost:8080/api/rentals/**` est routé dynamiquement vers `lb://service-locations`
(voir `gateway-routes-locations.properties` à la racine de ce livrable).

## Tests
```bash
./mvnw test    # H2 en mémoire, Eureka désactivé : aucune infrastructure requise
```
Postman : importer `postman/service-locations.postman_collection.json`
(dossiers A à D = V1 sur le port 8082, dossier E = V2 via la Gateway 8080).

## Endpoints
| Méthode | URL | Description | Codes |
|---|---|---|---|
| POST | /api/rentals | Créer une réservation | 201, 400, 409 |
| GET | /api/rentals | Lister | 200 |
| GET | /api/rentals/{id} | Consulter | 200, 400, 404 |
| GET | /api/rentals/user/{userEmail} | Historique d'un utilisateur | 200 |
| POST | /api/rentals/{id}/start | Démarrer | 200, 404, 409 |
| POST | /api/rentals/{id}/finish | Terminer (calcul du montant) | 200, 400, 404, 409 |
| PATCH | /api/rentals/{id}/status | Changer le statut | 200, 400, 404, 409 |
