import 'package:clinique_app/models/appointment_dto.dart';
import 'package:clinique_app/services/patient_service.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

/// Un FutureProvider qui charge la liste de patients une fois
/// et la garde en cache.
final patientListProvider = FutureProvider<List<PatientInfoDTO>>((ref) async {
  final patientService = ref.watch(patientServiceProvider);
  return patientService.getMyPatients();
});
