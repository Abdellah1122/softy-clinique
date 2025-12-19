import 'package:clinique_app/models/appointment_dto.dart';
import 'package:clinique_app/core/theme/app_theme.dart';
import 'package:clinique_app/providers/auth_provider.dart';
import 'package:clinique_app/screens/auth/login_screen.dart';
import 'package:clinique_app/screens/auth/register_screen.dart';
import 'package:clinique_app/screens/dashboard/appointment_detail_screen.dart';
import 'package:clinique_app/screens/home/home_screen.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

// -------------------------------------------------------------------
// STEP 1: DEFINE PROVIDER FOR GoRouter
// -------------------------------------------------------------------
final goRouterProvider = Provider<GoRouter>((ref) {
  // This listens for auth changes to trigger a re-route
  final authState = ValueNotifier<AuthState>(ref.watch(authProvider));
  ref.listen(authProvider, (_, next) {
    authState.value = next;
  });

  return GoRouter(
    refreshListenable: authState,
    routes: [
      GoRoute(path: '/', builder: (context, state) => const HomeScreen()),
      GoRoute(path: '/login', builder: (context, state) => const LoginScreen()),
      GoRoute(
        path: '/register',
        builder: (context, state) => const RegisterScreen(),
      ),
      // --- THE NEW DETAIL ROUTE ---
      GoRoute(
        path: '/appointment-detail',
        builder: (context, state) {
          // Get the appointment object passed as an "extra" parameter
          final appointment = state.extra as AppointmentDTO;
          return AppointmentDetailScreen(appointment: appointment);
        },
      ),
    ],
    redirect: (context, state) {
      // Read the AuthState object
      final auth = ref.read(authProvider);
      final location = state.matchedLocation;
      final isAuthPage = location == '/login' || location == '/register';

      final isAuthenticated = auth.status == AuthStatus.authenticated;

      // If user is not logged in, force them to the login page
      if (!isAuthenticated) {
        return isAuthPage ? null : '/login';
      }

      // If user is logged in and tries to go to login/register,
      // send them to the home page
      if (isAuthenticated && isAuthPage) {
        return '/';
      }

      // No redirect needed
      return null;
    },
  );
});

// -------------------------------------------------------------------
// STEP 2: CREATE THE APP WIDGET
// -------------------------------------------------------------------
void main() {
  runApp(const ProviderScope(child: CliniqueApp()));
}

// We change this to a ConsumerWidget to use Riverpod
class CliniqueApp extends ConsumerWidget {
  const CliniqueApp({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    // We watch the router provider
    final router = ref.watch(goRouterProvider);

    return MaterialApp.router(
      routerConfig: router, // Use the router from the provider
      title: 'Clinique Assist',
      debugShowCheckedModeBanner: false,
      theme: AppTheme.lightTheme,
    );
  }
}
