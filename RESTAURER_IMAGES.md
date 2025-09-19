# 🖼️ Guide de restauration des images des voitures

## 📋 Problème
Les images des voitures ont été perdues et les URLs dans la base de données ne pointent plus vers les bonnes images.

## ✅ Solution

### 1. Vérification des images
Les images sont présentes dans : `backend/src/main/resources/static/images/voitures/`
- ✅ toyota-corolla.svg
- ✅ bmw-x3.svg
- ✅ mercedes-classe-a.svg
- ✅ audi-a4.svg
- ✅ volkswagen-golf.svg
- ✅ peugeot-308.svg
- ✅ renault-clio.svg
- ✅ tesla-model3.svg
- ✅ default-car.svg

### 2. Mise à jour de la base de données

#### Option A : Script PowerShell (Windows)
```powershell
cd backend
.\execute_image_updates.ps1
```

#### Option B : Script Bash (Linux/Mac)
```bash
cd backend
chmod +x execute_image_updates.sh
./execute_image_updates.sh
```

#### Option C : Exécution manuelle
Connectez-vous à votre base de données PostgreSQL et exécutez :
```sql
UPDATE voitures SET image_url = '/api/images/voitures/toyota-corolla.svg' WHERE marque = 'Toyota' AND modele = 'Corolla';
UPDATE voitures SET image_url = '/api/images/voitures/bmw-x3.svg' WHERE marque = 'BMW' AND modele = 'X3';
UPDATE voitures SET image_url = '/api/images/voitures/mercedes-classe-a.svg' WHERE marque = 'Mercedes' AND modele = 'Classe A';
UPDATE voitures SET image_url = '/api/images/voitures/audi-a4.svg' WHERE marque = 'Audi' AND modele = 'A4';
UPDATE voitures SET image_url = '/api/images/voitures/volkswagen-golf.svg' WHERE marque = 'Volkswagen' AND modele = 'Golf';
UPDATE voitures SET image_url = '/api/images/voitures/peugeot-308.svg' WHERE marque = 'Peugeot' AND modele = '308';
UPDATE voitures SET image_url = '/api/images/voitures/renault-clio.svg' WHERE marque = 'Renault' AND modele = 'Clio';
UPDATE voitures SET image_url = '/api/images/voitures/tesla-model3.svg' WHERE marque = 'Tesla' AND modele = 'Model 3';
```

### 3. Démarrage du backend
```bash
cd backend
mvn spring-boot:run
```

### 4. Test des images
```bash
cd backend
python test_images.py
```

## 🔧 Configuration requise

### Base de données PostgreSQL
- Host: localhost
- Port: 5432
- Database: locme
- User: postgres
- Password: password

### Backend Spring Boot
- Port: 8080
- CORS activé pour toutes les origines

## 🧪 Vérification

### URLs des images
Les images seront accessibles via :
- http://localhost:8080/api/images/voitures/toyota-corolla.svg
- http://localhost:8080/api/images/voitures/bmw-x3.svg
- etc.

### API des voitures
- http://localhost:8080/api/voitures/disponibles

## 🚨 Dépannage

### Images non accessibles
1. Vérifiez que le backend est démarré
2. Vérifiez les logs du backend
3. Testez l'accès direct aux URLs

### Erreurs de base de données
1. Vérifiez la connexion PostgreSQL
2. Vérifiez que la table `voitures` existe
3. Vérifiez les permissions de l'utilisateur

### CORS
Le contrôleur d'images est configuré avec `@CrossOrigin(origins = "*")` pour permettre l'accès depuis le frontend.

## 📱 Frontend
L'application Flutter est déjà configurée pour afficher les images via l'API. Les URLs sont construites automatiquement dans le modèle `Voiture` avec la propriété `fullImageUrl`.
