package com.clinique.api.service;

import com.clinique.api.dto.PatientInfoDTO;
import com.clinique.api.entity.PatientProfile;
import com.clinique.api.entity.Role;
import com.clinique.api.entity.TherapistProfile;
import com.clinique.api.entity.User;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour PatientService.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PatientService - Tests du service patient")
class PatientServiceTest {

    @Mock
    private PatientProfileRepository patientProfileRepository;

    @Mock
    private TherapistProfileRepository therapistProfileRepository;

    @InjectMocks
    private PatientService patientService;

    private User therapistUser;
    private TherapistProfile therapistProfile;
    private PatientProfile patient1;
    private PatientProfile patient2;

    @BeforeEach
    void setUp() {
        therapistUser = User.builder()
                .id(1L)
                .email("therapist@test.com")
                .role(Role.ROLE_THERAPIST)
                .build();

        therapistProfile = new TherapistProfile();
        therapistProfile.setId(1L);
        therapistProfile.setUser(therapistUser);
        therapistProfile.setFirstName("Dr. Fatima");
        therapistProfile.setLastName("Zahra");

        User patientUser1 = User.builder()
                .id(2L)
                .email("patient1@test.com")
                .role(Role.ROLE_PATIENT)
                .build();

        User patientUser2 = User.builder()
                .id(3L)
                .email("patient2@test.com")
                .role(Role.ROLE_PATIENT)
                .build();

        patient1 = new PatientProfile();
        patient1.setId(1L);
        patient1.setUser(patientUser1);
        patient1.setFirstName("Ahmed");
        patient1.setLastName("Bennani");

        patient2 = new PatientProfile();
        patient2.setId(2L);
        patient2.setUser(patientUser2);
        patient2.setFirstName("Fatima");
        patient2.setLastName("Zahra");
    }

    /**
     * Vérifie qu'un thérapeute peut récupérer la liste de tous ses patients
     * distincts.
     */
    @Test
    @DisplayName("Doit récupérer tous les patients d'un thérapeute")
    void shouldGetAllPatientsForTherapist() {
        // Given
        List<PatientProfile> patients = Arrays.asList(patient1, patient2);
        when(therapistProfileRepository.findByUserId(1L)).thenReturn(Optional.of(therapistProfile));
        when(patientProfileRepository.findDistinctPatientsByTherapistId(1L)).thenReturn(patients);

        // When
        List<PatientInfoDTO> result = patientService.getMyPatients(therapistUser);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Ahmed", result.get(0).getFirstName());
        assertEquals("Bennani", result.get(0).getLastName());
        assertEquals("Fatima", result.get(1).getFirstName());
        assertEquals("Zahra", result.get(1).getLastName());
        verify(therapistProfileRepository).findByUserId(1L);
        verify(patientProfileRepository).findDistinctPatientsByTherapistId(1L);
    }

    /**
     * Vérifie que la liste est vide si le thérapeute n'a aucun patient assigné.
     */
    @Test
    @DisplayName("Doit retourner une liste vide si le thérapeute n'a pas de patients")
    void shouldReturnEmptyListWhenTherapistHasNoPatients() {
        // Given
        when(therapistProfileRepository.findByUserId(1L)).thenReturn(Optional.of(therapistProfile));
        when(patientProfileRepository.findDistinctPatientsByTherapistId(1L)).thenReturn(Collections.emptyList());

        // When
        List<PatientInfoDTO> result = patientService.getMyPatients(therapistUser);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(patientProfileRepository).findDistinctPatientsByTherapistId(1L);
    }

    /**
     * Vérifie qu'une exception est lancée si le profil du thérapeute n'est pas
     * trouvé.
     */
    @Test
    @DisplayName("Doit lancer une exception si le profil thérapeute n'existe pas")
    void shouldThrowExceptionWhenTherapistProfileNotFound() {
        // Given
        when(therapistProfileRepository.findByUserId(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            patientService.getMyPatients(therapistUser);
        });
        verify(patientProfileRepository, never()).findDistinctPatientsByTherapistId(anyLong());
    }

    /**
     * Vérifie que les données des patients sont correctement mappées dans le DTO de
     * retour.
     */
    @Test
    @DisplayName("Doit mapper correctement les informations du patient")
    void shouldMapPatientInfoCorrectly() {
        // Given
        List<PatientProfile> patients = Arrays.asList(patient1);
        when(therapistProfileRepository.findByUserId(1L)).thenReturn(Optional.of(therapistProfile));
        when(patientProfileRepository.findDistinctPatientsByTherapistId(1L)).thenReturn(patients);

        // When
        List<PatientInfoDTO> result = patientService.getMyPatients(therapistUser);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        PatientInfoDTO dto = result.get(0);
        assertEquals(1L, dto.getId());
        assertEquals("Ahmed", dto.getFirstName());
        assertEquals("Bennani", dto.getLastName());
    }

    /**
     * Vérifie que le service gère correctement une liste de plusieurs patients.
     */
    @Test
    @DisplayName("Doit gérer plusieurs patients avec des noms différents")
    void shouldHandleMultiplePatientsWithDifferentNames() {
        // Given
        PatientProfile patient3 = new PatientProfile();
        patient3.setId(3L);
        patient3.setFirstName("Youssef");
        patient3.setLastName("Idrissi");

        List<PatientProfile> patients = Arrays.asList(patient1, patient2, patient3);
        when(therapistProfileRepository.findByUserId(1L)).thenReturn(Optional.of(therapistProfile));
        when(patientProfileRepository.findDistinctPatientsByTherapistId(1L)).thenReturn(patients);

        // When
        List<PatientInfoDTO> result = patientService.getMyPatients(therapistUser);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("Youssef", result.get(2).getFirstName());
        assertEquals("Idrissi", result.get(2).getLastName());
    }
}