package com.clinique.api.service;

import com.clinique.api.dto.ProfileDTO;
import com.clinique.api.dto.UpdateProfileRequest;
import com.clinique.api.entity.*;
import com.clinique.api.exception.ResourceNotFoundException;
import com.clinique.api.repository.PatientProfileRepository;
import com.clinique.api.repository.TherapistProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour ProfileService.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProfileService - Tests du service de profil")
class ProfileServiceTest {

    @Mock
    private PatientProfileRepository patientProfileRepository;

    @Mock
    private TherapistProfileRepository therapistProfileRepository;

    @InjectMocks
    private ProfileService profileService;

    private User patientUser;
    private User therapistUser;
    private PatientProfile patientProfile;
    private TherapistProfile therapistProfile;

    @BeforeEach
    void setUp() {
        patientUser = User.builder()
                .id(1L)
                .email("patient@test.com")
                .role(Role.ROLE_PATIENT)
                .build();

        therapistUser = User.builder()
                .id(2L)
                .email("therapist@test.com")
                .role(Role.ROLE_THERAPIST)
                .build();

        patientProfile = new PatientProfile();
        patientProfile.setId(1L);
        patientProfile.setUser(patientUser);
        patientProfile.setFirstName("Ahmed");
        patientProfile.setLastName("Bennani");
        patientProfile.setDateOfBirth(LocalDate.of(1990, 5, 15));
        patientProfile.setPhoneNumber("+212612345678");

        therapistProfile = new TherapistProfile();
        therapistProfile.setId(1L);
        therapistProfile.setUser(therapistUser);
        therapistProfile.setFirstName("Dr. Fatima");
        therapistProfile.setLastName("Zahra");
        therapistProfile.setSpecialty("Kinésithérapeute");
        therapistProfile.setCredentials("Diplôme d'État");
    }

    /**
     * Vérifie qu'un patient peut récupérer son propre profil.
     */
    @Test
    @DisplayName("Doit récupérer le profil d'un patient")
    void shouldGetPatientProfile() {
        // Given
        when(patientProfileRepository.findByUserId(1L)).thenReturn(Optional.of(patientProfile));

        // When
        ProfileDTO result = profileService.getMyProfile(patientUser);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Ahmed", result.getFirstName());
        assertEquals("Bennani", result.getLastName());
        assertEquals("patient@test.com", result.getEmail());
        assertEquals(Role.ROLE_PATIENT, result.getRole());
        assertEquals(LocalDate.of(1990, 5, 15), result.getDateOfBirth());
        assertEquals("+212612345678", result.getPhoneNumber());
        verify(patientProfileRepository).findByUserId(1L);
    }

    /**
     * Vérifie qu'un thérapeute peut récupérer son propre profil.
     */
    @Test
    @DisplayName("Doit récupérer le profil d'un thérapeute")
    void shouldGetTherapistProfile() {
        // Given
        when(therapistProfileRepository.findByUserId(2L)).thenReturn(Optional.of(therapistProfile));

        // When
        ProfileDTO result = profileService.getMyProfile(therapistUser);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Dr. Fatima", result.getFirstName());
        assertEquals("Zahra", result.getLastName());
        assertEquals("therapist@test.com", result.getEmail());
        assertEquals(Role.ROLE_THERAPIST, result.getRole());
        assertEquals("Kinésithérapeute", result.getSpecialty());
        assertEquals("Diplôme d'État", result.getCredentials());
        verify(therapistProfileRepository).findByUserId(2L);
    }

    /**
     * Vérifie qu'une exception est lancée si le profil du patient est introuvable.
     */
    @Test
    @DisplayName("Doit lancer une exception si le profil patient n'existe pas")
    void shouldThrowExceptionWhenPatientProfileNotFound() {
        // Given
        when(patientProfileRepository.findByUserId(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            profileService.getMyProfile(patientUser);
        });
    }

    /**
     * Vérifie qu'une exception est lancée si le profil du thérapeute est
     * introuvable.
     */
    @Test
    @DisplayName("Doit lancer une exception si le profil thérapeute n'existe pas")
    void shouldThrowExceptionWhenTherapistProfileNotFound() {
        // Given
        when(therapistProfileRepository.findByUserId(2L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            profileService.getMyProfile(therapistUser);
        });
    }

