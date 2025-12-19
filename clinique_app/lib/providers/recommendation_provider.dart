import 'package:clinique_app/models/prediction_timing_response.dart';
import 'package:clinique_app/services/appointment_service.dart';
import 'package:equatable/equatable.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

// -------------------------------------------------
// 1. Define the states
// -------------------------------------------------

/// Represents the possible states for our recommendation fetch
enum RecommendationStatus { idle, loading, success, error }

/// The data class that holds our recommendation state
class RecommendationState extends Equatable {
  final RecommendationStatus status;
  final PredictionTimingResponse? recommendation;
  final String? error;

  const RecommendationState({
    this.status = RecommendationStatus.idle,
    this.recommendation,
    this.error,
  });

  // Helper to create a copy of the state
  RecommendationState copyWith({
    RecommendationStatus? status,
    PredictionTimingResponse? recommendation,
    String? error,
  }) {
    return RecommendationState(
      status: status ?? this.status,
      recommendation: recommendation ?? this.recommendation,
      error: error ?? this.error,
    );
  }

  @override
  List<Object?> get props => [status, recommendation, error];
}

// -------------------------------------------------
// 2. Define the Notifier (The "Controller")
// -------------------------------------------------

class RecommendationNotifier extends StateNotifier<RecommendationState> {
  final AppointmentService _appointmentService;

  RecommendationNotifier(this._appointmentService)
    : super(const RecommendationState());

  /// Fetches the recommendation for a specific patient
  Future<void> fetchRecommendation(int patientId) async {
    state = state.copyWith(status: RecommendationStatus.loading, error: null);
    try {
      final response = await _appointmentService.getRecommendation(patientId);
      state = state.copyWith(
        status: RecommendationStatus.success,
        recommendation: response,
      );
    } catch (e) {
      state = state.copyWith(
        status: RecommendationStatus.error,
        error: e.toString(),
      );
    }
  }

  /// Resets the provider to its initial idle state
  void reset() {
    state = const RecommendationState();
  }
}

// -------------------------------------------------
// 3. Define the Global Provider
// -------------------------------------------------

/// The global provider our UI will use
final recommendationProvider =
    StateNotifierProvider<RecommendationNotifier, RecommendationState>((ref) {
      final appointmentService = ref.watch(appointmentServiceProvider);
      return RecommendationNotifier(appointmentService);
    });
