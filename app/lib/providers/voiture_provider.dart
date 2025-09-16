import 'package:flutter/foundation.dart';
import 'package:locme/core/api_client.dart';
import 'package:locme/models/voiture.dart';

class VoitureProvider with ChangeNotifier {
  final ApiClient _apiClient = ApiClient();
  
  List<Voiture> _voitures = [];
  List<Voiture> _filteredVoitures = [];
  bool _isLoading = false;
  String? _error;
  String _searchQuery = '';
  TypeCarburant? _selectedCarburant;
  TypeTransmission? _selectedTransmission;
  double? _maxPrice;
  int? _minPlaces;

  List<Voiture> get voitures => _filteredVoitures;
  bool get isLoading => _isLoading;
  String? get error => _error;
  String get searchQuery => _searchQuery;
  TypeCarburant? get selectedCarburant => _selectedCarburant;
  TypeTransmission? get selectedTransmission => _selectedTransmission;
  double? get maxPrice => _maxPrice;
  int? get minPlaces => _minPlaces;

  // Load available voitures
  Future<void> loadAvailableVoitures() async {
    _setLoading(true);
    _clearError();
    
    try {
      final response = await _apiClient.getAvailableVoitures();
      if (response.statusCode == 200) {
        final responseData = response.data;
        
        // Check if the response has the expected structure
        if (responseData['success'] == true && responseData['data'] != null) {
          final List<dynamic> data = responseData['data'];
          _voitures = data.map((json) => Voiture.fromJson(json)).toList();
          _applyFilters();
        } else {
          _setError(responseData['message'] ?? 'Failed to load voitures');
        }
      } else {
        _setError('Failed to load voitures');
      }
    } catch (e) {
      _setError('Failed to load voitures: ${e.toString()}');
    } finally {
      _setLoading(false);
    }
  }

  // Get voiture by ID
  Future<Voiture?> getVoitureById(int id) async {
    try {
      final response = await _apiClient.getVoitureById(id);
      if (response.statusCode == 200) {
        final responseData = response.data;
        
        // Check if the response has the expected structure
        if (responseData['success'] == true && responseData['data'] != null) {
          return Voiture.fromJson(responseData['data']);
        } else {
          _setError(responseData['message'] ?? 'Failed to load voiture');
        }
      }
    } catch (e) {
      _setError('Failed to load voiture: ${e.toString()}');
    }
    return null;
  }

  // Create voiture (for société)
  Future<bool> createVoiture(Map<String, dynamic> voitureData) async {
    _setLoading(true);
    _clearError();
    
    try {
      final response = await _apiClient.createVoiture(voitureData);
      if (response.statusCode == 201 || response.statusCode == 200) {
        await loadAvailableVoitures(); // Refresh list
        return true;
      } else {
        _setError('Failed to create voiture');
        return false;
      }
    } catch (e) {
      _setError('Failed to create voiture: ${e.toString()}');
      return false;
    } finally {
      _setLoading(false);
    }
  }

  // Update voiture
  Future<bool> updateVoiture(int id, Map<String, dynamic> voitureData) async {
    _setLoading(true);
    _clearError();
    
    try {
      final response = await _apiClient.updateVoiture(id, voitureData);
      if (response.statusCode == 200) {
        await loadAvailableVoitures(); // Refresh list
        return true;
      } else {
        _setError('Failed to update voiture');
        return false;
      }
    } catch (e) {
      _setError('Failed to update voiture: ${e.toString()}');
      return false;
    } finally {
      _setLoading(false);
    }
  }

  // Delete voiture
  Future<bool> deleteVoiture(int id) async {
    _setLoading(true);
    _clearError();
    
    try {
      final response = await _apiClient.deleteVoiture(id);
      if (response.statusCode == 200 || response.statusCode == 204) {
        await loadAvailableVoitures(); // Refresh list
        return true;
      } else {
        _setError('Failed to delete voiture');
        return false;
      }
    } catch (e) {
      _setError('Failed to delete voiture: ${e.toString()}');
      return false;
    } finally {
      _setLoading(false);
    }
  }

  // Search voitures
  void searchVoitures(String query) {
    _searchQuery = query;
    _applyFilters();
  }

  // Filter by carburant
  void filterByCarburant(TypeCarburant? carburant) {
    _selectedCarburant = carburant;
    _applyFilters();
  }

  // Filter by transmission
  void filterByTransmission(TypeTransmission? transmission) {
    _selectedTransmission = transmission;
    _applyFilters();
  }

  // Filter by max price
  void filterByMaxPrice(double? maxPrice) {
    _maxPrice = maxPrice;
    _applyFilters();
  }

  // Filter by min places
  void filterByMinPlaces(int? minPlaces) {
    _minPlaces = minPlaces;
    _applyFilters();
  }

  // Clear all filters
  void clearFilters() {
    _searchQuery = '';
    _selectedCarburant = null;
    _selectedTransmission = null;
    _maxPrice = null;
    _minPlaces = null;
    _applyFilters();
  }

  // Apply filters
  void _applyFilters() {
    _filteredVoitures = _voitures.where((voiture) {
      // Search query filter
      if (_searchQuery.isNotEmpty) {
        final query = _searchQuery.toLowerCase();
        if (!voiture.marque.toLowerCase().contains(query) &&
            !voiture.modele.toLowerCase().contains(query)) {
          return false;
        }
      }

      // Carburant filter
      if (_selectedCarburant != null && voiture.carburant != _selectedCarburant) {
        return false;
      }

      // Transmission filter
      if (_selectedTransmission != null && voiture.transmission != _selectedTransmission) {
        return false;
      }

      // Price filter
      if (_maxPrice != null && voiture.prixParJour > _maxPrice!) {
        return false;
      }

      // Places filter
      if (_minPlaces != null && (voiture.nombrePlaces ?? 0) < _minPlaces!) {
        return false;
      }

      return true;
    }).toList();
    
    notifyListeners();
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
