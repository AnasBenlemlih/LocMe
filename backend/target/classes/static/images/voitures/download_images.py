#!/usr/bin/env python3
"""
Script pour télécharger des images d'exemple de voitures
"""

import urllib.request
import os

# URLs des images d'exemple (images libres de droits)
images = {
    "toyota-corolla.jpg": "https://images.unsplash.com/photo-1621007947382-bb3c3994e3fb?w=800&h=600&fit=crop&crop=center",
    "bmw-x3.jpg": "https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800&h=600&fit=crop&crop=center",
    "mercedes-classe-a.jpg": "https://images.unsplash.com/photo-1618843479313-40f8afb4b4d8?w=800&h=600&fit=crop&crop=center",
    "audi-a4.jpg": "https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop&crop=center",
    "volkswagen-golf.jpg": "https://images.unsplash.com/photo-1549317336-206569e8475c?w=800&h=600&fit=crop&crop=center",
    "peugeot-308.jpg": "https://images.unsplash.com/photo-1552519507-da3b142c6e3d?w=800&h=600&fit=crop&crop=center",
    "renault-clio.jpg": "https://images.unsplash.com/photo-1544636331-e26879cd4d9b?w=800&h=600&fit=crop&crop=center",
    "tesla-model3.jpg": "https://images.unsplash.com/photo-1560958089-b8a1929cea89?w=800&h=600&fit=crop&crop=center",
    "default-car.jpg": "https://images.unsplash.com/photo-1492144534655-ae79c964c9d7?w=800&h=600&fit=crop&crop=center"
}

def download_image(url, filename):
    """Télécharge une image depuis une URL"""
    try:
        print(f"Téléchargement de {filename}...")
        urllib.request.urlretrieve(url, filename)
        print(f"✓ {filename} téléchargé avec succès")
        return True
    except Exception as e:
        print(f"✗ Erreur lors du téléchargement de {filename}: {e}")
        return False

def main():
    """Fonction principale"""
    print("Téléchargement des images de voitures...")
    
    # Créer le dossier s'il n'existe pas
    os.makedirs(".", exist_ok=True)
    
    success_count = 0
    total_count = len(images)
    
    for filename, url in images.items():
        if download_image(url, filename):
            success_count += 1
    
    print(f"\nTéléchargement terminé: {success_count}/{total_count} images téléchargées avec succès")

if __name__ == "__main__":
    main()
