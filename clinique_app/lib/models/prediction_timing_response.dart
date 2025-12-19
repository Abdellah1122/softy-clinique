import 'package:equatable/equatable.dart';

class PredictionTimingResponse extends Equatable {
  final int recommendedDaysNextSession;

  const PredictionTimingResponse({required this.recommendedDaysNextSession});

  /// Factory constructor to create a model from JSON
  factory PredictionTimingResponse.fromJson(Map<String, dynamic> json) {
    return PredictionTimingResponse(
      recommendedDaysNextSession: json['recommended_days_next_session'],
    );
  }

  @override
  List<Object?> get props => [recommendedDaysNextSession];
}
