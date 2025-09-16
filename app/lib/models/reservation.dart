import 'user.dart';
import 'voiture.dart';
import 'societe.dart';

enum StatutReservation {
  EN_ATTENTE,
  CONFIRMEE,
  EN_COURS,
  TERMINEE,
  ANNULEE,
  REFUSEE,
}

class Reservation {
  final int id;
  final Voiture voiture;
  final User user;
  final DateTime dateDebut;
  final DateTime dateFin;
  final StatutReservation statut;
  final double montant;
  final String? commentaires;
  final String? lieuPrise;
  final String? lieuRetour;
  final DateTime? createdAt;
  final DateTime? updatedAt;

  Reservation({
    required this.id,
    required this.voiture,
    required this.user,
    required this.dateDebut,
    required this.dateFin,
    required this.statut,
    required this.montant,
    this.commentaires,
    this.lieuPrise,
    this.lieuRetour,
    this.createdAt,
    this.updatedAt,
  });

  factory Reservation.fromJson(Map<String, dynamic> json) {
    // Handle different voiture structures from backend
    Voiture voiture;
    if (json['voiture'] != null) {
      // Full voiture object
      voiture = Voiture.fromJson(json['voiture']);
    } else if (json['voitureId'] != null && json['voitureMarque'] != null) {
      // Simplified voiture structure (voitureId, voitureMarque, voitureModele)
      voiture = Voiture(
        id: json['voitureId'],
        marque: json['voitureMarque'],
        modele: json['voitureModele'],
        prixParJour: (json['voiturePrixParJour'] as num?)?.toDouble() ?? 0.0,
        disponible: json['voitureDisponible'] ?? true,
        annee: json['voitureAnnee'],
        kilometrage: json['voitureKilometrage'],
        carburant: json['voitureCarburant'] != null 
            ? TypeCarburant.values.firstWhere(
                (e) => e.name == json['voitureCarburant'],
                orElse: () => TypeCarburant.ESSENCE,
              )
            : null,
        transmission: json['voitureTransmission'] != null 
            ? TypeTransmission.values.firstWhere(
                (e) => e.name == json['voitureTransmission'],
                orElse: () => TypeTransmission.MANUELLE,
              )
            : null,
        nombrePlaces: json['voitureNombrePlaces'],
        imageUrl: json['voitureImageUrl'],
        description: json['voitureDescription'],
        societe: Societe(
          id: json['voitureSocieteId'] ?? 0,
          nom: json['voitureSocieteNom'] ?? 'Société inconnue',
          adresse: json['voitureSocieteAdresse'] ?? '',
          email: json['voitureSocieteEmail'] ?? '',
          telephone: json['voitureSocieteTelephone'],
          description: json['voitureSocieteDescription'],
          logoUrl: json['voitureSocieteLogoUrl'],
          user: User(
            id: json['voitureSocieteUserId'] ?? 0,
            nom: json['voitureSocieteNom'] ?? 'Société inconnue',
            email: json['voitureSocieteEmail'] ?? '',
            role: Role.SOCIETE,
          ),
        ),
      );
    } else {
      // Fallback for missing voiture data
      voiture = Voiture(
        id: 0,
        marque: 'Voiture inconnue',
        modele: 'Modèle inconnu',
        prixParJour: 0.0,
        disponible: false,
        societe: Societe(
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
        ),
      );
    }

    // Handle different user structures from backend
    User user;
    if (json['user'] != null) {
      // Full user object
      user = User.fromJson(json['user']);
    } else if (json['userId'] != null && json['userNom'] != null) {
      // Simplified user structure (userId, userNom)
      user = User(
        id: json['userId'],
        nom: json['userNom'],
        email: json['userEmail'] ?? '',
        role: Role.values.firstWhere(
          (e) => e.name == json['userRole'],
          orElse: () => Role.CLIENT,
        ),
        telephone: json['userTelephone'],
        adresse: json['userAdresse'],
      );
    } else {
      // Fallback for missing user data
      user = User(
        id: 0,
        nom: 'Utilisateur inconnu',
        email: '',
        role: Role.CLIENT,
      );
    }

    return Reservation(
      id: json['id'],
      voiture: voiture,
      user: user,
      dateDebut: DateTime.parse(json['dateDebut']),
      dateFin: DateTime.parse(json['dateFin']),
      statut: StatutReservation.values.firstWhere(
        (e) => e.name == json['statut'],
        orElse: () => StatutReservation.EN_ATTENTE,
      ),
      montant: (json['montant'] as num).toDouble(),
      commentaires: json['commentaires'],
      lieuPrise: json['lieuPrise'],
      lieuRetour: json['lieuRetour'],
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
      'voiture': voiture.toJson(),
      'user': user.toJson(),
      'dateDebut': dateDebut.toIso8601String().split('T')[0],
      'dateFin': dateFin.toIso8601String().split('T')[0],
      'statut': statut.name,
      'montant': montant,
      'commentaires': commentaires,
      'lieuPrise': lieuPrise,
      'lieuRetour': lieuRetour,
      'createdAt': createdAt?.toIso8601String(),
      'updatedAt': updatedAt?.toIso8601String(),
    };
  }

  Reservation copyWith({
    int? id,
    Voiture? voiture,
    User? user,
    DateTime? dateDebut,
    DateTime? dateFin,
    StatutReservation? statut,
    double? montant,
    String? commentaires,
    String? lieuPrise,
    String? lieuRetour,
    DateTime? createdAt,
    DateTime? updatedAt,
  }) {
    return Reservation(
      id: id ?? this.id,
      voiture: voiture ?? this.voiture,
      user: user ?? this.user,
      dateDebut: dateDebut ?? this.dateDebut,
      dateFin: dateFin ?? this.dateFin,
      statut: statut ?? this.statut,
      montant: montant ?? this.montant,
      commentaires: commentaires ?? this.commentaires,
      lieuPrise: lieuPrise ?? this.lieuPrise,
      lieuRetour: lieuRetour ?? this.lieuRetour,
      createdAt: createdAt ?? this.createdAt,
      updatedAt: updatedAt ?? this.updatedAt,
    );
  }

  int get dureeEnJours {
    return dateFin.difference(dateDebut).inDays + 1;
  }

  String get statutDisplayName {
    switch (statut) {
      case StatutReservation.EN_ATTENTE:
        return 'En attente';
      case StatutReservation.CONFIRMEE:
        return 'Confirmée';
      case StatutReservation.EN_COURS:
        return 'En cours';
      case StatutReservation.TERMINEE:
        return 'Terminée';
      case StatutReservation.ANNULEE:
        return 'Annulée';
      case StatutReservation.REFUSEE:
        return 'Refusée';
    }
  }

  bool get isActive {
    final now = DateTime.now();
    return statut == StatutReservation.CONFIRMEE && 
           now.isAfter(dateDebut) && 
           now.isBefore(dateFin);
  }

  bool get isUpcoming {
    final now = DateTime.now();
    return statut == StatutReservation.CONFIRMEE && 
           now.isBefore(dateDebut);
  }

  bool get isCompleted {
    return statut == StatutReservation.TERMINEE;
  }

  bool get isCancelled {
    return statut == StatutReservation.ANNULEE || 
           statut == StatutReservation.REFUSEE;
  }

  @override
  String toString() {
    return 'Reservation(id: $id, voiture: ${voiture.fullName}, user: ${user.nom}, statut: $statut)';
  }

  @override
  bool operator ==(Object other) {
    if (identical(this, other)) return true;
    return other is Reservation && other.id == id;
  }

  @override
  int get hashCode => id.hashCode;
}
