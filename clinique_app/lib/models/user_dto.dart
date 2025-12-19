import 'package:equatable/equatable.dart';

// We use Equatable to easily compare two UserDTO objects
class UserDTO extends Equatable {
  final int id;
  final String email;
  final String role;
  final int profileId;

  const UserDTO({
    required this.id,
    required this.email,
    required this.role,
    required this.profileId,
  });

  /// Factory constructor to create a UserDTO from a JSON map
  /// This is how we parse the JSON from the API
  factory UserDTO.fromJson(Map<String, dynamic> json) {
    return UserDTO(
      id: json['id'],
      email: json['email'],
      role: json['role'],
      profileId: json['profileId'],
    );
  }

  /// Lists the properties to be used for comparison
  @override
  List<Object?> get props => [id, email, role, profileId];
}
