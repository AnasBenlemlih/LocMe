import 'package:locme/core/api_client.dart';
import 'package:locme/core/storage_service.dart';
import 'package:locme/models/user.dart';

class AuthService {
  final ApiClient _apiClient = ApiClient();

  // Login user
  Future<Map<String, dynamic>> login(String email, String password) async {
    try {
      final response = await _apiClient.login(email, password);
      
      if (response.statusCode == 200) {
        final responseData = response.data;
        
        // Check if the response has the expected structure
        if (responseData['success'] == true && responseData['data'] != null) {
          final data = responseData['data'];
          
          // Save token and user data
          await StorageService.saveToken(data['token']);
          await StorageService.saveUser({
            'id': data['id'],
            'nom': data['nom'],
            'email': data['email'],
            'role': data['role'],
          });

          return {
            'success': true,
            'user': User.fromJson({
              'id': data['id'],
              'nom': data['nom'],
              'email': data['email'],
              'role': data['role'],
            }),
          };
        } else {
          return {
            'success': false,
            'message': responseData['message'] ?? 'Login failed',
          };
        }
      } else {
        return {
          'success': false,
          'message': 'Login failed',
        };
      }
    } catch (e) {
      String errorMessage = 'Erreur de connexion';
      
      if (e.toString().contains('SocketException') || e.toString().contains('Connection refused')) {
        errorMessage = 'Impossible de se connecter au serveur. Vérifiez que le backend est démarré sur localhost:8080';
      } else if (e.toString().contains('CORS')) {
        errorMessage = 'Erreur CORS. Vérifiez la configuration du backend pour autoriser les requêtes depuis le navigateur';
      } else if (e.toString().contains('timeout')) {
        errorMessage = 'Timeout de connexion. Le serveur met trop de temps à répondre';
      } else {
        errorMessage = 'Erreur réseau: ${e.toString()}';
      }
      
      return {
        'success': false,
        'message': errorMessage,
      };
    }
  }

  // Register user
  Future<Map<String, dynamic>> register({
    required String nom,
    required String email,
    required String password,
    String? telephone,
    String? adresse,
    String role = 'CLIENT',
  }) async {
    try {
      final response = await _apiClient.register({
        'nom': nom,
        'email': email,
        'motDePasse': password,
        'telephone': telephone,
        'adresse': adresse,
        'role': role,
      });

      if (response.statusCode == 201 || response.statusCode == 200) {
        final responseData = response.data;
        return {
          'success': true,
          'message': responseData['message'] ?? 'Registration successful',
        };
      } else {
        final responseData = response.data;
        return {
          'success': false,
          'message': responseData['message'] ?? 'Registration failed',
        };
      }
    } catch (e) {
      return {
        'success': false,
        'message': 'Network error: ${e.toString()}',
      };
    }
  }

  // Get current user
  Future<User?> getCurrentUser() async {
    try {
      final userData = await StorageService.getUser();
      if (userData != null) {
        return User.fromJson(userData);
      }
      return null;
    } catch (e) {
      return null;
    }
  }

  // Check if user is logged in
  Future<bool> isLoggedIn() async {
    return await StorageService.isLoggedIn();
  }

  // Logout user
  Future<void> logout() async {
    await StorageService.clearAll();
  }

  // Get stored token
  Future<String?> getToken() async {
    return await StorageService.getToken();
  }
}
