import 'package:clinique_app/core/api_client.dart';
import 'package:clinique_app/models/appointment_dto.dart'; // Contient PatientInfoDTO
import 'package:clinique_app/models/churn_risk_dto.dart';
import 'package:clinique_app/services/auth_service.dart'; // Pour le apiClientProvider
import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

// --- Provider ---
final patientServiceProvider = Provider<PatientService>((ref) {
  return PatientService(ref.watch(apiClientProvider));
});

// --- Service ---
class PatientService {
  final ApiClient _apiClient;

  PatientService(this._apiClient);

  /// Récupère la liste de tous les patients du thérapeute
  Future<List<PatientInfoDTO>> getMyPatients() async {
    try {
      final response = await _apiClient.dio.get('/patients');

      final List<dynamic> jsonList = response.data;
      return jsonList.map((json) => PatientInfoDTO.fromJson(json)).toList();
    } on DioException catch (e) {
      print("Failed to fetch patients: ${e.response?.data}");
      throw Exception('Failed to fetch patients: ${e.message}');
    }
  }

  /// NEW: Fetches churn risk for a specific patient
  Future<ChurnRiskDTO?> getChurnRisk(int patientId) async {
    try {
      final response = await _apiClient.dio.get('/patients/$patientId/churn-risk');
      return ChurnRiskDTO.fromJson(response.data);
    } on DioException catch (e) {
      // Just print error but don't crash UI, return null
      print("Failed to fetch churn risk: ${e.response?.data}");
      return null;
    }
  }
}
