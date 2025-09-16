import 'package:dio/dio.dart';

class ConnectionTest {
  static Future<bool> testBackendConnection() async {
    try {
      final dio = Dio(BaseOptions(
        connectTimeout: const Duration(seconds: 5),
        receiveTimeout: const Duration(seconds: 5),
      ));
      final response = await dio.get(
        'http://localhost:8080/api/voitures/disponibles',
        options: Options(
          headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json',
          },
        ),
      );
      
      print('Backend response status: ${response.statusCode}');
      print('Backend response data: ${response.data}');
      return response.statusCode == 200;
    } catch (e) {
      print('Backend connection error: $e');
      return false;
    }
  }
  
  static Future<bool> testAuthEndpoint() async {
    try {
      final dio = Dio(BaseOptions(
        connectTimeout: const Duration(seconds: 5),
        receiveTimeout: const Duration(seconds: 5),
      ));
      final response = await dio.post(
        'http://localhost:8080/api/auth/login',
        data: {
          'email': 'test@test.com',
          'motDePasse': 'test',
        },
        options: Options(
          headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json',
          },
        ),
      );
      
      print('Auth endpoint response status: ${response.statusCode}');
      return true; // Even if login fails, endpoint is reachable
    } catch (e) {
      print('Auth endpoint error: $e');
      return false;
    }
  }
}
