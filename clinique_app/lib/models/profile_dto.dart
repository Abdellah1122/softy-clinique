import 'package:equatable/equatable.dart';

// This model must match the fields returned by your Java ProfileController
class ProfileDTO extends Equatable {
  // Common Fields
  final int id; // Profile ID
  final int userId;
  final String email;
  final String role;
  final String firstName;
  final String lastName;

  // Patient-Specific Fields
  final String? dateOfBirth; // Sent as ISO String from Java LocalDate
  final String? phoneNumber;

  // Therapist-Specific Fields
  final String? specialty;
  final String? credentials;

  const ProfileDTO({
    required this.id,
    required this.userId,
    required this.email,
    required this.role,
    required this.firstName,
    required this.lastName,
    this.dateOfBirth,
    this.phoneNumber,
    this.specialty,
    this.credentials,
  });

  factory ProfileDTO.fromJson(Map<String, dynamic> json) {
    return ProfileDTO(
      id: json['id'],
      userId: json['userId'],
      email: json['email'],
      role: json['role'],
      firstName: json['firstName'],
      lastName: json['lastName'],
      // Patient Fields
      dateOfBirth: json['dateOfBirth'],
      phoneNumber: json['phoneNumber'],
      // Therapist Fields
      specialty: json['specialty'],
      credentials: json['credentials'],
    );
  }

  @override
  List<Object?> get props => [id, email, role, firstName, specialty];

  bool get isPatient => role == 'ROLE_PATIENT';
}
