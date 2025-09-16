import 'package:flutter/foundation.dart';
import 'package:locme/core/api_client.dart';
import 'package:locme/models/reservation.dart';
import 'package:locme/models/voiture.dart';
import 'package:locme/models/user.dart';

class ReservationProvider with ChangeNotifier {
  final ApiClient _apiClient = ApiClient();
  
  List<Reservation> _reservations = [];
  bool _isLoading = false;
  String? _error;

  List<Reservation> get reservations => _reservations;
  bool get isLoading => _isLoading;
  String? get error => _error;

  // Load user reservations
  Future<void> loadUserReservations(int userId) async {
    _setLoading(true);
    _clearError();
    
    try {
      final response = await _apiClient.getUserReservations(userId);
      if (response.statusCode == 200) {
        final responseData = response.data;
        
        // Check if the response has the expected structure
        if (responseData['success'] == true && responseData['data'] != null) {
          final List<dynamic> data = responseData['data'];
          _reservations = data.map((json) => Reservation.fromJson(json)).toList();
        } else {
          _setError(responseData['message'] ?? 'Failed to load reservations');
        }
      } else {
        _setError('Failed to load reservations');
      }
    } catch (e) {
      _setError('Failed to load reservations: ${e.toString()}');
    } finally {
      _setLoading(false);
    }
  }

  // Create reservation
  Future<bool> createReservation({
    required Voiture voiture,
    required User user,
    required DateTime dateDebut,
    required DateTime dateFin,
    String? commentaires,
    String? lieuPrise,
    String? lieuRetour,
  }) async {
    _setLoading(true);
    _clearError();
    
    try {
      // Calculate total amount
      final dureeEnJours = dateFin.difference(dateDebut).inDays + 1;
      final montant = voiture.prixParJour * dureeEnJours;

      final reservationData = {
        'voitureId': voiture.id,
        'userId': user.id,
        'dateDebut': dateDebut.toIso8601String().split('T')[0],
        'dateFin': dateFin.toIso8601String().split('T')[0],
        'montant': montant,
        'commentaires': commentaires,
        'lieuPrise': lieuPrise,
        'lieuRetour': lieuRetour,
      };

      final response = await _apiClient.createReservation(reservationData);
      if (response.statusCode == 201 || response.statusCode == 200) {
        await loadUserReservations(user.id); // Refresh list
        return true;
      } else {
        _setError('Failed to create reservation');
        return false;
      }
    } catch (e) {
      _setError('Failed to create reservation: ${e.toString()}');
      return false;
    } finally {
      _setLoading(false);
    }
  }

  // Update reservation status
  Future<bool> updateReservationStatus(int reservationId, StatutReservation status) async {
    _setLoading(true);
    _clearError();
    
    try {
      final response = await _apiClient.updateReservationStatus(reservationId, status.name);
      if (response.statusCode == 200) {
        // Update local reservation
        final index = _reservations.indexWhere((r) => r.id == reservationId);
        if (index != -1) {
          _reservations[index] = _reservations[index].copyWith(statut: status);
          notifyListeners();
        }
        return true;
      } else {
        _setError('Failed to update reservation status');
        return false;
      }
    } catch (e) {
      _setError('Failed to update reservation status: ${e.toString()}');
      return false;
    } finally {
      _setLoading(false);
    }
  }

  // Get reservation by ID
  Future<Reservation?> getReservationById(int id) async {
    try {
      final response = await _apiClient.getReservationById(id);
      if (response.statusCode == 200) {
        return Reservation.fromJson(response.data);
      }
    } catch (e) {
      _setError('Failed to load reservation: ${e.toString()}');
    }
    return null;
  }

  // Get active reservations
  List<Reservation> get activeReservations {
    return _reservations.where((r) => r.isActive).toList();
  }

  // Get upcoming reservations
  List<Reservation> get upcomingReservations {
    return _reservations.where((r) => r.isUpcoming).toList();
  }

  // Get completed reservations
  List<Reservation> get completedReservations {
    return _reservations.where((r) => r.isCompleted).toList();
  }

  // Get cancelled reservations
  List<Reservation> get cancelledReservations {
    return _reservations.where((r) => r.isCancelled).toList();
  }

  // Get reservations by status
  List<Reservation> getReservationsByStatus(StatutReservation status) {
    return _reservations.where((r) => r.statut == status).toList();
  }

  // Calculate total revenue (for société)
  double get totalRevenue {
    return _reservations
        .where((r) => r.statut == StatutReservation.TERMINEE)
        .fold(0.0, (sum, r) => sum + r.montant);
  }

  // Get monthly revenue
  double getMonthlyRevenue(int year, int month) {
    return _reservations
        .where((r) => 
            r.statut == StatutReservation.TERMINEE &&
            r.dateFin.year == year &&
            r.dateFin.month == month)
        .fold(0.0, (sum, r) => sum + r.montant);
  }

  // Clear error
  void clearError() {
    _clearError();
  }

  // Private methods
  void _setLoading(bool loading) {
    _isLoading = loading;
    notifyListeners();
  }

  void _setError(String error) {
    _error = error;
    notifyListeners();
  }

  void _clearError() {
    _error = null;
    notifyListeners();
  }
}
