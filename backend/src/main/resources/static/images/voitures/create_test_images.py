#!/usr/bin/env python3
"""
Script pour créer des images de test pour les voitures
"""

import base64
from PIL import Image, ImageDraw, ImageFont
import os

def create_car_image(brand, model, filename):
    """Crée une image de test pour une voiture"""
    # Créer une image 800x600 avec un fond dégradé
    img = Image.new('RGB', (800, 600), color='white')
    draw = ImageDraw.Draw(img)
    
    # Créer un dégradé de fond
    for y in range(600):
        color_value = int(200 + (y / 600) * 55)  # Dégradé de 200 à 255
        draw.line([(0, y), (800, y)], fill=(color_value, color_value, color_value))
    
    # Dessiner une forme de voiture simple
    # Corps de la voiture
    draw.rectangle([150, 300, 650, 450], fill='#2E86AB', outline='#1B4F72', width=3)
    
    # Toit de la voiture
    draw.polygon([(200, 300), (400, 200), (600, 300)], fill='#1B4F72')
    
    # Roues
    draw.ellipse([180, 420, 220, 460], fill='#2C3E50', outline='#1B4F72', width=2)
    draw.ellipse([580, 420, 620, 460], fill='#2C3E50', outline='#1B4F72', width=2)
    
    # Phares
    draw.ellipse([160, 320, 180, 340], fill='#F39C12', outline='#E67E22', width=2)
    draw.ellipse([620, 320, 640, 340], fill='#F39C12', outline='#E67E22', width=2)
    
    # Vitres
    draw.rectangle([220, 220, 580, 300], fill='#85C1E9', outline='#1B4F72', width=2)
    
    # Ajouter le texte de la marque et modèle
    try:
        # Essayer d'utiliser une police système
        font_large = ImageFont.truetype("arial.ttf", 48)
        font_small = ImageFont.truetype("arial.ttf", 24)
    except:
        # Utiliser la police par défaut si arial n'est pas disponible
        font_large = ImageFont.load_default()
        font_small = ImageFont.load_default()
    
    # Texte de la marque
    brand_bbox = draw.textbbox((0, 0), brand, font=font_large)
    brand_width = brand_bbox[2] - brand_bbox[0]
    draw.text(((800 - brand_width) // 2, 100), brand, fill='#1B4F72', font=font_large)
    
    # Texte du modèle
    model_bbox = draw.textbbox((0, 0), model, font=font_small)
    model_width = model_bbox[2] - model_bbox[0]
    draw.text(((800 - model_width) // 2, 160), model, fill='#34495E', font=font_small)
    
    # Sauvegarder l'image
    img.save(filename, 'JPEG', quality=85)
    print(f"✓ Image créée: {filename}")

def main():
    """Fonction principale"""
    print("Création des images de test pour les voitures...")
    
    # Liste des voitures
    cars = [
        ("Toyota", "Corolla", "toyota-corolla.jpg"),
        ("BMW", "X3", "bmw-x3.jpg"),
        ("Mercedes", "Classe A", "mercedes-classe-a.jpg"),
        ("Audi", "A4", "audi-a4.jpg"),
        ("Volkswagen", "Golf", "volkswagen-golf.jpg"),
        ("Peugeot", "308", "peugeot-308.jpg"),
        ("Renault", "Clio", "renault-clio.jpg"),
        ("Tesla", "Model 3", "tesla-model3.jpg"),
        ("Default", "Car", "default-car.jpg")
    ]
    
    for brand, model, filename in cars:
        create_car_image(brand, model, filename)
    
    print(f"\n✓ {len(cars)} images créées avec succès!")

if __name__ == "__main__":
    main()

