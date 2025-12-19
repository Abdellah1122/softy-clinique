package com.clinique.api.service;

import com.clinique.api.dto.AuthRequest;
import com.clinique.api.dto.AuthResponse;
import com.clinique.api.dto.RegisterRequest;
import com.clinique.api.dto.UserDTO;
import com.clinique.api.entity.*;
import com.clinique.api.exception.ResourceNotFoundException;
// PAS DE MAPPER
import com.clinique.api.repository.PatientProfileRepository;
import com.clinique.api.repository.TherapistProfileRepository;
import com.clinique.api.repository.UserRepository;
import com.clinique.api.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final TherapistProfileRepository therapistProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    // PAS DE MAPPER

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Un compte avec cet email existe déjà");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();
        User savedUser = userRepository.save(user);

        Long profileId;
        if (request.getRole() == Role.ROLE_PATIENT) {
            PatientProfile patientProfile = new PatientProfile();
            patientProfile.setUser(savedUser);
            patientProfile.setFirstName(request.getFirstName());
            patientProfile.setLastName(request.getLastName());
            profileId = patientProfileRepository.save(patientProfile).getId();

        } else if (request.getRole() == Role.ROLE_THERAPIST) {
            TherapistProfile therapistProfile = new TherapistProfile();
            therapistProfile.setUser(savedUser);
            therapistProfile.setFirstName(request.getFirstName());
            therapistProfile.setLastName(request.getLastName());
            therapistProfile.setSpecialty(request.getSpecialty());
            profileId = therapistProfileRepository.save(therapistProfile).getId();
        } else {
            throw new IllegalArgumentException("Rôle non valide pour l'inscription");
        }

        var jwtToken = jwtService.generateToken(savedUser);

        // MAPPING MANUEL
        UserDTO userDTO = new UserDTO();
        userDTO.setId(savedUser.getId());
        userDTO.setEmail(savedUser.getEmail());
        userDTO.setRole(savedUser.getRole());
        userDTO.setProfileId(profileId);

        return AuthResponse.builder()
                .token(jwtToken)
                .user(userDTO)
                .build();
    }

    @Transactional(readOnly = true)
    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));

        Long profileId = findProfileId(user);
        var jwtToken = jwtService.generateToken(user);

        // MAPPING MANUEL
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setEmail(user.getEmail());
        userDTO.setRole(user.getRole());
        userDTO.setProfileId(profileId);

        return AuthResponse.builder()
                .token(jwtToken)
                .user(userDTO)
                .build();
    }

    private Long findProfileId(User user) {
        if (user.getRole() == Role.ROLE_PATIENT) {
            return patientProfileRepository.findByUserId(user.getId())
                    .map(PatientProfile::getId)
                    .orElse(null);
        } else if (user.getRole() == Role.ROLE_THERAPIST) {
            return therapistProfileRepository.findByUserId(user.getId())
                    .map(TherapistProfile::getId)
                    .orElse(null);
        }
        return null;
    }
}