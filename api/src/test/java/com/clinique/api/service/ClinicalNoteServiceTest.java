package com.clinique.api.service;

import com.clinique.api.dto.AppointmentDTO;
import com.clinique.api.dto.UpdateNoteRequest;
import com.clinique.api.entity.*;
import com.clinique.api.exception.ResourceNotFoundException;
import com.clinique.api.repository.AppointmentRepository;
import com.clinique.api.repository.ClinicalNoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour ClinicalNoteService.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ClinicalNoteService - Tests du service de notes cliniques")
class ClinicalNoteServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private ClinicalNoteRepository clinicalNoteRepository;

    @Mock
    private MlService mlService;

    @InjectMocks
    private ClinicalNoteService clinicalNoteService;

    private User therapistUser;
    private User otherTherapistUser;
    private PatientProfile patientProfile;
    private TherapistProfile therapistProfile;
    private Appointment appointment;
    private ClinicalNote clinicalNote;

    @BeforeEach
    void setUp() {
        User patientUser = User.builder()
                .id(1L)
                .email("patient@test.com")
                .role(Role.ROLE_PATIENT)
                .build();

        therapistUser = User.builder()
                .id(2L)
                .email("therapist@test.com")
                .role(Role.ROLE_THERAPIST)
                .build();

        otherTherapistUser = User.builder()
                .id(3L)
                .email("other@test.com")
                .role(Role.ROLE_THERAPIST)
                .build();

        patientProfile = new PatientProfile();
        patientProfile.setId(1L);
        patientProfile.setUser(patientUser);
        patientProfile.setFirstName("Ahmed");
        patientProfile.setLastName("Bennani");

        therapistProfile = new TherapistProfile();
        therapistProfile.setId(1L);
        therapistProfile.setUser(therapistUser);
        therapistProfile.setFirstName("Dr. Fatima");
        therapistProfile.setLastName("Zahra");
        therapistProfile.setSpecialty("Kinésithérapeute");

        appointment = new Appointment();
        appointment.setId(1L);
        appointment.setPatient(patientProfile);
        appointment.setTherapist(therapistProfile);
        appointment.setSessionDateTime(LocalDateTime.now());
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setCreatedAt(LocalDateTime.now());

        clinicalNote = new ClinicalNote();
        clinicalNote.setId(1L);
        clinicalNote.setAppointment(appointment);
        clinicalNote.setSummary("Test summary");
        clinicalNote.setPrivateNotes("Private notes");
        clinicalNote.setPatientProgressScore(8);
    }

    /**
     * Vérifie qu'une nouvelle note clinique est ajoutée correctement au
     * rendez-vous.
     */
    @Test
    @DisplayName("Doit ajouter une nouvelle note clinique")
    void shouldAddNewClinicalNote() {
        // Given
        UpdateNoteRequest request = new UpdateNoteRequest();
        request.setSummary("Le patient progresse bien");
        request.setPrivateNotes("Notes privées du thérapeute");
        request.setPatientProgressScore(7);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(clinicalNoteRepository.findByAppointmentId(1L)).thenReturn(Optional.empty());
        when(clinicalNoteRepository.save(any(ClinicalNote.class))).thenReturn(clinicalNote);

        // When
        AppointmentDTO result = clinicalNoteService.addOrUpdateNote(1L, request, therapistUser);

        // Then
        assertNotNull(result);
        verify(clinicalNoteRepository).save(argThat(note -> note.getSummary().equals("Le patient progresse bien") &&
                note.getPrivateNotes().equals("Notes privées du thérapeute") &&
                note.getPatientProgressScore() == 7));
    }

    /**
     * Vérifie qu'une note clinique existante est mise à jour correctement.
     */
    @Test
    @DisplayName("Doit mettre à jour une note clinique existante")
    void shouldUpdateExistingClinicalNote() {
        // Given
        UpdateNoteRequest request = new UpdateNoteRequest();
        request.setSummary("Mise à jour de la note");
        request.setPrivateNotes("Nouvelles notes privées");
        request.setPatientProgressScore(9);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(clinicalNoteRepository.findByAppointmentId(1L)).thenReturn(Optional.of(clinicalNote));
        when(clinicalNoteRepository.save(any(ClinicalNote.class))).thenReturn(clinicalNote);

        // When
        AppointmentDTO result = clinicalNoteService.addOrUpdateNote(1L, request, therapistUser);

        // Then
        assertNotNull(result);
        verify(clinicalNoteRepository).save(argThat(note -> note.getSummary().equals("Mise à jour de la note") &&
                note.getPrivateNotes().equals("Nouvelles notes privées") &&
                note.getPatientProgressScore() == 9));
    }

    /**
     * Vérifie qu'une exception est lancée si l'on tente d'ajouter une note à un
     * rendez-vous inexistant.
     */
    @Test
    @DisplayName("Doit lancer une exception si le rendez-vous n'existe pas")
    void shouldThrowExceptionWhenAppointmentNotFound() {
        // Given
        UpdateNoteRequest request = new UpdateNoteRequest();
        request.setSummary("Test");

        when(appointmentRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            clinicalNoteService.addOrUpdateNote(999L, request, therapistUser);
        });
    }

    /**
     * Vérifie qu'un thérapeute ne peut pas modifier la note d'un rendez-vous qui ne
     * lui est pas assigné.
     */
    @Test
    @DisplayName("Doit refuser l'accès à un thérapeute non autorisé")
    void shouldDenyAccessToUnauthorizedTherapist() {
        // Given
        UpdateNoteRequest request = new UpdateNoteRequest();
        request.setSummary("Test");

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        // When & Then
        assertThrows(AccessDeniedException.class, () -> {
            clinicalNoteService.addOrUpdateNote(1L, request, otherTherapistUser);
        });
    }

    /**
     * Vérifie qu'il est possible de créer une note sans attribuer de score de
     * progrès.
     */
    @Test
    @DisplayName("Doit créer une note avec un score de progrès null")
    void shouldCreateNoteWithNullProgressScore() {
        // Given
        UpdateNoteRequest request = new UpdateNoteRequest();
        request.setSummary("Première séance");
        request.setPrivateNotes("Notes initiales");
        request.setPatientProgressScore(null);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(clinicalNoteRepository.findByAppointmentId(1L)).thenReturn(Optional.empty());
        when(clinicalNoteRepository.save(any(ClinicalNote.class))).thenReturn(clinicalNote);

        // When
        AppointmentDTO result = clinicalNoteService.addOrUpdateNote(1L, request, therapistUser);

        // Then
        assertNotNull(result);
        verify(clinicalNoteRepository).save(argThat(note -> note.getPatientProgressScore() == null));
    }

    /**
     * Vérifie que la relation entre la note clinique et le rendez-vous est bien
     * établie.
     */
    @Test
    @DisplayName("Doit lier la note au rendez-vous")
    void shouldLinkNoteToAppointment() {
        // Given
        UpdateNoteRequest request = new UpdateNoteRequest();
        request.setSummary("Test");
        request.setPatientProgressScore(5);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(clinicalNoteRepository.findByAppointmentId(1L)).thenReturn(Optional.empty());
        when(clinicalNoteRepository.save(any(ClinicalNote.class))).thenReturn(clinicalNote);

        // When
        clinicalNoteService.addOrUpdateNote(1L, request, therapistUser);

        // Then
        verify(clinicalNoteRepository).save(argThat(note -> note.getAppointment().equals(appointment)));
    }

    /**
     * Vérifie que le DTO retourné contient bien les informations de la note
     * nouvellement créée/mise à jour.
     */
    @Test
    @DisplayName("Doit retourner un AppointmentDTO avec la note")
    void shouldReturnAppointmentDTOWithNote() {
        // Given
        UpdateNoteRequest request = new UpdateNoteRequest();
        request.setSummary("Test note");
        request.setPatientProgressScore(8);

        appointment.setNote(clinicalNote);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(clinicalNoteRepository.findByAppointmentId(1L)).thenReturn(Optional.of(clinicalNote));
        when(clinicalNoteRepository.save(any(ClinicalNote.class))).thenReturn(clinicalNote);

        // When
        AppointmentDTO result = clinicalNoteService.addOrUpdateNote(1L, request, therapistUser);

        // Then
        assertNotNull(result);
        assertNotNull(result.getNote());
        assertEquals(1L, result.getNote().getId());
    }

    /**
     * Vérifie qu'il est possible d'attribuer le score de progrès maximum (10).
     */
    @Test
    @DisplayName("Doit créer une note avec un score de progrès de 10")
    void shouldCreateNoteWithMaxProgressScore() {
        // Given
        UpdateNoteRequest request = new UpdateNoteRequest();
        request.setSummary("Excellent progrès");
        request.setPatientProgressScore(10);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(clinicalNoteRepository.findByAppointmentId(1L)).thenReturn(Optional.empty());
        when(clinicalNoteRepository.save(any(ClinicalNote.class))).thenReturn(clinicalNote);

        // When
        AppointmentDTO result = clinicalNoteService.addOrUpdateNote(1L, request, therapistUser);

        // Then
        assertNotNull(result);
        verify(clinicalNoteRepository).save(argThat(note -> note.getPatientProgressScore() == 10));
    }
}