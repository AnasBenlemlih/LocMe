#!/usr/bin/env python3
"""
Script pour générer toutes les images de voitures
"""

cars = [
    ("BMW", "X3", "bmw-x3.svg"),
    ("Mercedes", "Classe A", "mercedes-classe-a.svg"),
    ("Audi", "A4", "audi-a4.svg"),
    ("Volkswagen", "Golf", "volkswagen-golf.svg"),
    ("Peugeot", "308", "peugeot-308.svg"),
    ("Renault", "Clio", "renault-clio.svg"),
    ("Tesla", "Model 3", "tesla-model3.svg"),
]

svg_template = '''<svg width="800" height="600" xmlns="http://www.w3.org/2000/svg">
  <defs>
    <linearGradient id="bg" x1="0%" y1="0%" x2="0%" y2="100%">
      <stop offset="0%" style="stop-color:#E8F4FD;stop-opacity:1" />
      <stop offset="100%" style="stop-color:#B8D4F0;stop-opacity:1" />
    </linearGradient>
  </defs>
  
  <!-- Background -->
  <rect width="800" height="600" fill="url(#bg)"/>
  
  <!-- Car body -->
  <rect x="150" y="300" width="500" height="150" fill="#2E86AB" stroke="#1B4F72" stroke-width="3"/>
  
  <!-- Car roof -->
  <polygon points="200,300 400,200 600,300" fill="#1B4F72"/>
  
  <!-- Wheels -->
  <circle cx="200" cy="450" r="30" fill="#2C3E50" stroke="#1B4F72" stroke-width="2"/>
  <circle cx="600" cy="450" r="30" fill="#2C3E50" stroke="#1B4F72" stroke-width="2"/>
  
  <!-- Headlights -->
  <circle cx="160" cy="330" r="15" fill="#F39C12" stroke="#E67E22" stroke-width="2"/>
  <circle cx="640" cy="330" r="15" fill="#F39C12" stroke="#E67E22" stroke-width="2"/>
  
  <!-- Windows -->
  <rect x="220" y="220" width="360" height="80" fill="#85C1E9" stroke="#1B4F72" stroke-width="2"/>
  
  <!-- Text -->
  <text x="400" y="100" text-anchor="middle" font-family="Arial, sans-serif" font-size="48" font-weight="bold" fill="#1B4F72">{brand}</text>
  <text x="400" y="140" text-anchor="middle" font-family="Arial, sans-serif" font-size="24" fill="#34495E">{model}</text>
</svg>'''

def generate_svg(brand, model, filename):
    """Génère un fichier SVG pour une voiture"""
    content = svg_template.format(brand=brand, model=model)
    with open(filename, 'w', encoding='utf-8') as f:
        f.write(content)
    print(f"✓ Généré: {filename}")

def main():
    """Fonction principale"""
    print("Génération des images SVG pour les voitures...")
    
    for brand, model, filename in cars:
        generate_svg(brand, model, filename)
    
    print(f"\n✓ {len(cars)} images SVG générées avec succès!")

if __name__ == "__main__":
    main()


