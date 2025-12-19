import 'package:clinique_app/models/user_dto.dart';
import 'package:equatable/equatable.dart';

class AuthResponse extends Equatable {
  final String token;
  final UserDTO user;

  const AuthResponse({required this.token, required this.user});

  /// Factory constructor to parse the JSON from the API
  factory AuthResponse.fromJson(Map<String, dynamic> json) {
    return AuthResponse(
      token: json['token'],
      // We also parse the nested UserDTO object
      user: UserDTO.fromJson(json['user']),
    );
  }

  @override
  List<Object?> get props => [token, user];
}
