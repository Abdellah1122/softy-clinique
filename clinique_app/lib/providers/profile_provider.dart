import 'package:clinique_app/models/profile_dto.dart';
import 'package:clinique_app/providers/auth_provider.dart';
import 'package:clinique_app/services/auth_service.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

// --- Global Profile Provider (Fetches data once) ---
final myProfileProvider = FutureProvider.autoDispose<ProfileDTO>((ref) async {
  // Check if the user is authenticated by watching the status
  final isAuthenticated = ref.watch(
    authProvider.select((state) => state.status == AuthStatus.authenticated),
  ); // <-- FIXED GETTER LOGIC

  if (!isAuthenticated) {
    // Return a dummy error or null if not logged in (GoRouter handles the redirect anyway)
    return Future.error('User not authenticated');
  }

  final authService = ref.watch(authServiceProvider);
  return authService.fetchMyProfile();
});

// --- StateNotifier for Update Actions (Manages save state) ---
final profileUpdateProvider =
    StateNotifierProvider.autoDispose<
      ProfileUpdateNotifier,
      AsyncValue<ProfileDTO>
    >((ref) {
      final initialData = ref.watch(myProfileProvider);
      return ProfileUpdateNotifier(ref, initialData);
    });

class ProfileUpdateNotifier extends StateNotifier<AsyncValue<ProfileDTO>> {
  final Ref _ref;
  final AuthService _authService;

  ProfileUpdateNotifier(this._ref, AsyncValue<ProfileDTO> initialProfile)
    : _authService = _ref.watch(authServiceProvider),
      super(initialProfile);

  /// Handles the profile update request
  Future<void> updateProfile({required Map<String, dynamic> data}) async {
    // Set state to loading while keeping the previous data visible
    state = AsyncValue.loading();

    try {
      final updatedProfile = await _authService.updateMyProfile(data: data);

      // Update successful: set the new profile data
      state = AsyncValue.data(updatedProfile);

      // CRUCIAL: Invalidate the original fetching provider so it refetches the new data
      _ref.invalidate(myProfileProvider);
    } catch (e, stack) {
      // Update failed: retain the old data but show the error
      state = AsyncValue.error(e, stack);
    }
  }
}
