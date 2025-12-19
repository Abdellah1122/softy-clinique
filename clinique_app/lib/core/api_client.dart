import 'package:dio/dio.dart'; // <-- FIXED THE IMPORT
import 'package:clinique_app/core/secure_storage.dart';

/// Creates a customized Dio client for our API
class ApiClient {
  final Dio dio;
  final SecureStorageService _storage;

  // Your Spring Boot API's base URL
  // Using localhost for the iOS Simulator
  static const String _baseUrl = 'http://localhost:8080/api/v1';

  ApiClient(this._storage) : dio = Dio(BaseOptions(baseUrl: _baseUrl)) {
    // This is the interceptor, it's like a "filter"
    dio.interceptors.add(
      InterceptorsWrapper(
        // This function runs BEFORE every request
        onRequest: (options, handler) async {
          // Define public paths that DON'T need a token
          final publicPaths = ['/auth/login', '/auth/register'];

          // If the request path is public, just continue
          if (publicPaths.contains(options.path)) {
            return handler.next(options);
          }

          // Otherwise, get the token from storage
          final token = await _storage.readToken();

          if (token != null) {
            // Add the "Authorization: Bearer <token>" header
            options.headers['Authorization'] = 'Bearer $token';
          }

          // Continue with the (now modified) request
          return handler.next(options);
        },

        // You can also handle errors here, e.g., if token expires
        onError: (DioException e, handler) async {
          if (e.response?.statusCode == 401) {
            // Token is invalid or expired
            // We could try to refresh it or just force a logout
            print("Token expired or invalid, logging out.");
            await _storage.deleteToken();
            // You would redirect to login screen here
          }
          return handler.next(e);
        },
      ),
    );
  }
}
