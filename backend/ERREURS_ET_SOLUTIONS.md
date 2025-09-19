# Guide de Résolution des Erreurs - Tests d'Intégration

## 🚨 Erreurs Communes et Solutions

### 1. **Erreur: Java non trouvé**
```
Error: JAVA_HOME not found in your environment
```

**Solutions:**
- Installer Java 21 depuis [Oracle](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html) ou [OpenJDK](https://adoptium.net/temurin/releases/?version=21)
- Définir la variable d'environnement `JAVA_HOME`
- Ajouter `%JAVA_HOME%\bin` au PATH

### 2. **Erreur: Maven non trouvé**
```
'mvn' is not recognized as an internal or external command
```

**Solutions:**
- Utiliser le wrapper Maven: `.\mvnw.cmd` au lieu de `mvn`
- Ou installer Maven et l'ajouter au PATH

### 3. **Erreur: Docker non disponible**
```
Could not find a valid Docker environment
```

**Solutions:**
- Installer [Docker Desktop](https://www.docker.com/products/docker-desktop/)
- Démarrer Docker Desktop
- Vérifier que Docker fonctionne: `docker ps`

### 4. **Erreur: Contexte Spring ne se charge pas**
```
Failed to load ApplicationContext
```

**Solutions:**
- Vérifier que toutes les dépendances sont présentes dans `pom.xml`
- Vérifier la configuration dans `application-test.yml`
- Exécuter d'abord `SpringContextTest` pour diagnostiquer

### 5. **Erreur: Base de données non accessible**
```
Connection refused
```

**Solutions:**
- Vérifier que Testcontainers peut accéder à Docker
- Vérifier que PostgreSQL est disponible
- Redémarrer Docker si nécessaire

### 6. **Erreur: Token JWT invalide**
```
JWT signature does not match
```

**Solutions:**
- Vérifier que la clé secrète JWT est correcte dans `application-test.yml`
- S'assurer que la clé est assez longue (minimum 256 bits)

### 7. **Erreur: Package JUnit Platform Suite manquant**
```
java: package org.junit.platform.suite.api does not exist
```

**Solutions:**
- Les dépendances JUnit Platform Suite ont été ajoutées au `pom.xml`
- Alternative: Utiliser `AllIntegrationTests` au lieu de `IntegrationTestSuite`
- Ou exécuter les tests individuellement avec le pattern: `mvn test -Dtest=*IntegrationTest`

### 8. **Erreur: Méthode JWT parserBuilder() introuvable**
```
java: cannot find symbol: method parserBuilder()
```

**Solutions:**
- La version JWT 0.12.3 utilise une API différente
- Utiliser `Jwts.parser().verifyWith(key).build().parseSignedClaims(token)` au lieu de `parserBuilder()`
- S'assurer que la clé secrète JWT est encodée en Base64

## 🔧 Tests de Diagnostic

### Test 1: Vérification de l'environnement
```powershell
# Vérifier Java
java -version

# Vérifier Docker
docker --version
docker ps

# Vérifier Maven Wrapper
.\mvnw.cmd -version
```

### Test 2: Test de base
```powershell
# Exécuter le test de configuration Spring
.\mvnw.cmd test -Dtest=SpringContextTest

# Exécuter le test de base Testcontainers
.\mvnw.cmd test -Dtest=BasicSetupTest
```

### Test 3: Test d'intégration simple
```powershell
# Exécuter un test d'intégration simple
.\mvnw.cmd test -Dtest=SimpleIntegrationTest
```

## 📋 Ordre d'Exécution Recommandé

1. **Vérifier l'environnement** (Java, Docker, Maven)
2. **Exécuter SpringContextTest** (vérifier la configuration Spring)
3. **Exécuter BasicSetupTest** (vérifier Testcontainers)
4. **Exécuter SimpleIntegrationTest** (vérifier l'intégration de base)
5. **Exécuter les tests complets** (AuthIntegrationTest, etc.)

## 🐛 Debug Mode

Pour plus d'informations sur les erreurs:

```powershell
# Mode verbose
.\mvnw.cmd test -Dtest=IntegrationTestSuite -X

# Logs de debug
.\mvnw.cmd test -Dtest=IntegrationTestSuite -Dlogging.level.com.locme=DEBUG

# Logs Spring
.\mvnw.cmd test -Dtest=IntegrationTestSuite -Dlogging.level.org.springframework=DEBUG
```

## 🔍 Vérifications Manuelles

### Vérifier les fichiers de configuration:
- ✅ `pom.xml` - Dépendances Testcontainers présentes
- ✅ `application-test.yml` - Configuration de test correcte
- ✅ `BaseIntegrationTest.java` - Configuration de base
- ✅ `IntegrationTestConfig.java` - Configuration de sécurité

### Vérifier les imports:
- ✅ Tous les imports sont corrects
- ✅ Pas d'imports manquants
- ✅ Versions des dépendances compatibles

## 📞 Support

Si les erreurs persistent:

1. **Vérifier les logs** pour des messages d'erreur spécifiques
2. **Exécuter les tests un par un** pour isoler le problème
3. **Vérifier la configuration Docker** et les permissions
4. **Redémarrer Docker Desktop** si nécessaire

## 🎯 Tests de Validation

Une fois que l'environnement fonctionne, ces tests devraient tous passer:

```powershell
# Test de base
.\mvnw.cmd test -Dtest=BasicSetupTest

# Test de contexte Spring
.\mvnw.cmd test -Dtest=SpringContextTest

# Test d'intégration simple
.\mvnw.cmd test -Dtest=SimpleIntegrationTest

# Tests d'authentification
.\mvnw.cmd test -Dtest=AuthIntegrationTest

# Tous les tests d'intégration (méthode 1)
.\mvnw.cmd test -Dtest=IntegrationTestSuite

# Tous les tests d'intégration (méthode 2 - alternative)
.\mvnw.cmd test -Dtest=*IntegrationTest

# Tous les tests d'intégration (méthode 3 - individuellement)
.\mvnw.cmd test -Dtest=AuthIntegrationTest,VoitureIntegrationTest,ReservationIntegrationTest,PaiementIntegrationTest,FavoriteIntegrationTest
```
