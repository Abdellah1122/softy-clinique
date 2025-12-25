package com.clinique.api.service;

import com.clinique.api.dto.*;
import com.clinique.api.entity.*;
import com.clinique.api.exception.ResourceNotFoundException;
import com.clinique.api.repository.AppointmentRepository;
import com.clinique.api.repository.PatientProfileRepository;
import com.clinique.api.repository.TherapistProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour AppointmentService.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AppointmentService - Tests du service de rendez-vous")
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PatientProfileRepository patientProfileRepository;

    @Mock
    private TherapistProfileRepository therapistProfileRepository;

    @Mock
    private WebClient mlWebClient;

    @InjectMocks
    private AppointmentService appointmentService;

    private User patientUser;
    private User therapistUser;
    private User adminUser;
    private PatientProfile patientProfile;
    private TherapistProfile therapistProfile;
    private Appointment appointment;

    @BeforeEach
    void setUp() {
        // Setup users
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

        adminUser = User.builder()
                .id(3L)
                .email("admin@test.com")
                .role(Role.ROLE_ADMIN)
                .build();

        // Setup profiles
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

        // Setup appointment
        appointment = new Appointment();
        appointment.setId(1L);
        appointment.setPatient(patientProfile);
        appointment.setTherapist(therapistProfile);
        appointment.setSessionDateTime(LocalDateTime.now().plusDays(7));
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setCreatedAt(LocalDateTime.now());
    }

    /**
     * Vérifie qu'un rendez-vous est créé avec succès lorsque toutes les données
     * sont valides.
     * Note: Ignore le fallback du WebClient pour le score de risque (non critique).
     */
    @Test
    @DisplayName("Doit créer un rendez-vous avec succès")
    void shouldCreateAppointmentSuccessfully() {
        // Given
        CreateAppointmentRequest request = new CreateAppointmentRequest();
        request.setPatientId(1L);
        request.setTherapistId(1L);
        request.setSessionDateTime(LocalDateTime.now().plusDays(7));

        when(patientProfileRepository.findById(1L)).thenReturn(Optional.of(patientProfile));
        when(therapistProfileRepository.findById(1L)).thenReturn(Optional.of(therapistProfile));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        // Note: Le WebClient échouera et le score sera null (comportement attendu)
        // On ne mock pas le WebClient car c'est trop complexe et ce n'est pas critique
        // pour ce test

        // When
        AppointmentDTO result = appointmentService.createAppointment(request);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(AppointmentStatus.SCHEDULED, result.getStatus());
        verify(appointmentRepository, atLeastOnce()).save(any(Appointment.class));
    }

    /**
     * Vérifie qu'une exception est lancée si l'on tente de créer un rendez-vous
     * pour un patient inexistant.
     */
    @Test
    @DisplayName("Doit lancer une exception si le patient n'existe pas")
    void shouldThrowExceptionWhenPatientNotFound() {
        // Given
        CreateAppointmentRequest request = new CreateAppointmentRequest();
        request.setPatientId(999L);
        request.setTherapistId(1L);
        request.setSessionDateTime(LocalDateTime.now().plusDays(7));

        when(patientProfileRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            appointmentService.createAppointment(request);
        });
    }

    /**
     * Vérifie qu'une exception est lancée si l'on tente de créer un rendez-vous
     * pour un thérapeute inexistant.
     */
    @Test
    @DisplayName("Doit lancer une exception si le thérapeute n'existe pas")
    void shouldThrowExceptionWhenTherapistNotFound() {
        // Given
        CreateAppointmentRequest request = new CreateAppointmentRequest();
        request.setPatientId(1L);
        request.setTherapistId(999L);
        request.setSessionDateTime(LocalDateTime.now().plusDays(7));

        when(patientProfileRepository.findById(1L)).thenReturn(Optional.of(patientProfile));
        when(therapistProfileRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            appointmentService.createAppointment(request);
        });
    }

    /**
     * Vérifie qu'un patient peut récupérer les détails de son propre rendez-vous.
     */
    @Test
    @DisplayName("Doit récupérer un rendez-vous par ID pour un patient")
    void shouldGetAppointmentByIdForPatient() {
        // Given
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        // When
        AppointmentDTO result = appointmentService.getAppointmentById(1L, patientUser);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(appointmentRepository).findById(1L);
    }

    /**
     * Vérifie qu'une exception est lancée si le rendez-vous demandé n'existe pas.
     */
    @Test
    @DisplayName("Doit lancer une exception si le rendez-vous n'existe pas")
    void shouldThrowExceptionWhenAppointmentNotFound() {
        // Given
        when(appointmentRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            appointmentService.getAppointmentById(999L, patientUser);
        });
    }

    /**
     * Vérifie qu'un patient ne peut pas accéder aux rendez-vous d'un autre patient.
     */
    @Test
    @DisplayName("Doit refuser l'accès à un patient non autorisé")
    void shouldDenyAccessToUnauthorizedPatient() {
        // Given
        User otherPatient = User.builder()
                .id(99L)
                .email("other@test.com")
                .role(Role.ROLE_PATIENT)
                .build();

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        // When & Then
        assertThrows(AccessDeniedException.class, () -> {
            appointmentService.getAppointmentById(1L, otherPatient);
        });
    }

    /**
     * Vérifie qu'un administrateur peut accéder à n'importe quel rendez-vous.
     */
    @Test
    @DisplayName("Doit autoriser l'accès à un admin")
    void shouldAllowAccessToAdmin() {
        // Given
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        // When
        AppointmentDTO result = appointmentService.getAppointmentById(1L, adminUser);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    /**
     * Vérifie qu'un patient peut récupérer la liste de tous ses rendez-vous.
     */
    @Test
    @DisplayName("Doit récupérer les rendez-vous d'un patient")
    void shouldGetAppointmentsForPatient() {
        // Given
        List<Appointment> appointments = Arrays.asList(appointment);
        when(patientProfileRepository.findById(1L)).thenReturn(Optional.of(patientProfile));
        when(appointmentRepository.findByPatientId(1L)).thenReturn(appointments);

        // When
        List<AppointmentDTO> result = appointmentService.getAppointmentsForPatient(1L, patientUser);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    /**
     * Vérifie qu'il est interdit de récupérer la liste des rendez-vous d'un autre
     * patient.
     */
    @Test
    @DisplayName("Doit refuser l'accès aux rendez-vous d'un autre patient")
    void shouldDenyAccessToOtherPatientAppointments() {
        // Given
        User otherPatient = User.builder()
                .id(99L)
                .role(Role.ROLE_PATIENT)
                .build();

        when(patientProfileRepository.findById(1L)).thenReturn(Optional.of(patientProfile));

        // When & Then
        assertThrows(AccessDeniedException.class, () -> {
            appointmentService.getAppointmentsForPatient(1L, otherPatient);
        });
    }

    /**
     * Vérifie qu'un thérapeute peut récupérer la liste de ses propres rendez-vous.
     */
    @Test
    @DisplayName("Doit récupérer les rendez-vous d'un thérapeute")
    void shouldGetAppointmentsForTherapist() {
        // Given
        List<Appointment> appointments = Arrays.asList(appointment);
        when(therapistProfileRepository.findById(1L)).thenReturn(Optional.of(therapistProfile));
        when(appointmentRepository.findByTherapistId(1L)).thenReturn(appointments);

        // When
        List<AppointmentDTO> result = appointmentService.getAppointmentsForTherapist(1L, therapistUser);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    /**
     * Vérifie qu'il est interdit de récupérer la liste des rendez-vous d'un autre
     * thérapeute (sauf admin).
     */
    @Test
    @DisplayName("Doit refuser l'accès aux rendez-vous d'un autre thérapeute")
    void shouldDenyAccessToOtherTherapistAppointments() {
        // Given
        User otherTherapist = User.builder()
                .id(99L)
                .role(Role.ROLE_THERAPIST)
                .build();

        when(therapistProfileRepository.findById(1L)).thenReturn(Optional.of(therapistProfile));

        // When & Then
        assertThrows(AccessDeniedException.class, () -> {
            appointmentService.getAppointmentsForTherapist(1L, otherTherapist);
        });
    }

    /**
     * Vérifie qu'un administrateur a le droit de voir le planning d'un thérapeute.
     */
    @Test
    @DisplayName("Doit permettre à un admin de voir les rendez-vous d'un thérapeute")
    void shouldAllowAdminToSeeTherapistAppointments() {
        // Given
        List<Appointment> appointments = Arrays.asList(appointment);
        when(therapistProfileRepository.findById(1L)).thenReturn(Optional.of(therapistProfile));
        when(appointmentRepository.findByTherapistId(1L)).thenReturn(appointments);

        // When
        List<AppointmentDTO> result = appointmentService.getAppointmentsForTherapist(1L, adminUser);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    /**
     * Vérifie qu'un patient peut annuler son propre rendez-vous.
     */
    @Test
    @DisplayName("Doit annuler un rendez-vous par le patient")
    void shouldCancelAppointmentByPatient() {
        // Given
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        // When
        AppointmentDTO result = appointmentService.cancelAppointment(1L, patientUser);

        // Then
        assertNotNull(result);
        verify(appointmentRepository).save(any(Appointment.class));
    }

    /**
     * Vérifie qu'un patient ne peut pas annuler le rendez-vous de quelqu'un
     * d'autre.
     */
    @Test
    @DisplayName("Doit refuser l'annulation par un autre patient")
    void shouldDenyCancellationByOtherPatient() {
        // Given
        User otherPatient = User.builder()
                .id(99L)
                .role(Role.ROLE_PATIENT)
                .build();

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        // When & Then
        assertThrows(AccessDeniedException.class, () -> {
            appointmentService.cancelAppointment(1L, otherPatient);
        });
    }

    /**
     * Vérifie qu'il est impossible d'annuler un rendez-vous déjà marqué comme
     * terminé.
     */
    @Test
    @DisplayName("Doit refuser l'annulation d'un rendez-vous terminé")
    void shouldDenyCancellationOfCompletedAppointment() {
        // Given
        appointment.setStatus(AppointmentStatus.COMPLETED);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            appointmentService.cancelAppointment(1L, patientUser);
        });
    }

    /**
     * Vérifie qu'un thérapeute peut marquer un rendez-vous comme terminé (après la
     * séance).
     */
    @Test
    @DisplayName("Doit marquer un rendez-vous comme terminé par le thérapeute")
    void shouldCompleteAppointmentByTherapist() {
        // Given
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        // When
        AppointmentDTO result = appointmentService.completeAppointment(1L, therapistUser);

        // Then
        assertNotNull(result);
        verify(appointmentRepository).save(any(Appointment.class));
    }

    /**
     * Vérifie qu'un thérapeute ne peut pas clore le rendez-vous d'un autre
     * thérapeute.
     */
    @Test
    @DisplayName("Doit refuser la complétion par un autre thérapeute")
    void shouldDenyCompletionByOtherTherapist() {
        // Given
        User otherTherapist = User.builder()
                .id(99L)
                .role(Role.ROLE_THERAPIST)
                .build();

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        // When & Then
        assertThrows(AccessDeniedException.class, () -> {
            appointmentService.completeAppointment(1L, otherTherapist);
        });
    }

    /**
     * Vérifie que le système retourne une recommandation par défaut (7 jours) en
     * l'absence de score de progrès précédent.
     */
    @Test
    @DisplayName("Doit obtenir une recommandation de timing par défaut si pas de score")
    void shouldGetDefaultTimingRecommendationWhenNoScore() {
        // Given
        when(patientProfileRepository.existsById(1L)).thenReturn(true);
        when(appointmentRepository.findLastProgressScoreByPatientId(1L)).thenReturn(null);

        // When
        PredictionTimingResponse result = appointmentService.getTimingRecommendation(1L);

        // Then
        assertNotNull(result);
        assertEquals(7, result.getRecommendedDaysNextSession());
    }

    /**
     * Vérifie qu'une exception est lancée si on demande une recommandation pour un
     * patient inconnu.
     */
    @Test
    @DisplayName("Doit lancer une exception si le patient n'existe pas pour timing")
    void shouldThrowExceptionWhenPatientNotFoundForTiming() {
        // Given
        when(patientProfileRepository.existsById(999L)).thenReturn(false);

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            appointmentService.getTimingRecommendation(999L);
        });
    }
}