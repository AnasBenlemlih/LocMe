#!/bin/bash

echo "Mise à jour des URLs des images des voitures..."
echo

# Remplacez ces valeurs par vos paramètres de connexion PostgreSQL
DB_HOST=localhost
DB_PORT=5432
DB_NAME=locme
DB_USER=postgres
DB_PASSWORD=password

echo "Connexion à la base de données PostgreSQL..."
PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -f update_voiture_images.sql

echo
echo "Mise à jour terminée !"
