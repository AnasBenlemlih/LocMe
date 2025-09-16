import 'dart:convert';
import 'dart:io';
import 'package:dio/dio.dart';
import 'package:locme/core/storage_service.dart';

class ApiClient {
  static const String baseUrl = 'http://localhost:8080/api';
  static const bool useMockData = false; // Set to false when backend is running
  late Dio _dio;

  ApiClient() {
    _dio = Dio(BaseOptions(
      baseUrl: baseUrl,
      connectTimeout: const Duration(seconds: 10),
      receiveTimeout: const Duration(seconds: 10),
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
      },
      // Add CORS mode for web
      extra: {
        'withCredentials': false,
      },
    ));

    // Add logging interceptor for debugging
    _dio.interceptors.add(LogInterceptor(
      requestBody: true,
      responseBody: true,
      error: true,
      requestHeader: true,
      responseHeader: false,
    ));

    // Add interceptor for authentication
    _dio.interceptors.add(InterceptorsWrapper(
      onRequest: (options, handler) async {
        final token = await StorageService.getToken();
        if (token != null) {
          options.headers['Authorization'] = 'Bearer $token';
        }
        handler.next(options);
      },
      onError: (error, handler) {
        if (error.response?.statusCode == 401) {
          // Token expired or invalid, clear storage
          StorageService.clearAll();
        }
        handler.next(error);
      },
    ));
  }

  // Auth endpoints
  Future<Response> login(String email, String password) async {
    if (useMockData) {
      return _mockLogin(email, password);
    }
    
    try {
      return await _dio.post('/auth/login', data: {
        'email': email,
        'motDePasse': password,
      });
    } catch (e) {
      // If backend is not available, fallback to mock data
      print('Backend not available, falling back to mock data: $e');
      return _mockLogin(email, password);
    }
  }

  Future<Response> _mockLogin(String email, String password) async {
    // Simulate network delay
    await Future.delayed(const Duration(seconds: 1));
    
    // Mock successful login for demo accounts
    if (email == 'client@locme.com' && password == 'password') {
      return Response(
        data: {
          'token': 'mock_jwt_token_client',
          'id': 1,
          'nom': 'Client Demo',
          'email': email,
          'role': 'CLIENT',
        },
        statusCode: 200,
        requestOptions: RequestOptions(path: '/auth/login'),
      );
    } else if (email == 'societe@locme.com' && password == 'password') {
      return Response(
        data: {
          'token': 'mock_jwt_token_societe',
          'id': 2,
          'nom': 'Société Demo',
          'email': email,
          'role': 'SOCIETE',
        },
        statusCode: 200,
        requestOptions: RequestOptions(path: '/auth/login'),
      );
    } else if (email == 'admin@locme.com' && password == 'password') {
      return Response(
        data: {
          'token': 'mock_jwt_token_admin',
          'id': 3,
          'nom': 'Admin Demo',
          'email': email,
          'role': 'ADMIN',
        },
        statusCode: 200,
        requestOptions: RequestOptions(path: '/auth/login'),
      );
    } else {
      return Response(
        data: {'message': 'Invalid credentials'},
        statusCode: 401,
        requestOptions: RequestOptions(path: '/auth/login'),
      );
    }
  }

  Future<Response> register(Map<String, dynamic> userData) async {
    if (useMockData) {
      return _mockRegister(userData);
    }
    return await _dio.post('/auth/register', data: userData);
  }

  Future<Response> _mockRegister(Map<String, dynamic> userData) async {
    await Future.delayed(const Duration(seconds: 1));
    
    // Mock successful registration
    return Response(
      data: {'message': 'Registration successful'},
      statusCode: 201,
      requestOptions: RequestOptions(path: '/auth/register'),
    );
  }

  // Voiture endpoints
  Future<Response> getAvailableVoitures() async {
    if (useMockData) {
      return _mockGetAvailableVoitures();
    }
    return await _dio.get('/voitures/disponibles');
  }

  Future<Response> _mockGetAvailableVoitures() async {
    await Future.delayed(const Duration(seconds: 1));
    
    final mockVoitures = [
      {
        'id': 1,
        'marque': 'Toyota',
        'modele': 'Corolla',
        'prixParJour': 45.0,
        'disponible': true,
        'annee': 2022,
        'kilometrage': 15000,
        'carburant': 'ESSENCE',
        'transmission': 'AUTOMATIQUE',
        'nombrePlaces': 5,
        'imageUrl': null,
        'description': 'Voiture économique et fiable, parfaite pour la ville.',
        'societe': {
          'id': 1,
          'nom': 'AutoRent Paris',
          'adresse': '123 Rue de la Paix, Paris',
          'email': 'contact@autorent.fr',
          'telephone': '01 23 45 67 89',
          'user': {
            'id': 2,
            'nom': 'Société Demo',
            'email': 'societe@locme.com',
            'role': 'SOCIETE',
          }
        }
      },
      {
        'id': 2,
        'marque': 'BMW',
        'modele': 'X3',
        'prixParJour': 85.0,
        'disponible': true,
        'annee': 2023,
        'kilometrage': 8000,
        'carburant': 'DIESEL',
        'transmission': 'AUTOMATIQUE',
        'nombrePlaces': 5,
        'imageUrl': null,
        'description': 'SUV premium avec toutes les options.',
        'societe': {
          'id': 1,
          'nom': 'AutoRent Paris',
          'adresse': '123 Rue de la Paix, Paris',
          'email': 'contact@autorent.fr',
          'telephone': '01 23 45 67 89',
          'user': {
            'id': 2,
            'nom': 'Société Demo',
            'email': 'societe@locme.com',
            'role': 'SOCIETE',
          }
        }
      },
      {
        'id': 3,
        'marque': 'Renault',
        'modele': 'Clio',
        'prixParJour': 35.0,
        'disponible': true,
        'annee': 2021,
        'kilometrage': 25000,
        'carburant': 'ESSENCE',
        'transmission': 'MANUELLE',
        'nombrePlaces': 5,
        'imageUrl': null,
        'description': 'Compacte et économique, idéale pour la ville.',
        'societe': {
          'id': 2,
          'nom': 'CityCar Rental',
          'adresse': '456 Avenue des Champs, Lyon',
          'email': 'info@citycar.fr',
          'telephone': '04 78 90 12 34',
          'user': {
            'id': 4,
            'nom': 'CityCar Manager',
            'email': 'manager@citycar.fr',
            'role': 'SOCIETE',
          }
        }
      },
      {
        'id': 4,
        'marque': 'Tesla',
        'modele': 'Model 3',
        'prixParJour': 120.0,
        'disponible': true,
        'annee': 2023,
        'kilometrage': 5000,
        'carburant': 'ELECTRIQUE',
        'transmission': 'AUTOMATIQUE',
        'nombrePlaces': 5,
        'imageUrl': null,
        'description': 'Voiture électrique haut de gamme avec autopilote.',
        'societe': {
          'id': 3,
          'nom': 'EcoDrive',
          'adresse': '789 Boulevard Vert, Marseille',
          'email': 'hello@ecodrive.fr',
          'telephone': '04 91 23 45 67',
          'user': {
            'id': 5,
            'nom': 'EcoDrive Admin',
            'email': 'admin@ecodrive.fr',
            'role': 'SOCIETE',
          }
        }
      },
    ];

    return Response(
      data: mockVoitures,
      statusCode: 200,
      requestOptions: RequestOptions(path: '/voitures/disponibles'),
    );
  }

  Future<Response> getVoitureById(int id) async {
    if (useMockData) {
      return _mockGetVoitureById(id);
    }
    return await _dio.get('/voitures/$id');
  }

  Future<Response> _mockGetVoitureById(int id) async {
    await Future.delayed(const Duration(milliseconds: 500));
    
    // Get the voiture from mock data
    final mockResponse = await _mockGetAvailableVoitures();
    final voitures = mockResponse.data as List;
    
    final voiture = voitures.firstWhere(
      (v) => v['id'] == id,
      orElse: () => null,
    );
    
    if (voiture != null) {
      return Response(
        data: voiture,
        statusCode: 200,
        requestOptions: RequestOptions(path: '/voitures/$id'),
      );
    } else {
      return Response(
        data: {'message': 'Voiture not found'},
        statusCode: 404,
        requestOptions: RequestOptions(path: '/voitures/$id'),
      );
    }
  }

  Future<Response> createVoiture(Map<String, dynamic> voitureData) async {
    return await _dio.post('/voitures', data: voitureData);
  }

  Future<Response> updateVoiture(int id, Map<String, dynamic> voitureData) async {
    return await _dio.put('/voitures/$id', data: voitureData);
  }

  Future<Response> deleteVoiture(int id) async {
    return await _dio.delete('/voitures/$id');
  }

  // Reservation endpoints
  Future<Response> createReservation(Map<String, dynamic> reservationData) async {
    return await _dio.post('/reservations', data: reservationData);
  }

  Future<Response> getUserReservations(int userId) async {
    return await _dio.get('/reservations/my-reservations');
  }

  Future<Response> getReservationById(int id) async {
    return await _dio.get('/reservations/$id');
  }

  Future<Response> updateReservationStatus(int id, String status) async {
    return await _dio.put('/reservations/$id/status', data: {'statut': status});
  }

  // Societe endpoints
  Future<Response> getSocieteById(int id) async {
    return await _dio.get('/societes/$id');
  }

  Future<Response> updateSociete(int id, Map<String, dynamic> societeData) async {
    return await _dio.put('/societes/$id', data: societeData);
  }

  // Payment endpoints
  Future<Response> createPaymentIntent(Map<String, dynamic> paymentData) async {
    return await _dio.post('/payments/checkout', data: paymentData);
  }

  Future<Response> confirmPayment(String paymentIntentId) async {
    return await _dio.post('/payments/confirm', data: {
      'paymentIntentId': paymentIntentId,
    });
  }

  // Admin endpoints
  Future<Response> getAllUsers() async {
    return await _dio.get('/admin/users');
  }

  Future<Response> getAllSocietes() async {
    return await _dio.get('/admin/societes');
  }

  Future<Response> getStats() async {
    return await _dio.get('/admin/stats');
  }

  // Generic methods for custom endpoints
  Future<Response> get(String path, {Map<String, dynamic>? queryParameters}) async {
    return await _dio.get(path, queryParameters: queryParameters);
  }

  Future<Response> post(String path, {dynamic data, Map<String, dynamic>? queryParameters}) async {
    return await _dio.post(path, data: data, queryParameters: queryParameters);
  }

  Future<Response> put(String path, {dynamic data, Map<String, dynamic>? queryParameters}) async {
    return await _dio.put(path, data: data, queryParameters: queryParameters);
  }

  Future<Response> delete(String path, {Map<String, dynamic>? queryParameters}) async {
    return await _dio.delete(path, queryParameters: queryParameters);
  }
}
