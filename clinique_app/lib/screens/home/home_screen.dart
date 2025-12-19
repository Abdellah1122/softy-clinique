import 'package:clinique_app/core/theme/app_theme.dart';
import 'package:clinique_app/providers/auth_provider.dart';
import 'package:clinique_app/screens/dashboard/appointments_view.dart';
import 'package:clinique_app/screens/dashboard/create_view.dart';
import 'package:clinique_app/screens/dashboard/settings_view.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

class HomeScreen extends ConsumerStatefulWidget {
  const HomeScreen({super.key});

  @override
  ConsumerState<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends ConsumerState<HomeScreen> {
  int _selectedIndex = 0;

  void _onItemTapped(int index) {
    setState(() {
      _selectedIndex = index;
    });
  }

  static const List<Widget> _therapistScreens = <Widget>[
    AppointmentsView(isPatientView: false),
    CreateView(),
    SettingsView(),
  ];

  static const List<String> _therapistTitles = <String>[
    'Dashboard',
    'New Appointment',
    'Settings',
  ];

  @override
  Widget build(BuildContext context) {
    final role = ref.watch(authProvider.select((state) => state.user?.role));

    if (role == 'ROLE_THERAPIST') {
      return Scaffold(
        appBar: AppBar(
          title: Text(
            _therapistTitles[_selectedIndex],
            style: AppTextStyles.headlineMedium.copyWith(color: Colors.white),
          ),
          centerTitle: true,
        ),
        body: IndexedStack(index: _selectedIndex, children: _therapistScreens),
        bottomNavigationBar: Container(
          decoration: BoxDecoration(
            boxShadow: [
              BoxShadow(
                color: Colors.black.withOpacity(0.05),
                blurRadius: 10,
                offset: const Offset(0, -5),
              ),
            ],
          ),
          child: NavigationBar(
            selectedIndex: _selectedIndex,
            onDestinationSelected: _onItemTapped,
            backgroundColor: Colors.white,
            indicatorColor: AppColors.primaryLight,
            destinations: const [
              NavigationDestination(
                icon: Icon(Icons.dashboard_outlined),
                selectedIcon: Icon(Icons.dashboard, color: AppColors.primary),
                label: 'Dashboard',
              ),
              NavigationDestination(
                icon: Icon(Icons.add_circle_outline),
                selectedIcon: Icon(Icons.add_circle, color: AppColors.primary),
                label: 'Create',
              ),
              NavigationDestination(
                icon: Icon(Icons.settings_outlined),
                selectedIcon: Icon(Icons.settings, color: AppColors.primary),
                label: 'Settings',
              ),
            ],
          ),
        ),
      );
    } else if (role == 'ROLE_PATIENT') {
      return Scaffold(
        appBar: AppBar(
          title: Text(
            'My Appointments',
            style: AppTextStyles.headlineMedium.copyWith(color: Colors.white),
          ),
          centerTitle: true,
          actions: [
            IconButton(
              icon: const Icon(Icons.logout),
              tooltip: 'Logout',
              onPressed: () {
                ref.read(authProvider.notifier).logout();
              },
            ),
          ],
        ),
        body: const AppointmentsView(isPatientView: true),
      );
    } else {
      return const Scaffold(
        body: Center(
          child: CircularProgressIndicator(),
        ),
      );
    }
  }
}
