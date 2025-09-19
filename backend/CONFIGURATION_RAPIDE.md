# Configuration Rapide - Java et Maven

## 🚨 Problème Actuel
```
Error: JAVA_HOME not found in your environment.
```

## ⚡ Solution Rapide

### Option 1: Installation Java 21 (Recommandée)

1. **Télécharger Java 21:**
   - Aller sur [Adoptium OpenJDK 21](https://adoptium.net/temurin/releases/?version=21)
   - Télécharger "Windows x64 MSI Installer"

2. **Installer Java:**
   - Exécuter le fichier MSI téléchargé
   - Suivre l'assistant d'installation
   - **Important:** Noter le chemin d'installation (généralement `C:\Program Files\Eclipse Adoptium\jdk-21.x.x\`)

3. **Configurer JAVA_HOME:**
   ```powershell
   # Ouvrir PowerShell en tant qu'administrateur
   # Remplacer le chemin par votre installation réelle
   [Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Eclipse Adoptium\jdk-21.0.1.12-hotspot\", "Machine")
   
   # Ajouter Java au PATH
   $currentPath = [Environment]::GetEnvironmentVariable("PATH", "Machine")
   [Environment]::SetEnvironmentVariable("PATH", "$currentPath;%JAVA_HOME%\bin", "Machine")
   ```

4. **Redémarrer PowerShell et tester:**
   ```powershell
   java -version
   echo $env:JAVA_HOME
   ```

### Option 2: Configuration Temporaire (Session Actuelle)

Si vous ne voulez pas installer Java globalement:

```powershell
# Trouver Java sur le système
Get-ChildItem "C:\Program Files" -Name "*java*" -Directory
Get-ChildItem "C:\Program Files (x86)" -Name "*java*" -Directory

# Si Java est trouvé, configurer temporairement
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.1.12-hotspot"
$env:PATH = "$env:PATH;$env:JAVA_HOME\bin"

# Tester
java -version
```

### Option 3: Utiliser un IDE

Si vous utilisez un IDE (IntelliJ IDEA, Eclipse, VS Code):

1. **IntelliJ IDEA:**
   - File → Project Structure → Project → Project SDK
   - Ajouter JDK 21

2. **Eclipse:**
   - Window → Preferences → Java → Installed JREs
   - Ajouter JDK 21

3. **VS Code:**
   - Installer l'extension "Extension Pack for Java"
   - Configurer `java.home` dans les paramètres

## 🧪 Test de Validation

Une fois Java configuré:

```powershell
# Vérifier Java
java -version

# Vérifier JAVA_HOME
echo $env:JAVA_HOME

# Compiler le projet
.\mvnw.cmd compile

# Exécuter les tests
.\mvnw.cmd test -Dtest=SpringContextTest
```

## 🔧 Commandes Alternatives

Si Maven Wrapper ne fonctionne toujours pas:

```powershell
# Utiliser Maven directement (si installé)
mvn compile test-compile

# Ou utiliser l'IDE pour exécuter les tests
# IntelliJ: Clic droit sur la classe de test → Run
# Eclipse: Clic droit sur la classe de test → Run As → JUnit Test
```

## 📋 Checklist de Configuration

- [ ] Java 21 installé
- [ ] JAVA_HOME configuré
- [ ] Java ajouté au PATH
- [ ] PowerShell redémarré
- [ ] `java -version` fonctionne
- [ ] `.\mvnw.cmd -version` fonctionne
- [ ] Docker Desktop installé et démarré

## 🆘 Support

Si vous avez encore des problèmes:

1. **Vérifier les variables d'environnement:**
   ```powershell
   Get-ChildItem Env: | Where-Object Name -like "*JAVA*"
   ```

2. **Vérifier les installations Java:**
   ```powershell
   Get-ItemProperty "HKLM:\SOFTWARE\JavaSoft\JDK" -ErrorAction SilentlyContinue
   ```

3. **Redémarrer l'ordinateur** après configuration des variables d'environnement système
