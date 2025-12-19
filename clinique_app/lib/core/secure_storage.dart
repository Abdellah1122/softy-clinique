import 'package:flutter_secure_storage/flutter_secure_storage.dart';

/// A service class for handling secure storage.
/// This is a "singleton" meaning we will only ever have one instance of it.
class SecureStorageService {
  // Define a private key name for our token
  static const _tokenKey = 'auth_token';

  // Create the storage instance
  final _storage = const FlutterSecureStorage();

  /// Deletes the JWT token from secure storage.
  /// Used for logout.
  Future<void> deleteToken() async {
    await _storage.delete(key: _tokenKey);
  }

  /// Reads the JWT token from secure storage.
  /// Returns null if no token is found.
  Future<String?> readToken() async {
    return await _storage.read(key: _tokenKey);
  }

  /// Writes the JWT token to secure storage.
  /// Used after login or register.
  Future<void> writeToken(String token) async {
    await _storage.write(key: _tokenKey, value: token);
  }
}
