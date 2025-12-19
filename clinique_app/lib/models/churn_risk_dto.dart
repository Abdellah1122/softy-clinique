import 'package:equatable/equatable.dart';

class ChurnRiskDTO extends Equatable {
  final bool isChurnRisk;
  final double churnProbability;

  const ChurnRiskDTO({
    required this.isChurnRisk,
    required this.churnProbability,
  });

  factory ChurnRiskDTO.fromJson(Map<String, dynamic> json) {
    return ChurnRiskDTO(
      isChurnRisk: json['isChurnRisk'] ?? false,
      churnProbability: (json['churnProbability'] as num?)?.toDouble() ?? 0.0,
    );
  }

  @override
  List<Object?> get props => [isChurnRisk, churnProbability];
}
