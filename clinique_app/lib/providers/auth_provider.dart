import 'dart:async';
import 'package:clinique_app/models/user_dto.dart';
import 'package:clinique_app/services/auth_service.dart';
import 'package:equatable/equatable.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

// -------------------------------------------------
// 1. Define the states
// -------------------------------------------------
enum AuthStatus { unknown, authenticated, unauthenticated, loading }

class AuthState extends Equatable {
  final AuthStatus status;
  final UserDTO? user;
  final String? error;

  const AuthState({this.status = AuthStatus.unknown, this.user, this.error});

  AuthState copyWith({AuthStatus? status, UserDTO? user, String? error}) {
    return AuthState(
      status: status ?? this.status,
      user: user ?? this.user,
      error: error ?? this.error,
    );
  }

  @override
  List<Object?> get props => [status, user, error];
}

// -------------------------------------------------
// 2. Define the Notifier (The "Controller")
// -------------------------------------------------
class AuthNotifier extends StateNotifier<AuthState> {
  final AuthService _authService;

  AuthNotifier(this._authService) : super(const AuthState()) {
    _checkToken();
  }

  Future<void> _checkToken() async {
    // We start as unauthenticated
    state = state.copyWith(status: AuthStatus.unauthenticated);
  }

  Future<void> login(String email, String password) async {
    state = state.copyWith(status: AuthStatus.loading, error: null);
    try {
      final authResponse = await _authService.login(email, password);
      state = state.copyWith(
        status: AuthStatus.authenticated,
        user: authResponse.user,
      );
    } catch (e) {
      state = state.copyWith(
        status: AuthStatus.unauthenticated,
        error: e.toString(),
      );
    }
  }

  Future<void> register({
    required String email,
    required String password,
    required String firstName,
    required String lastName,
    required String role,
  }) async {
    state = state.copyWith(status: AuthStatus.loading, error: null);
    try {
      final authResponse = await _authService.register(
        email: email,
        password: password,
        firstName: firstName,
        lastName: lastName,
        role: role,
      );
      state = state.copyWith(
        status: AuthStatus.authenticated,
        user: authResponse.user,
      );
    } catch (e) {
      state = state.copyWith(
        status: AuthStatus.unauthenticated,
        error: e.toString(),
      );
    }
  }

  Future<void> logout() async {
    await _authService.logout();
    state = state.copyWith(status: AuthStatus.unauthenticated, user: null);
  }

  // Helper getter
  bool get isAuthenticated => state.status == AuthStatus.authenticated;
}

// -------------------------------------------------
// 3. Define the Global Provider
// -------------------------------------------------
final authProvider = StateNotifierProvider<AuthNotifier, AuthState>((ref) {
  final authService = ref.watch(authServiceProvider);
  return AuthNotifier(authService);
});