    /**
     * Vérifie qu'un patient peut mettre à jour ses informations personnelles.
     */
    @Test
    @DisplayName("Doit mettre à jour le profil d'un patient")
    void shouldUpdatePatientProfile() {
        // Given
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFirstName("Mohammed");
        request.setLastName("Alami");
        request.setDateOfBirth(LocalDate.of(1992, 3, 20));
        request.setPhoneNumber("+212698765432");

        when(patientProfileRepository.findByUserId(1L)).thenReturn(Optional.of(patientProfile));
        when(patientProfileRepository.save(any(PatientProfile.class))).thenReturn(patientProfile);

        // When
        ProfileDTO result = profileService.updateMyProfile(patientUser, request);

        // Then
        assertNotNull(result);
        verify(patientProfileRepository).save(argThat(profile -> profile.getFirstName().equals("Mohammed") &&
                profile.getLastName().equals("Alami") &&
                profile.getDateOfBirth().equals(LocalDate.of(1992, 3, 20)) &&
                profile.getPhoneNumber().equals("+212698765432")));
    }

    /**
     * Vérifie qu'un thérapeute peut mettre à jour ses informations
     * professionnelles.
     */
    @Test
    @DisplayName("Doit mettre à jour le profil d'un thérapeute")
    void shouldUpdateTherapistProfile() {
        // Given
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFirstName("Dr. Youssef");
        request.setLastName("Idrissi");
        request.setSpecialty("Psychologue");
        request.setCredentials("Master en Psychologie");

        when(therapistProfileRepository.findByUserId(2L)).thenReturn(Optional.of(therapistProfile));
        when(therapistProfileRepository.save(any(TherapistProfile.class))).thenReturn(therapistProfile);

        // When
        ProfileDTO result = profileService.updateMyProfile(therapistUser, request);

        // Then
        assertNotNull(result);
        verify(therapistProfileRepository).save(argThat(profile -> profile.getFirstName().equals("Dr. Youssef") &&
                profile.getLastName().equals("Idrissi") &&
                profile.getSpecialty().equals("Psychologue") &&
                profile.getCredentials().equals("Master en Psychologie")));
    }

    /**
     * Vérifie qu'une exception est lancée lors de la tentative de mise à jour d'un
     * profil patient inexistant.
     */
    @Test
    @DisplayName("Doit lancer une exception lors de la mise à jour si le profil patient n'existe pas")
    void shouldThrowExceptionWhenUpdatingNonExistentPatientProfile() {
        // Given
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFirstName("Test");
        request.setLastName("User");

        when(patientProfileRepository.findByUserId(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            profileService.updateMyProfile(patientUser, request);
        });
    }

    /**
     * Vérifie qu'une exception est lancée lors de la tentative de mise à jour d'un
     * profil thérapeute inexistant.
     */
    @Test
    @DisplayName("Doit lancer une exception lors de la mise à jour si le profil thérapeute n'existe pas")
    void shouldThrowExceptionWhenUpdatingNonExistentTherapistProfile() {
        // Given
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFirstName("Test");
        request.setLastName("Therapist");

        when(therapistProfileRepository.findByUserId(2L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            profileService.updateMyProfile(therapistUser, request);
        });
    }

    /**
     * Vérifie qu'une exception est lancée si un utilisateur avec un rôle non
     * supporté tente d'accéder à son profil.
     */
    @Test
    @DisplayName("Doit lancer une exception pour un rôle non supporté")
    void shouldThrowExceptionForUnsupportedRole() {
        // Given
        User adminUser = User.builder()
                .id(3L)
                .email("admin@test.com")
                .role(Role.ROLE_ADMIN)
                .build();

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            profileService.getMyProfile(adminUser);
        });
    }

    /**
     * Vérifie qu'une exception est lancée si un utilisateur avec un rôle non
     * supporté tente de mettre à jour son profil.
     */
    @Test
    @DisplayName("Doit lancer une exception lors de la mise à jour pour un rôle non supporté")
    void shouldThrowExceptionWhenUpdatingUnsupportedRole() {
        // Given
        User adminUser = User.builder()
                .id(3L)
                .email("admin@test.com")
                .role(Role.ROLE_ADMIN)
                .build();

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFirstName("Admin");
        request.setLastName("User");

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            profileService.updateMyProfile(adminUser, request);
        });
    }
}