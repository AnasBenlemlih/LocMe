#!/usr/bin/env python3
"""
Script de test pour vérifier que les images des voitures sont accessibles
"""

import requests
import os

# URL de base du backend
BASE_URL = "http://localhost:8080"

# Liste des images à tester
IMAGES = [
    "toyota-corolla.svg",
    "bmw-x3.svg", 
    "mercedes-classe-a.svg",
    "audi-a4.svg",
    "volkswagen-golf.svg",
    "peugeot-308.svg",
    "renault-clio.svg",
    "tesla-model3.svg",
    "default-car.svg"
]

def test_image_access():
    """Teste l'accès aux images via l'API"""
    print("🔍 Test d'accès aux images des voitures...")
    print("=" * 50)
    
    success_count = 0
    total_count = len(IMAGES)
    
    for image in IMAGES:
        url = f"{BASE_URL}/api/images/voitures/{image}"
        try:
            response = requests.get(url, timeout=5)
            if response.status_code == 200:
                print(f"✅ {image} - OK ({response.headers.get('content-type', 'unknown')})")
                success_count += 1
            else:
                print(f"❌ {image} - Erreur {response.status_code}")
        except requests.exceptions.RequestException as e:
            print(f"❌ {image} - Erreur de connexion: {e}")
    
    print("=" * 50)
    print(f"📊 Résultat: {success_count}/{total_count} images accessibles")
    
    if success_count == total_count:
        print("🎉 Toutes les images sont accessibles !")
    else:
        print("⚠️  Certaines images ne sont pas accessibles")
        print("💡 Vérifiez que le backend est démarré sur localhost:8080")

def test_voitures_api():
    """Teste l'API des voitures pour vérifier les URLs d'images"""
    print("\n🚗 Test de l'API des voitures...")
    print("=" * 50)
    
    try:
        response = requests.get(f"{BASE_URL}/api/voitures/disponibles", timeout=5)
        if response.status_code == 200:
            data = response.json()
            if data.get('success') and data.get('data'):
                voitures = data['data']
                print(f"📋 {len(voitures)} voitures trouvées")
                
                for voiture in voitures:
                    marque = voiture.get('marque', 'N/A')
                    modele = voiture.get('modele', 'N/A')
                    image_url = voiture.get('imageUrl', 'N/A')
                    print(f"  🚙 {marque} {modele} - Image: {image_url}")
            else:
                print("❌ Format de réponse inattendu")
        else:
            print(f"❌ Erreur API: {response.status_code}")
    except requests.exceptions.RequestException as e:
        print(f"❌ Erreur de connexion: {e}")

if __name__ == "__main__":
    test_image_access()
    test_voitures_api()
