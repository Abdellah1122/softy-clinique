import 'package:clinique_app/core/api_client.dart';
import 'package:clinique_app/models/appointment_dto.dart';
import 'package:clinique_app/models/prediction_timing_response.dart';
import 'package:clinique_app/services/auth_service.dart';
import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

// --- Provider Definition ---
final appointmentServiceProvider = Provider<AppointmentService>((ref) {
  return AppointmentService(ref.watch(apiClientProvider));
});

// --- Service Class ---
class AppointmentService {
  final ApiClient _apiClient;

  AppointmentService(this._apiClient);

  /// Fetches a list of appointments for a specific therapist
  Future<List<AppointmentDTO>> getAppointmentsForTherapist(
    int therapistId,
  ) async {
    try {
      final response = await _apiClient.dio.get(
        '/appointments/therapist/$therapistId',
      );
      final List<dynamic> jsonList = response.data;
      return jsonList.map((json) => AppointmentDTO.fromJson(json)).toList();
    } on DioException catch (e) {
      print("Failed to fetch appointments: ${e.response?.data}");
      throw Exception('Failed to fetch appointments: ${e.message}');
    }
  }

  /// Fetches a list of appointments for a specific patient
  Future<List<AppointmentDTO>> getAppointmentsForPatient(int patientId) async {
    try {
      final response = await _apiClient.dio.get(
        '/appointments/patient/$patientId',
      );
      final List<dynamic> jsonList = response.data;
      return jsonList.map((json) => AppointmentDTO.fromJson(json)).toList();
    } on DioException catch (e) {
      print("Failed to fetch patient appointments: ${e.response?.data}");
      throw Exception('Failed to fetch patient appointments: ${e.message}');
    }
  }

  /// Fetches a timing recommendation for a patient
  Future<PredictionTimingResponse> getRecommendation(int patientId) async {
    try {
      final response = await _apiClient.dio.get(
        '/appointments/patient/$patientId/recommendation',
      );
      return PredictionTimingResponse.fromJson(response.data);
    } on DioException catch (e) {
      print("Failed to fetch recommendation: ${e.response?.data}");
      throw Exception('Failed to fetch recommendation: ${e.message}');
    }
  }

  /// Creates a new appointment
  Future<AppointmentDTO> createAppointment({
    required int patientId,
    required int therapistId,
    required DateTime sessionDateTime,
  }) async {
    try {
      final response = await _apiClient.dio.post(
        '/appointments',
        data: {
          'patientId': patientId,
          'therapistId': therapistId,
          'sessionDateTime': sessionDateTime.toIso8601String(),
        },
      );
      return AppointmentDTO.fromJson(response.data);
    } on DioException catch (e) {
      print("Failed to create appointment: ${e.response?.data}");
      String errorMessage =
          e.response?.data['message'] ?? e.message ?? "Unknown error";
      if (e.response?.data['sessionDateTime'] != null) {
        errorMessage = e.response!.data['sessionDateTime'];
      }
      throw Exception('Failed to create appointment: $errorMessage');
    }
  }

  /// --- THIS IS THE MISSING METHOD ---
  /// Adds or updates a clinical note
  Future<AppointmentDTO> addOrUpdateNote({
    required int appointmentId,
    required String summary,
    String? privateNotes,
    required int patientProgressScore,
  }) async {
    try {
      final response = await _apiClient.dio.put(
        '/appointments/$appointmentId/note',
        data: {
          'summary': summary,
          'privateNotes': privateNotes,
          'patientProgressScore': patientProgressScore,
        },
      );
      return AppointmentDTO.fromJson(response.data);
    } on DioException catch (e) {
      print("Failed to update note: ${e.response?.data}");
      throw Exception('Failed to update note: ${e.message}');
    }
  }

  Future<AppointmentDTO> updateAppointmentStatus({
    required int appointmentId,
    required String action, // 'cancel' or 'complete'
  }) async {
    try {
      final response = await _apiClient.dio.put(
        '/appointments/$appointmentId/$action',
      );
      // The API returns the updated appointment DTO
      return AppointmentDTO.fromJson(response.data);
    } on DioException catch (e) {
      print("Failed to update status ($action): ${e.response?.data}");
      String errorMessage =
          e.response?.data['message'] ?? e.message ?? "Unknown error";
      throw Exception('Failed to update status: $errorMessage');
    }
  }
}
