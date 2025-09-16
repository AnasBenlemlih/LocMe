# 🚗 LocMe - Application de Location de Voitures

Application backend REST API pour une plateforme de location de voitures développée avec **Java Spring Boot**.

## 🚀 Fonctionnalités

- **Authentification JWT** avec rôles (CLIENT, SOCIETE, ADMIN)
- **Gestion des utilisateurs** et des sociétés de location
- **Catalogue de voitures** avec disponibilité en temps réel
- **Système de réservation** avec gestion des conflits
- **API REST** complète avec validation
- **Base de données PostgreSQL** avec JPA/Hibernate
- **Sécurité** avec Spring Security
- **Gestion d'erreurs** centralisée
- **Documentation API** avec Swagger/OpenAPI

## 🏗️ Architecture

Le projet suit une architecture **package by feature** :

```
src/main/java/com/locme/
├── LocmeApplication.java              # Classe principale
├── auth/                              # Authentification
│   ├── User.java                      # Entité utilisateur
│   ├── Role.java                      # Enum des rôles
│   ├── AuthController.java            # Controller auth
│   ├── AuthService.java               # Service auth
│   ├── JwtService.java                # Service JWT
│   ├── UserRepository.java            # Repository utilisateur
│   └── dto/                           # DTOs d'authentification
├── societe/                           # Gestion des sociétés
│   ├── Societe.java                   # Entité société
│   └── SocieteRepository.java         # Repository société
├── voiture/                           # Gestion des voitures
│   ├── Voiture.java                   # Entité voiture
│   ├── TypeCarburant.java             # Enum carburant
│   ├── TypeTransmission.java          # Enum transmission
│   ├── VoitureController.java         # Controller voitures
│   ├── VoitureService.java            # Service voitures
│   ├── VoitureRepository.java         # Repository voitures
│   └── dto/                           # DTOs voitures
├── reservation/                       # Gestion des réservations
│   ├── Reservation.java               # Entité réservation
│   ├── StatutReservation.java         # Enum statuts
│   ├── ReservationController.java     # Controller réservations
│   ├── ReservationService.java        # Service réservations
│   ├── ReservationRepository.java     # Repository réservations
│   └── dto/                           # DTOs réservations
├── paiement/                          # Gestion des paiements
│   ├── Paiement.java                  # Entité paiement
│   ├── StatutPaiement.java            # Enum statuts
│   ├── MethodePaiement.java           # Enum méthodes
│   └── PaiementRepository.java        # Repository paiements
├── config/                            # Configuration
│   └── SecurityConfig.java            # Configuration sécurité
└── common/                            # Classes communes
    ├── ApiResponse.java               # Réponse API standardisée
    ├── GlobalExceptionHandler.java    # Gestionnaire d'erreurs
    └── exceptions/                    # Exceptions personnalisées
```

## 🛠️ Technologies

- **Java 21**
- **Spring Boot 3.5.5**
- **Spring Security** + JWT
- **Spring Data JPA**
- **PostgreSQL**
- **Maven**
- **Validation** (Bean Validation)
- **Swagger/OpenAPI** (Documentation)

## 📋 Prérequis

- Java 21+
- Maven 3.6+
- PostgreSQL 12+

## ⚙️ Configuration

### 1. Base de données

Créez une base de données PostgreSQL :

```sql
CREATE DATABASE locme;
```

### 2. Variables d'environnement

Configurez les variables suivantes dans `application.yml` ou via des variables d'environnement :

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/locme
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:password}

spring:
  security:
    jwt:
      secret: ${JWT_SECRET:T807GhPnTJoAQ/VHSoIPQ3mb5ZperAGqaj8Rskr/kyo=}
      expiration: 86400000 # 24 heures
```

### 3. Lancement

```bash
# Compilation
mvn clean compile

# Lancement
mvn spring-boot:run
```

L'API sera disponible sur `http://localhost:8080`

## 📚 API Endpoints

### Authentification

| Méthode | Endpoint | Description | Accès |
|---------|----------|-------------|-------|
| POST | `/api/auth/register` | Inscription | Public |
| POST | `/api/auth/login` | Connexion | Public |
| GET | `/api/auth/me` | Profil utilisateur | Authentifié |

### Voitures

| Méthode | Endpoint | Description | Accès |
|---------|----------|-------------|-------|
| GET | `/api/voitures` | Liste toutes les voitures | Public |
| GET | `/api/voitures/disponibles` | **Voitures disponibles** | Public |
| GET | `/api/voitures/{id}` | Détails d'une voiture | Public |
| POST | `/api/voitures` | Créer une voiture | SOCIETE/ADMIN |
| PUT | `/api/voitures/{id}` | Modifier une voiture | SOCIETE/ADMIN |
| DELETE | `/api/voitures/{id}` | Supprimer une voiture | SOCIETE/ADMIN |

