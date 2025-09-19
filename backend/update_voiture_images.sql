-- Script pour mettre à jour les URLs des images des voitures
-- Exécuter ce script dans votre base de données PostgreSQL

UPDATE voitures SET image_url = '/api/images/voitures/toyota-corolla.svg' WHERE marque = 'Toyota' AND modele = 'Corolla';
UPDATE voitures SET image_url = '/api/images/voitures/bmw-x3.svg' WHERE marque = 'BMW' AND modele = 'X3';
UPDATE voitures SET image_url = '/api/images/voitures/mercedes-classe-a.svg' WHERE marque = 'Mercedes' AND modele = 'Classe A';
UPDATE voitures SET image_url = '/api/images/voitures/audi-a4.svg' WHERE marque = 'Audi' AND modele = 'A4';
UPDATE voitures SET image_url = '/api/images/voitures/volkswagen-golf.svg' WHERE marque = 'Volkswagen' AND modele = 'Golf';
UPDATE voitures SET image_url = '/api/images/voitures/peugeot-308.svg' WHERE marque = 'Peugeot' AND modele = '308';
UPDATE voitures SET image_url = '/api/images/voitures/renault-clio.svg' WHERE marque = 'Renault' AND modele = 'Clio';
UPDATE voitures SET image_url = '/api/images/voitures/tesla-model3.svg' WHERE marque = 'Tesla' AND modele = 'Model 3';

-- Vérification des mises à jour
SELECT id, marque, modele, image_url FROM voitures ORDER BY marque, modele;
