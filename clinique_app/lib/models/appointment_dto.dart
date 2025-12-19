import 'package:equatable/equatable.dart';

// This file defines all the models related to an Appointment,
// matching the DTOs from your Spring Boot API.

class AppointmentDTO extends Equatable {
  final int id;
  final String sessionDateTime;
  final String status;
  final double? cancellationRiskScore;
  final PatientInfoDTO patient;
  final TherapistInfoDTO therapist;
  final ClinicalNoteDTO? note;

  const AppointmentDTO({
    required this.id,
    required this.sessionDateTime,
    required this.status,
    this.cancellationRiskScore,
    required this.patient,
    required this.therapist,
    this.note,
  });

  factory AppointmentDTO.fromJson(Map<String, dynamic> json) {
    return AppointmentDTO(
      id: json['id'],
      sessionDateTime: json['sessionDateTime'],
      status: json['status'],
      cancellationRiskScore: json['cancellationRiskScore'],
      patient: PatientInfoDTO.fromJson(json['patient']),
      therapist: TherapistInfoDTO.fromJson(json['therapist']),
      note: json['note'] != null
          ? ClinicalNoteDTO.fromJson(json['note'])
          : null,
    );
  }

  @override
  List<Object?> get props => [
    id,
    sessionDateTime,
    status,
    patient,
    therapist,
    note,
  ];
}

// --- Sub-Models ---

class PatientInfoDTO extends Equatable {
  final int id;
  final String firstName;
  final String lastName;

  const PatientInfoDTO({
    required this.id,
    required this.firstName,
    required this.lastName,
  });

  factory PatientInfoDTO.fromJson(Map<String, dynamic> json) {
    return PatientInfoDTO(
      id: json['id'],
      firstName: json['firstName'],
      lastName: json['lastName'],
    );
  }

  @override
  List<Object?> get props => [id, firstName, lastName];
}

class TherapistInfoDTO extends Equatable {
  final int id;
  final String firstName;
  final String lastName;
  final String? specialty;

  const TherapistInfoDTO({
    required this.id,
    required this.firstName,
    required this.lastName,
    this.specialty,
  });

  factory TherapistInfoDTO.fromJson(Map<String, dynamic> json) {
    return TherapistInfoDTO(
      id: json['id'],
      firstName: json['firstName'],
      lastName: json['lastName'],
      specialty: json['specialty'],
    );
  }

  @override
  List<Object?> get props => [id, firstName, lastName, specialty];
}

// --- CORRECTION HERE ---
class ClinicalNoteDTO extends Equatable {
  final int id;
  final String? summary;
  final int? patientProgressScore;
  final double? sentimentScore;
  final String? sentimentLabel;

  const ClinicalNoteDTO({
    required this.id,
    this.summary,
    this.patientProgressScore,
    this.sentimentScore,
    this.sentimentLabel,
  });

  factory ClinicalNoteDTO.fromJson(Map<String, dynamic> json) {
    return ClinicalNoteDTO(
      id: json['id'],
      summary: json['summary'],
      patientProgressScore: json['patientProgressScore'],
      sentimentScore: json['sentimentScore'],
      sentimentLabel: json['sentimentLabel'],
    );
  }

  @override
  List<Object?> get props => [id, summary, patientProgressScore, sentimentScore, sentimentLabel];
}
