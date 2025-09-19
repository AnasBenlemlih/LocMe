import 'reservation.dart';

enum StatutPaiement {
  EN_ATTENTE,
  PAYE,
  ECHEC,
  REMBOURSE,
  PARTIELLEMENT_REMBOURSE,
}

enum MethodePaiement {
  CARTE_CREDIT,
  CARTE_DEBIT,
  VIREMENT,
  ESPECES,
  CHEQUE,
}

class Paiement {
  final int id;
  final Reservation reservation;
  final double montant;
  final StatutPaiement statut;
  final MethodePaiement? methodePaiement;
  final String? stripePaymentIntentId;
  final String? transactionId;
  final DateTime? datePaiement;
  final DateTime? createdAt;
  final DateTime? updatedAt;

  Paiement({
    required this.id,
    required this.reservation,
    required this.montant,
    required this.statut,
    this.methodePaiement,
    this.stripePaymentIntentId,
    this.transactionId,
    this.datePaiement,
    this.createdAt,
    this.updatedAt,
  });

  factory Paiement.fromJson(Map<String, dynamic> json) {
    return Paiement(
      id: json['id'],
      reservation: Reservation.fromJson(json['reservation']),
      montant: (json['montant'] as num).toDouble(),
      statut: StatutPaiement.values.firstWhere(
        (e) => e.name == json['statut'],
        orElse: () => StatutPaiement.EN_ATTENTE,
      ),
      methodePaiement: json['methodePaiement'] != null 
          ? MethodePaiement.values.firstWhere(
              (e) => e.name == json['methodePaiement'],
              orElse: () => MethodePaiement.CARTE_CREDIT,
            )
          : null,
      stripePaymentIntentId: json['stripePaymentIntentId'],
      transactionId: json['transactionId'],
      datePaiement: json['datePaiement'] != null 
          ? DateTime.parse(json['datePaiement']) 
          : null,
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
      'reservation': reservation.toJson(),
      'montant': montant,
      'statut': statut.name,
      'methodePaiement': methodePaiement?.name,
      'stripePaymentIntentId': stripePaymentIntentId,
      'transactionId': transactionId,
      'datePaiement': datePaiement?.toIso8601String(),
      'createdAt': createdAt?.toIso8601String(),
      'updatedAt': updatedAt?.toIso8601String(),
    };
  }

  Paiement copyWith({
    int? id,
    Reservation? reservation,
    double? montant,
    StatutPaiement? statut,
    MethodePaiement? methodePaiement,
    String? stripePaymentIntentId,
    String? transactionId,
    DateTime? datePaiement,
    DateTime? createdAt,
    DateTime? updatedAt,
  }) {
    return Paiement(
      id: id ?? this.id,
      reservation: reservation ?? this.reservation,
      montant: montant ?? this.montant,
      statut: statut ?? this.statut,
      methodePaiement: methodePaiement ?? this.methodePaiement,
      stripePaymentIntentId: stripePaymentIntentId ?? this.stripePaymentIntentId,
      transactionId: transactionId ?? this.transactionId,
      datePaiement: datePaiement ?? this.datePaiement,
      createdAt: createdAt ?? this.createdAt,
      updatedAt: updatedAt ?? this.updatedAt,
    );
  }

  String get statutDisplayName {
    switch (statut) {
      case StatutPaiement.EN_ATTENTE:
        return 'En attente';
      case StatutPaiement.PAYE:
        return 'Payé';
      case StatutPaiement.ECHEC:
        return 'Échec';
      case StatutPaiement.REMBOURSE:
        return 'Remboursé';
      case StatutPaiement.PARTIELLEMENT_REMBOURSE:
        return 'Partiellement remboursé';
    }
  }

  String get methodePaiementDisplayName {
    switch (methodePaiement) {
      case MethodePaiement.CARTE_CREDIT:
        return 'Carte de crédit';
      case MethodePaiement.CARTE_DEBIT:
        return 'Carte de débit';
      case MethodePaiement.VIREMENT:
        return 'Virement';
      case MethodePaiement.ESPECES:
        return 'Espèces';
      case MethodePaiement.CHEQUE:
        return 'Chèque';
      default:
        return 'Non spécifié';
    }
  }

  bool get isPaid => statut == StatutPaiement.PAYE;
  bool get isPending => statut == StatutPaiement.EN_ATTENTE;
  bool get isFailed => statut == StatutPaiement.ECHEC;
  bool get isRefunded => statut == StatutPaiement.REMBOURSE || 
                        statut == StatutPaiement.PARTIELLEMENT_REMBOURSE;

  @override
  String toString() {
    return 'Paiement(id: $id, montant: $montant, statut: $statut)';
  }

  @override
  bool operator ==(Object other) {
    if (identical(this, other)) return true;
    return other is Paiement && other.id == id;
  }

  @override
  int get hashCode => id.hashCode;
}

