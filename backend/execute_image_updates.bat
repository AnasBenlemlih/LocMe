@echo off
echo Mise à jour des URLs des images des voitures...
echo.

REM Remplacez ces valeurs par vos paramètres de connexion PostgreSQL
set DB_HOST=localhost
set DB_PORT=5432
set DB_NAME=locme
set DB_USER=postgres
set DB_PASSWORD=password

echo Connexion à la base de données PostgreSQL...
psql -h %DB_HOST% -p %DB_PORT% -U %DB_USER% -d %DB_NAME% -f update_voiture_images.sql

echo.
echo Mise à jour terminée !
pause
