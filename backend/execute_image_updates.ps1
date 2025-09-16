# Script PowerShell pour mettre à jour les URLs des images des voitures

Write-Host "Mise à jour des URLs des images des voitures..." -ForegroundColor Green
Write-Host ""

# Paramètres de connexion PostgreSQL (modifiez selon votre configuration)
$DB_HOST = "localhost"
$DB_PORT = "5432"
$DB_NAME = "locme"
$DB_USER = "postgres"
$DB_PASSWORD = "password"

try {
    Write-Host "Connexion à la base de données PostgreSQL..." -ForegroundColor Yellow
    
    # Exécuter le script SQL
    $env:PGPASSWORD = $DB_PASSWORD
    psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -f update_voiture_images.sql
    
    Write-Host ""
    Write-Host "Mise à jour terminée avec succès !" -ForegroundColor Green
}
catch {
    Write-Host "Erreur lors de l'exécution : $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
    Write-Host "Vérifiez que :" -ForegroundColor Yellow
    Write-Host "1. PostgreSQL est installé et en cours d'exécution" -ForegroundColor Yellow
    Write-Host "2. La base de données 'locme' existe" -ForegroundColor Yellow
    Write-Host "3. Les paramètres de connexion sont corrects" -ForegroundColor Yellow
    Write-Host "4. L'utilisateur a les permissions nécessaires" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Appuyez sur une touche pour continuer..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
