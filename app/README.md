# LocMe - Flutter Frontend

Une application Flutter complète pour la plateforme de location de voitures LocMe, compatible avec Android, iOS et Web.

## 🚀 Fonctionnalités

### Authentification
- ✅ Connexion/Déconnexion avec JWT
- ✅ Inscription avec rôles (Client, Société, Admin)
- ✅ Stockage sécurisé des tokens
- ✅ Gestion des sessions

### Client
- ✅ Accueil avec voitures disponibles
- ✅ Recherche et filtrage des voitures
- ✅ Création de réservations
- ✅ Gestion du profil utilisateur
- ✅ Historique des réservations

### Société
- ✅ Tableau de bord société
- ✅ Gestion des voitures (structure de base)
- ✅ Gestion des réservations (structure de base)

### Admin
- ✅ Interface d'administration (structure de base)
- ✅ Gestion des utilisateurs (structure de base)
- ✅ Gestion des sociétés (structure de base)
- ✅ Statistiques (structure de base)

### Paiements
- ✅ Intégration Stripe
- ✅ Interface de paiement sécurisée
- ✅ Gestion des cartes de crédit

## 🏗️ Architecture

```
app/
├── lib/
│   ├── main.dart                  # Point d'entrée
│   ├── core/                      # Services de base
│   │   ├── api_client.dart        # Client HTTP avec intercepteurs
│   │   ├── auth_service.dart      # Service d'authentification
│   │   ├── storage_service.dart   # Stockage sécurisé
│   │   └── payment_service.dart   # Service de paiement Stripe
│   │
│   ├── models/                    # Modèles de données
│   │   ├── user.dart
│   │   ├── societe.dart
│   │   ├── voiture.dart
│   │   ├── reservation.dart
│   │   └── paiement.dart
│   │
│   ├── screens/                   # Écrans de l'application
│   │   ├── auth/                  # Authentification
│   │   ├── client/                # Interface client
│   │   ├── societe/               # Interface société
│   │   ├── admin/                 # Interface admin
│   │   └── payment/               # Paiements
│   │
│   ├── widgets/                   # Widgets réutilisables
│   │   ├── voiture_card.dart
│   │   ├── reservation_card.dart
│   │   └── custom_button.dart
│   │
│   ├── providers/                 # Gestion d'état (Provider)
│   │   ├── auth_provider.dart
│   │   ├── voiture_provider.dart
│   │   └── reservation_provider.dart
│   │
│   └── routes.dart                # Configuration de navigation
│
├── web/                           # Configuration web
│   ├── index.html
│   ├── manifest.json
│   └── favicon.png
│
└── pubspec.yaml                   # Dépendances
```

## 🛠️ Technologies utilisées

- **Flutter** - Framework de développement
- **Provider** - Gestion d'état
- **Go Router** - Navigation
- **Dio** - Client HTTP
- **Flutter Secure Storage** - Stockage sécurisé (mobile)
- **Shared Preferences** - Stockage (web)
- **Flutter Stripe** - Paiements
- **Cached Network Image** - Cache d'images
- **Material Design 3** - Design system

## 📱 Plateformes supportées

- ✅ **Android** - Application native
- ✅ **iOS** - Application native  
- ✅ **Web** - Application web responsive

## 🚀 Installation et démarrage

### Prérequis
- Flutter SDK (>=3.0.0)
- Dart SDK
- Android Studio / VS Code
- Backend LocMe en cours d'exécution

### Installation

1. **Cloner le projet**
```bash
cd app
```

2. **Installer les dépendances**
```bash
flutter pub get
```

3. **Configurer Stripe**
   - Modifier `lib/core/payment_service.dart`
   - Remplacer `publishableKey` par votre clé publique Stripe

4. **Configurer l'API**
   - Modifier `lib/core/api_client.dart`
   - Ajuster `baseUrl` selon votre backend

### Démarrage

**Web:**
```bash
flutter run -d chrome
```

**Android:**
```bash
flutter run -d android
```

**iOS:**
```bash
flutter run -d ios
```

## 🔧 Configuration

### Variables d'environnement

Créez un fichier `.env` à la racine du projet :

```env
API_BASE_URL=http://localhost:8080/api
STRIPE_PUBLISHABLE_KEY=pk_test_your_key_here
```

### Backend

Assurez-vous que le backend Spring Boot est en cours d'exécution sur `http://localhost:8080`.

## 📋 Comptes de démonstration

L'application inclut des comptes de test :

- **Client:** `client@locme.com` / `password`
- **Société:** `societe@locme.com` / `password`  
- **Admin:** `admin@locme.com` / `password`

## 🔐 Sécurité

- ✅ JWT tokens stockés de manière sécurisée
- ✅ Chiffrement des données sensibles
- ✅ Validation des entrées utilisateur
- ✅ Gestion des erreurs sécurisée
- ✅ HTTPS pour les communications

## 🎨 Design

- ✅ Material Design 3
- ✅ Thème sombre/clair automatique
- ✅ Interface responsive (mobile + web)
- ✅ Animations fluides
- ✅ Accessibilité

## 📦 Build et déploiement

### Web
```bash
flutter build web
```

### Android
```bash
flutter build apk --release
```

### iOS
```bash
flutter build ios --release
```

## 🧪 Tests

```bash
# Tests unitaires
flutter test

# Tests d'intégration
flutter test integration_test/
```

## 📝 API Endpoints

L'application communique avec les endpoints suivants :

### Authentification
- `POST /api/auth/login`
- `POST /api/auth/register`

### Voitures
- `GET /api/voitures/disponibles`
- `GET /api/voitures/{id}`
- `POST /api/voitures` (société)

### Réservations
- `POST /api/reservations`
- `GET /api/reservations/user/{id}`
- `PUT /api/reservations/{id}/status`

### Paiements
- `POST /api/payments/checkout`
- `POST /api/payments/confirm`

## 🤝 Contribution

1. Fork le projet
2. Créer une branche feature (`git checkout -b feature/AmazingFeature`)
3. Commit les changements (`git commit -m 'Add some AmazingFeature'`)
4. Push vers la branche (`git push origin feature/AmazingFeature`)
5. Ouvrir une Pull Request

## 📄 Licence

Ce projet est sous licence MIT. Voir le fichier `LICENSE` pour plus de détails.

## 🆘 Support

Pour toute question ou problème :
- Créer une issue sur GitHub
- Contacter l'équipe de développement

---

**LocMe** - Votre partenaire de confiance pour la location de voitures 🚗