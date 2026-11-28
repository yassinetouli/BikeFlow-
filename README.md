# BikeFlow

BikeFlow est une plateforme de location de vélos et trottinettes construite autour d'une architecture de microservices Spring Boot.

Le projet permet de gérer les utilisateurs, les factures, la flotte de véhicules, les stations, les locations et les notifications. Les services communiquent via une API Gateway, Eureka pour la découverte des services, Spring Cloud Config pour la configuration centralisée et RabbitMQ pour l'envoi asynchrone des notifications.

## Fonctionnalités

- Gestion des utilisateurs et vérification de leur éligibilité
- Gestion des factures et de leur statut
- Gestion des stations, vélos et trottinettes
- Suivi du statut des véhicules : disponible, en location ou en maintenance
- Création, démarrage et clôture des locations
- Calcul du montant selon la distance parcourue
- Historique des locations par utilisateur
- Publication et envoi de notifications par email
- Découverte des services avec Eureka
- Routage centralisé des requêtes avec API Gateway
- Stockage persistant dans des bases MySQL séparées

## Architecture

<<<<<<< HEAD
<img width="2760" height="1480" alt="architecture_microservices_style_original" src="https://github.com/user-attachments/assets/47e874c2-440c-4edf-a8f2-df6848d3cbe8" />

=======
<img width="1600" height="1105" alt="WhatsApp Image 2026-11-25 at 16 47 09" src="https://github.com/user-attachments/assets/45e14bcf-e292-432e-82df-ccd35dab2448" />
>>>>>>> 7005120ac66d5f1e55e58bfcc267e308f96a6063

| Composant | Rôle | Port |
|---|---|---:|
| API Gateway | Point d'entrée des API | 8080 |
| Eureka Server | Registre et découverte des microservices | 8761 |
| Config Server | Configuration externalisée | 8888 |
| Service Utilisateurs | Utilisateurs, factures et notifications email | 8081 |
| Service Locations | Réservation et cycle de vie des locations | 8082 |
| Service Flotte | Stations et véhicules | 8083 |
| Service Notifications | Consommation RabbitMQ et envoi des emails | 8084 |
| MySQL | Bases de données persistantes | 3306 |
| phpMyAdmin | Interface d'administration MySQL | 8085 |

## Diagramme de classes

<img width="2105" height="2198" alt="diagramme_classes (1)" src="https://github.com/user-attachments/assets/7740d740-264c-4a21-b5a2-09ccc42a4ce7" />
>>>>>>> 7005120ac66d5f1e55e58bfcc267e308f96a6063


## Structure du projet

```text
Bike_Flow/
├── Config_server/          # Serveur de configuration Spring Cloud
├── Depot_config/           # Fichiers de configuration centralisée
├── Eureka-server/          # Service Registry Eureka
├── Gateway/                # API Gateway
├── notif_service/          # Service de notifications
├── service-flotte/         # Gestion des stations et véhicules
├── service-locations/      # Gestion des locations
├── service-utilisateurs/   # Gestion des utilisateurs et factures
├── docker/                 # Initialisation des bases MySQL
├── docker-compose.yml
└── docs/
```

## Technologies

- Java 17
- Spring Boot 4
- Spring Cloud Gateway
- Spring Cloud Netflix Eureka
- Spring Cloud Config
- Spring Data JPA
- OpenFeign
- RabbitMQ
- MySQL 8
- Docker Compose
- Maven

## Prérequis

- JDK 17 ou supérieur
- Maven 3.9+
- Docker Desktop
- RabbitMQ sur les ports 5672
- Un serveur SMTP local sur le port 1025, tel que MailHog, pour les notifications

## Bases de données

Docker Compose initialise trois bases indépendantes :

- `bikeflow_utilisateurs`
- `bikeflow_flotte`
- `bikeflow_locations`

Les mots de passe MySQL configurés dans les services utilisent actuellement `123456`. Ils peuvent être adaptés dans les fichiers `application.properties` ou via les variables d'environnement prises en charge.

## Installation et démarrage

### 1. Démarrer MySQL

Depuis la racine du projet :

```bash
docker compose up -d
```

phpMyAdmin est accessible sur `http://localhost:8085`.

### 2. Démarrer RabbitMQ

RabbitMQ doit être disponible localement avec les identifiants utilisés par les services :

```text
Utilisateur : tp
Mot de passe : tp123
```

### 3. Démarrer les services Spring

L'ordre recommandé est le suivant :

1. Eureka Server
2. Config Server
3. Service Utilisateurs
4. Service Flotte
5. Service Locations
6. Service Notifications
7. API Gateway

Sur Windows, démarrer chaque module depuis son dossier :

```powershell
cd Eureka-server
.\mvnw.cmd spring-boot:run
```

Puis répéter la commande dans les autres dossiers avec `.\mvnw.cmd spring-boot:run`.

## Accès aux applications

| Ressource | URL |
|---|---|
| Eureka Dashboard | http://localhost:8761 |
| API Gateway | http://localhost:8080 |
| Config Server | http://localhost:8888 |
| phpMyAdmin | http://localhost:8085 |
| Service Utilisateurs | http://localhost:8081 |
| Service Locations | http://localhost:8082 |
| Service Flotte | http://localhost:8083 |
| Service Notifications | http://localhost:8084 |

## Exemples d'endpoints

```text
GET    /api/users
GET    /api/users/{id}
GET    /api/users/{id}/eligibility

GET    /api/stations
GET    /api/vehicles

GET    /api/rentals
GET    /api/rentals/{id}
GET    /api/rentals/user/{userEmail}
POST   /api/rentals
POST   /api/rentals/{id}/start
POST   /api/rentals/{id}/finish
PATCH  /api/rentals/{id}/status
```

Les collections Postman sont disponibles dans les dossiers `postman` des modules concernés.

## Tests

Exécuter les tests d'un microservice depuis son dossier :

```bash
./mvnw test
```

Sur Windows :

```powershell
.\mvnw.cmd test
```
