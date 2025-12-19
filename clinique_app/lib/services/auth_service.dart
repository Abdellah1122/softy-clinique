import 'package:clinique_app/core/api_client.dart';
import 'package:clinique_app/core/secure_storage.dart';
import 'package:clinique_app/models/auth_response.dart';
import 'package:clinique_app/models/profile_dto.dart';
import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

// --- Global Utility Providers (Defined here to fix import errors) ---
final secureStorageProvider = Provider((ref) => SecureStorageService());
final apiClientProvider = Provider(
  (ref) => ApiClient(ref.watch(secureStorageProvider)),
);
// --- END UTILITY PROVIDERS ---

// --- Service Provider Definition ---
final authServiceProvider = Provider<AuthService>((ref) {
  // Now it can find the providers defined just above
  return AuthService(
    ref.watch(apiClientProvider),
    ref.watch(secureStorageProvider),
  );
});

// --- Service Class (rest of the file) ---
class AuthService {
  final ApiClient _apiClient;
  final SecureStorageService _storage;

  AuthService(this._apiClient, this._storage);

  /// Attempts to log in the user with the given credentials.
  Future<AuthResponse> login(String email, String password) async {
    try {
      final response = await _apiClient.dio.post(
        '/auth/login',
        data: {'email': email, 'password': password},
      );
      final authResponse = AuthResponse.fromJson(response.data);
      await _storage.writeToken(authResponse.token);
      return authResponse;
    } on DioException catch (e) {
      print("Login failed: ${e.response?.data}");
      throw Exception('Failed to login: ${e.response?.data['message']}');
    }
  }

  /// Attempts to register a new user.
  Future<AuthResponse> register({
    required String email,
    required String password,
    required String firstName,
    required String lastName,
    required String role, // "ROLE_PATIENT" or "ROLE_THERAPIST"
  }) async {
    try {
      final response = await _apiClient.dio.post(
        '/auth/register',
        data: {
          'email': email,
          'password': password,
          'firstName': firstName,
          'lastName': lastName,
          'role': role,
        },
      );
      final authResponse = AuthResponse.fromJson(response.data);
      await _storage.writeToken(authResponse.token);
      return authResponse;
    } on DioException catch (e) {
      print("Registration failed: ${e.response?.data}");
      throw Exception('Failed to register: ${e.response?.data['message']}');
    }
  }

  /// Logs the user out by deleting their token.
  Future<void> logout() async {
    await _storage.deleteToken();
  }

  /// Fetches the current user's full profile details (/profiles/me)
  Future<ProfileDTO> fetchMyProfile() async {
    try {
      final response = await _apiClient.dio.get('/profiles/me');
      return ProfileDTO.fromJson(response.data);
    } on DioException catch (e) {
      print("Failed to fetch profile: ${e.response?.data}");
      throw Exception('Failed to load profile: ${e.message}');
    }
  }

  /// Updates the current user's profile details (PUT /profiles/me)
  Future<ProfileDTO> updateMyProfile({
    required Map<String, dynamic> data,
  }) async {
    try {
      final response = await _apiClient.dio.put(
        '/profiles/me',
        data:
            data, // Data contains the fields to update (firstName, specialty, etc.)
      );
      return ProfileDTO.fromJson(response.data);
    } on DioException catch (e) {
      print("Failed to update profile: ${e.response?.data}");
      throw Exception('Failed to update profile: ${e.message}');
    }
  }
}
