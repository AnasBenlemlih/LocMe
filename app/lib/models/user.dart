enum Role {
  CLIENT,
  SOCIETE,
  ADMIN,
}

class User {
  final int id;
  final String nom;
  final String email;
  final Role role;
  final String? telephone;
  final String? adresse;
  final DateTime? createdAt;
  final DateTime? updatedAt;

  User({
    required this.id,
    required this.nom,
    required this.email,
    required this.role,
    this.telephone,
    this.adresse,
    this.createdAt,
    this.updatedAt,
  });

  factory User.fromJson(Map<String, dynamic> json) {
    return User(
      id: json['id'],
      nom: json['nom'],
      email: json['email'],
      role: Role.values.firstWhere(
        (e) => e.name == json['role'],
        orElse: () => Role.CLIENT,
      ),
      telephone: json['telephone'],
      adresse: json['adresse'],
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
      'email': email,
      'role': role.name,
      'telephone': telephone,
      'adresse': adresse,
      'createdAt': createdAt?.toIso8601String(),
      'updatedAt': updatedAt?.toIso8601String(),
    };
  }

  User copyWith({
    int? id,
    String? nom,
    String? email,
    Role? role,
    String? telephone,
    String? adresse,
    DateTime? createdAt,
    DateTime? updatedAt,
  }) {
    return User(
      id: id ?? this.id,
      nom: nom ?? this.nom,
      email: email ?? this.email,
      role: role ?? this.role,
      telephone: telephone ?? this.telephone,
      adresse: adresse ?? this.adresse,
      createdAt: createdAt ?? this.createdAt,
      updatedAt: updatedAt ?? this.updatedAt,
    );
  }

  bool get isClient => role == Role.CLIENT;
  bool get isSociete => role == Role.SOCIETE;
  bool get isAdmin => role == Role.ADMIN;

  @override
  String toString() {
    return 'User(id: $id, nom: $nom, email: $email, role: $role)';
  }

  @override
  bool operator ==(Object other) {
    if (identical(this, other)) return true;
    return other is User && other.id == id;
  }

  @override
  int get hashCode => id.hashCode;
}