### Réservations

| Méthode | Endpoint | Description | Accès |
|---------|----------|-------------|-------|
| GET | `/api/reservations` | Toutes les réservations | ADMIN |
| GET | `/api/reservations/my-reservations` | Mes réservations | Authentifié |
| GET | `/api/reservations/{id}` | Détails réservation | Authentifié |
| POST | `/api/reservations` | Créer réservation | CLIENT/ADMIN |
| PUT | `/api/reservations/{id}/status` | Modifier statut | SOCIETE/ADMIN |
| DELETE | `/api/reservations/{id}` | Supprimer réservation | Propriétaire/ADMIN |

## 🔐 Authentification

### Inscription

```json
POST /api/auth/register
{
  "nom": "John Doe",
  "email": "john@example.com",
  "motDePasse": "password123",
  "role": "CLIENT",
  "telephone": "0123456789",
  "adresse": "123 Rue Example, Paris"
}
```

### Connexion

```json
POST /api/auth/login
{
  "email": "john@example.com",
  "motDePasse": "password123"
}
```

### Utilisation du token

Ajoutez le header d'autorisation :

```
Authorization: Bearer <votre-jwt-token>
```

## 🚗 Exemples d'utilisation

### 1. Récupérer les voitures disponibles

```bash
GET /api/voitures/disponibles
```

Avec filtres :
```bash
GET /api/voitures/disponibles?marque=BMW&prixMin=50&prixMax=100
```

Réponse :
```json
{
  "success": true,
  "message": "Opération réussie",
  "data": [
    {
      "id": 1,
      "marque": "Toyota",
      "modele": "Corolla",
      "prixParJour": 45.00,
      "disponible": true,
      "annee": 2022,
      "kilometrage": 15000,
      "carburant": "ESSENCE",
      "transmission": "MANUELLE",
      "nombrePlaces": 5,
      "societeId": 1,
      "societeNom": "AutoRent Paris"
    }
  ]
}
```

### 2. Créer une réservation

```bash
POST /api/reservations
Authorization: Bearer <token>

{
  "voitureId": 1,
  "dateDebut": "2024-01-15",
  "dateFin": "2024-01-20",
  "commentaires": "Réservation pour vacances",
  "lieuPrise": "Aéroport CDG",
  "lieuRetour": "Aéroport CDG"
}
```

## 🧪 Données de test

Le fichier `data.sql` contient des données de test :

- **Utilisateurs** : 
  - admin@locme.com (ADMIN)
  - client@test.com (CLIENT)
  - societe@autorent.com (SOCIETE)
- **Mot de passe** : `password` (hashé)
- **Voitures** : 8 voitures de différentes marques et catégories

## 📖 Documentation API

Une fois l'application lancée, accédez à la documentation Swagger :

- **Swagger UI** : http://localhost:8080/swagger-ui.html
- **API Docs** : http://localhost:8080/api-docs

## 🔧 Développement

### Structure des réponses

Toutes les réponses suivent le format `ApiResponse<T>` :

```json
{
  "success": boolean,
  "message": string,
  "data": T,
  "error": string (si erreur)
}
```

### Gestion des erreurs

Les erreurs sont gérées par `GlobalExceptionHandler` et retournent des codes HTTP appropriés :

- `400` : Erreur de validation ou logique métier
- `401` : Non authentifié
- `403` : Accès refusé
- `404` : Ressource non trouvée
- `500` : Erreur serveur

## 🚀 Commandes Maven

```bash
# Compiler et lancer
mvn clean spring-boot:run

# Créer un JAR
mvn clean package

# Exécuter les tests
mvn test

# Voir l'arbre des dépendances
mvn dependency:tree
```

## 📝 Prochaines étapes

- [ ] Intégration Stripe pour les paiements
- [ ] Tests unitaires et d'intégration
- [ ] Upload d'images pour les voitures
- [ ] Notifications par email
- [ ] Système de notation et avis
- [ ] Géolocalisation des voitures
- [ ] Système de fidélité
- [ ] Dashboard administrateur

## 🤝 Contribution

1. Fork le projet
2. Créez une branche feature (`git checkout -b feature/AmazingFeature`)
3. Committez vos changements (`git commit -m 'Add some AmazingFeature'`)
4. Push vers la branche (`git push origin feature/AmazingFeature`)
5. Ouvrez une Pull Request

## 📄 Licence

Ce projet est sous licence MIT. Voir le fichier `LICENSE` pour plus de détails.

---

**LocMe** - Votre partenaire de confiance pour la location de voitures ! 🚗✨