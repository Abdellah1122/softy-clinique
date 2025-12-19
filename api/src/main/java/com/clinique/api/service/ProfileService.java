package com.clinique.api.service;

import com.clinique.api.dto.ProfileDTO;
import com.clinique.api.dto.UpdateProfileRequest;
import com.clinique.api.entity.PatientProfile;
import com.clinique.api.entity.Role;
import com.clinique.api.entity.TherapistProfile;
import com.clinique.api.entity.User;
import com.clinique.api.exception.ResourceNotFoundException;
import com.clinique.api.repository.PatientProfileRepository;
import com.clinique.api.repository.TherapistProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final PatientProfileRepository patientProfileRepository;
    private final TherapistProfileRepository therapistProfileRepository;

    /**
     * Fetches the profile for the currently logged-in user.
     */
    @Transactional(readOnly = true)
    public ProfileDTO getMyProfile(User currentUser) {
        if (currentUser.getRole() == Role.ROLE_PATIENT) {
            PatientProfile profile = patientProfileRepository.findByUserId(currentUser.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found"));
            return mapPatientToProfileDTO(profile, currentUser);

        } else if (currentUser.getRole() == Role.ROLE_THERAPIST) {
            TherapistProfile profile = therapistProfileRepository.findByUserId(currentUser.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Therapist profile not found"));
            return mapTherapistToProfileDTO(profile, currentUser);
        }

        throw new IllegalStateException("User role not supported for profiles.");
    }

    /**
     * Updates the profile for the currently logged-in user.
     */
    @Transactional
    public ProfileDTO updateMyProfile(User currentUser, UpdateProfileRequest request) {
        if (currentUser.getRole() == Role.ROLE_PATIENT) {
            PatientProfile profile = patientProfileRepository.findByUserId(currentUser.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found"));

            // Update patient fields
            profile.setFirstName(request.getFirstName());
            profile.setLastName(request.getLastName());
            profile.setDateOfBirth(request.getDateOfBirth());
            profile.setPhoneNumber(request.getPhoneNumber());
            PatientProfile updatedProfile = patientProfileRepository.save(profile);
            return mapPatientToProfileDTO(updatedProfile, currentUser);

        } else if (currentUser.getRole() == Role.ROLE_THERAPIST) {
            TherapistProfile profile = therapistProfileRepository.findByUserId(currentUser.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Therapist profile not found"));

            // Update therapist fields
            profile.setFirstName(request.getFirstName());
            profile.setLastName(request.getLastName());
            profile.setSpecialty(request.getSpecialty());
            profile.setCredentials(request.getCredentials());
            TherapistProfile updatedProfile = therapistProfileRepository.save(profile);
            return mapTherapistToProfileDTO(updatedProfile, currentUser);
        }

        throw new IllegalStateException("User role not supported for profiles.");
    }

    // --- Manual Mappers ---

    private ProfileDTO mapPatientToProfileDTO(PatientProfile profile, User user) {
        ProfileDTO dto = new ProfileDTO();
        dto.setId(profile.getId());
        dto.setUserId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setFirstName(profile.getFirstName());
        dto.setLastName(profile.getLastName());
        dto.setDateOfBirth(profile.getDateOfBirth());
        dto.setPhoneNumber(profile.getPhoneNumber());
        return dto;
    }

    private ProfileDTO mapTherapistToProfileDTO(TherapistProfile profile, User user) {
        ProfileDTO dto = new ProfileDTO();
        dto.setId(profile.getId());
        dto.setUserId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setFirstName(profile.getFirstName());
        dto.setLastName(profile.getLastName());
        dto.setSpecialty(profile.getSpecialty());
        dto.setCredentials(profile.getCredentials());
        return dto;
    }
}