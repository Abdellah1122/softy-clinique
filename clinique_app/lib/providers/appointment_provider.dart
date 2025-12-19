import 'package:clinique_app/models/appointment_dto.dart';
import 'package:clinique_app/providers/auth_provider.dart';
import 'package:clinique_app/services/appointment_service.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

/// Provider pour la liste de RDV du THÉRAPEUTE
final therapistAppointmentsProvider = FutureProvider<List<AppointmentDTO>>((
  ref,
) async {
  final authState = ref.watch(authProvider);
  final appointmentService = ref.watch(appointmentServiceProvider);
  final therapistId = authState.user?.profileId;

  if (therapistId != null) {
    return appointmentService.getAppointmentsForTherapist(therapistId);
  } else {
    return [];
  }
});

/// --- NOUVEAU PROVIDER POUR LE PATIENT ---
final patientAppointmentsProvider = FutureProvider<List<AppointmentDTO>>((
  ref,
) async {
  final authState = ref.watch(authProvider);
  final appointmentService = ref.watch(appointmentServiceProvider);
  final patientId = authState.user?.profileId;

  if (patientId != null) {
    // Appelle la nouvelle méthode de service
    return appointmentService.getAppointmentsForPatient(patientId);
  } else {
    return [];
  }
});
