-- Données de test pour l'application LocMe

-- Insertion d'utilisateurs de test
INSERT INTO users (nom, email, mot_de_passe, role, telephone, adresse, created_at, updated_at) VALUES
('Admin LocMe', 'admin@locme.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'ADMIN', '0123456789', '123 Rue Admin, Paris', NOW(), NOW()),
('Client Test', 'client@test.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'CLIENT', '0987654321', '456 Avenue Client, Lyon', NOW(), NOW()),
('Société AutoRent', 'societe@autorent.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'SOCIETE', '0555666777', '789 Boulevard Société, Marseille', NOW(), NOW()),
('Société CarFast', 'contact@carfast.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'SOCIETE', '0444555666', '321 Rue CarFast, Toulouse', NOW(), NOW());

-- Insertion de sociétés de test
INSERT INTO societes (nom, adresse, email, telephone, description, user_id, created_at, updated_at) VALUES
('AutoRent Paris', '123 Avenue des Champs-Élysées, 75008 Paris', 'contact@autorent.com', '0145678901', 'Location de voitures premium à Paris', 3, NOW(), NOW()),
('CarFast Toulouse', '456 Rue de la République, 31000 Toulouse', 'contact@carfast.com', '0561234567', 'Location de voitures rapides et fiables', 4, NOW(), NOW());

-- Insertion de voitures de test
INSERT INTO voitures (marque, modele, prix_par_jour, disponible, annee, kilometrage, carburant, transmission, nombre_places, description, societe_id, created_at, updated_at) VALUES
('Toyota', 'Corolla', 45.00, true, 2022, 15000, 'ESSENCE', 'MANUELLE', 5, 'Voiture économique et fiable, parfaite pour la ville', 1, NOW(), NOW()),
('BMW', 'X3', 85.00, true, 2023, 8000, 'ESSENCE', 'AUTOMATIQUE', 5, 'SUV premium avec toutes les options', 1, NOW(), NOW()),
('Mercedes', 'Classe A', 75.00, true, 2022, 12000, 'ESSENCE', 'AUTOMATIQUE', 5, 'Berline compacte luxueuse', 1, NOW(), NOW()),
('Audi', 'A4', 80.00, true, 2023, 5000, 'DIESEL', 'AUTOMATIQUE', 5, 'Berline sportive et confortable', 1, NOW(), NOW()),
('Volkswagen', 'Golf', 50.00, true, 2022, 18000, 'ESSENCE', 'MANUELLE', 5, 'Compacte polyvalente et économique', 1, NOW(), NOW()),
('Peugeot', '308', 40.00, true, 2021, 25000, 'DIESEL', 'MANUELLE', 5, 'Voiture familiale spacieuse', 2, NOW(), NOW()),
('Renault', 'Clio', 35.00, true, 2022, 20000, 'ESSENCE', 'MANUELLE', 5, 'Citadine parfaite pour la ville', 2, NOW(), NOW()),
('Tesla', 'Model 3', 120.00, true, 2023, 3000, 'ELECTRIQUE', 'AUTOMATIQUE', 5, 'Voiture électrique haute performance', 2, NOW(), NOW());