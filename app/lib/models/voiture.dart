import 'societe.dart';
import 'user.dart';
import '../core/api_client.dart';

enum TypeCarburant {
  ESSENCE,
  DIESEL,
  HYBRIDE,
  ELECTRIQUE,
  GPL,
}

enum TypeTransmission {
  MANUELLE,
  AUTOMATIQUE,
  SEMI_AUTOMATIQUE,
}

class Voiture {
  final int id;
  final String marque;
  final String modele;
  final double prixParJour;
  final bool disponible;
  final int? annee;
  final int? kilometrage;
  final TypeCarburant? carburant;
  final TypeTransmission? transmission;
  final int? nombrePlaces;
  final String? imageUrl;
  final String? description;
  final Societe societe;
  final DateTime? createdAt;
  final DateTime? updatedAt;

  Voiture({
    required this.id,
    required this.marque,
    required this.modele,
    required this.prixParJour,
    required this.disponible,
    this.annee,
    this.kilometrage,
    this.carburant,
    this.transmission,
    this.nombrePlaces,
    this.imageUrl,
    this.description,
    required this.societe,
    this.createdAt,
    this.updatedAt,
  });

  factory Voiture.fromJson(Map<String, dynamic> json) {
    // Handle different societe structures from backend
    Societe societe;
    if (json['societe'] != null) {
      // Full societe object
      societe = Societe.fromJson(json['societe']);
    } else if (json['societeId'] != null && json['societeNom'] != null) {
      // Simplified societe structure (societeId, societeNom)
      societe = Societe(
        id: json['societeId'],
        nom: json['societeNom'],
        adresse: json['societeAdresse'] ?? '',
        email: json['societeEmail'] ?? '',
        telephone: json['societeTelephone'],
        description: json['societeDescription'],
        logoUrl: json['societeLogoUrl'],
        user: User(
          id: json['societeUserId'] ?? 0,
          nom: json['societeNom'] ?? '',
          email: json['societeEmail'] ?? '',
          role: Role.SOCIETE,
        ),
      );
    } else {
      // Fallback for missing societe data
      societe = Societe(
        id: 0,
        nom: 'Société inconnue',
        adresse: '',
        email: '',
        user: User(
          id: 0,
          nom: 'Société inconnue',
          email: '',
          role: Role.SOCIETE,
        ),
      );
    }

    return Voiture(
      id: json['id'],
      marque: json['marque'],
      modele: json['modele'],
      prixParJour: (json['prixParJour'] as num).toDouble(),
      disponible: json['disponible'] ?? true,
      annee: json['annee'],
      kilometrage: json['kilometrage'],
      carburant: json['carburant'] != null 
          ? TypeCarburant.values.firstWhere(
              (e) => e.name == json['carburant'],
              orElse: () => TypeCarburant.ESSENCE,
            )
          : null,
      transmission: json['transmission'] != null 
          ? TypeTransmission.values.firstWhere(
              (e) => e.name == json['transmission'],
              orElse: () => TypeTransmission.MANUELLE,
            )
          : null,
      nombrePlaces: json['nombrePlaces'],
      imageUrl: json['imageUrl'],
      description: json['description'],
      societe: societe,
      createdAt: json['createdAt'] != null 
          ? DateTime.parse(json['createdAt']) 
          : null,
      updatedAt: json['updatedAt'] != null 
          ? DateTime.parse(json['updatedAt']) 
          : null,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'marque': marque,
      'modele': modele,
      'prixParJour': prixParJour,
      'disponible': disponible,
      'annee': annee,
      'kilometrage': kilometrage,
      'carburant': carburant?.name,
      'transmission': transmission?.name,
      'nombrePlaces': nombrePlaces,
      'imageUrl': imageUrl,
      'description': description,
      'societe': societe.toJson(),
      'createdAt': createdAt?.toIso8601String(),
      'updatedAt': updatedAt?.toIso8601String(),
    };
  }

  Voiture copyWith({
    int? id,
    String? marque,
    String? modele,
    double? prixParJour,
    bool? disponible,
    int? annee,
    int? kilometrage,
    TypeCarburant? carburant,
    TypeTransmission? transmission,
    int? nombrePlaces,
    String? imageUrl,
    String? description,
    Societe? societe,
    DateTime? createdAt,
    DateTime? updatedAt,
  }) {
    return Voiture(
      id: id ?? this.id,
      marque: marque ?? this.marque,
      modele: modele ?? this.modele,
      prixParJour: prixParJour ?? this.prixParJour,
      disponible: disponible ?? this.disponible,
      annee: annee ?? this.annee,
      kilometrage: kilometrage ?? this.kilometrage,
      carburant: carburant ?? this.carburant,
      transmission: transmission ?? this.transmission,
      nombrePlaces: nombrePlaces ?? this.nombrePlaces,
      imageUrl: imageUrl ?? this.imageUrl,
      description: description ?? this.description,
      societe: societe ?? this.societe,
      createdAt: createdAt ?? this.createdAt,
      updatedAt: updatedAt ?? this.updatedAt,
    );
  }

  String get fullName => '$marque $modele';
  
  String? get fullImageUrl {
    if (imageUrl == null) return null;
    if (imageUrl!.startsWith('http')) return imageUrl;
    return '${ApiClient.imageBaseUrl}$imageUrl';
  }
  
  String get carburantDisplayName {
    switch (carburant) {
      case TypeCarburant.ESSENCE:
        return 'Essence';
      case TypeCarburant.DIESEL:
        return 'Diesel';
      case TypeCarburant.HYBRIDE:
        return 'Hybride';
      case TypeCarburant.ELECTRIQUE:
        return 'Électrique';
      case TypeCarburant.GPL:
        return 'GPL';
      default:
        return 'Non spécifié';
    }
  }

  String get transmissionDisplayName {
    switch (transmission) {
      case TypeTransmission.MANUELLE:
        return 'Manuelle';
      case TypeTransmission.AUTOMATIQUE:
        return 'Automatique';
      case TypeTransmission.SEMI_AUTOMATIQUE:
        return 'Semi-automatique';
      default:
        return 'Non spécifié';
    }
  }

  @override
  String toString() {
    return 'Voiture(id: $id, marque: $marque, modele: $modele, prixParJour: $prixParJour)';
  }

  @override
  bool operator ==(Object other) {
    if (identical(this, other)) return true;
    return other is Voiture && other.id == id;
  }

  @override
  int get hashCode => id.hashCode;
}
