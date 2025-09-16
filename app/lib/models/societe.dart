import 'user.dart';

class Societe {
  final int id;
  final String nom;
  final String adresse;
  final String email;
  final String? telephone;
  final String? description;
  final String? logoUrl;
  final User user;
  final DateTime? createdAt;
  final DateTime? updatedAt;

  Societe({
    required this.id,
    required this.nom,
    required this.adresse,
    required this.email,
    this.telephone,
    this.description,
    this.logoUrl,
    required this.user,
    this.createdAt,
    this.updatedAt,
  });

  factory Societe.fromJson(Map<String, dynamic> json) {
    return Societe(
      id: json['id'],
      nom: json['nom'],
      adresse: json['adresse'],
      email: json['email'],
      telephone: json['telephone'],
      description: json['description'],
      logoUrl: json['logoUrl'],
      user: User.fromJson(json['user']),
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
      'nom': nom,
      'adresse': adresse,
      'email': email,
      'telephone': telephone,
      'description': description,
      'logoUrl': logoUrl,
      'user': user.toJson(),
      'createdAt': createdAt?.toIso8601String(),
      'updatedAt': updatedAt?.toIso8601String(),
    };
  }

  Societe copyWith({
    int? id,
    String? nom,
    String? adresse,
    String? email,
    String? telephone,
    String? description,
    String? logoUrl,
    User? user,
    DateTime? createdAt,
    DateTime? updatedAt,
  }) {
    return Societe(
      id: id ?? this.id,
      nom: nom ?? this.nom,
      adresse: adresse ?? this.adresse,
      email: email ?? this.email,
      telephone: telephone ?? this.telephone,
      description: description ?? this.description,
      logoUrl: logoUrl ?? this.logoUrl,
      user: user ?? this.user,
      createdAt: createdAt ?? this.createdAt,
      updatedAt: updatedAt ?? this.updatedAt,
    );
  }

  @override
  String toString() {
    return 'Societe(id: $id, nom: $nom, email: $email)';
  }

  @override
  bool operator ==(Object other) {
    if (identical(this, other)) return true;
    return other is Societe && other.id == id;
  }

  @override
  int get hashCode => id.hashCode;
}

