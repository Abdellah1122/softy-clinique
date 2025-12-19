package com.clinique.api.service;

import com.clinique.api.dto.*;
import com.clinique.api.entity.*;
import com.clinique.api.exception.ResourceNotFoundException;
// PAS D'IMPORT DE MAPPER
import com.clinique.api.repository.AppointmentRepository;
import com.clinique.api.repository.PatientProfileRepository;
import com.clinique.api.repository.TherapistProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final TherapistProfileRepository therapistProfileRepository;
    // PAS DE MAPPER INJECTÉ
    private final WebClient mlWebClient;

    @Transactional
    public AppointmentDTO createAppointment(CreateAppointmentRequest request) {
        PatientProfile patient = patientProfileRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Profil patient non trouvé: " + request.getPatientId()));

        TherapistProfile therapist = therapistProfileRepository.findById(request.getTherapistId())
                .orElseThrow(() -> new ResourceNotFoundException("Profil thérapeute non trouvé: " + request.getTherapistId()));

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setTherapist(therapist);
        appointment.setSessionDateTime(request.getSessionDateTime());
        appointment.setStatus(AppointmentStatus.SCHEDULED);

        Appointment savedAppointment = appointmentRepository.save(appointment);

        try {
            Double riskScore = getPredictionScore(savedAppointment);
            savedAppointment.setCancellationRiskScore(riskScore);
            savedAppointment = appointmentRepository.save(savedAppointment);
            log.info("Score de risque calculé avec succès: {}", riskScore);
        } catch (Exception e) {
            log.warn("Impossible de contacter le service de ML (risque). Le RDV est créé sans score. Erreur: {}", e.getMessage());
            savedAppointment.setCancellationRiskScore(null);
        }

        // MAPPING MANUEL
        return mapToAppointmentDTO(savedAppointment);
    }

    // ... (les méthodes getPredictionScore et getTimingRecommendation restent les mêmes) ...
    private Double getPredictionScore(Appointment appointment) {
        double leadTimeDays = (double) ChronoUnit.DAYS.between(
                appointment.getCreatedAt().toLocalDate(),
                appointment.getSessionDateTime().toLocalDate()
        );
        int dayOfWeek = appointment.getSessionDateTime().getDayOfWeek().getValue();
        int hourOfDay = appointment.getSessionDateTime().getHour();
        if (dayOfWeek == 7) dayOfWeek = 0;

        PredictionRequest predictionRequest = new PredictionRequest(leadTimeDays, dayOfWeek, hourOfDay);
        log.info("Appel du service de ML (risque) avec les features: {}", predictionRequest);

        PredictionResponse response = mlWebClient.post()
                .uri("/predict")
                .bodyValue(predictionRequest)
                .retrieve()
                .bodyToMono(PredictionResponse.class)
                .block(Duration.ofSeconds(2));

        if (response != null) {
            return response.getCancellationRiskScore();
        }
        return null;
    }

    @Transactional(readOnly = true)
    public PredictionTimingResponse getTimingRecommendation(Long patientProfileId) {
        if (!patientProfileRepository.existsById(patientProfileId)) {
            throw new ResourceNotFoundException("Patient not found with id: " + patientProfileId);
        }
        Integer lastScore = appointmentRepository.findLastProgressScoreByPatientId(patientProfileId);
        if (lastScore == null) {
            log.warn("Pas de score de progression trouvé pour le patient {}", patientProfileId);
            PredictionTimingResponse defaultResponse = new PredictionTimingResponse();
            defaultResponse.setRecommendedDaysNextSession(7);
            return defaultResponse;
        }
        PredictionTimingRequest timingRequest = new PredictionTimingRequest(lastScore);
        log.info("Appel du service de ML (timing) avec le score: {}", lastScore);
        try {
            return mlWebClient.post()
                    .uri("/predict-timing")
                    .bodyValue(timingRequest)
                    .retrieve()
                    .bodyToMono(PredictionTimingResponse.class)
                    .block(Duration.ofSeconds(2));
        } catch (Exception e) {
            log.error("Erreur lors de l'appel du service de prédiction de timing: {}", e.getMessage());
            PredictionTimingResponse defaultResponse = new PredictionTimingResponse();
            defaultResponse.setRecommendedDaysNextSession(7);
            return defaultResponse;
        }
    }


    @Transactional(readOnly = true)
    public AppointmentDTO getAppointmentById(Long id, User currentUser) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rendez-vous non trouvé avec l'ID: " + id));

        assertCanAccessAppointment(appointment, currentUser);
        // MAPPING MANUEL
        return mapToAppointmentDTO(appointment);
    }

    @Transactional(readOnly = true)
    public List<AppointmentDTO> getAppointmentsForPatient(Long patientProfileId, User currentUser) {
        PatientProfile patient = patientProfileRepository.findById(patientProfileId)
                .orElseThrow(() -> new ResourceNotFoundException("Profil patient non trouvé avec l'ID: " + patientProfileId));

        if (currentUser.getRole() == Role.ROLE_PATIENT && !patient.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Vous n'êtes pas autorisé à voir les rendez-vous de cet utilisateur.");
        }

        // MAPPING MANUEL
        return appointmentRepository.findByPatientId(patientProfileId).stream()
                .map(this::mapToAppointmentDTO) // Utilise notre méthode manuelle
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AppointmentDTO> getAppointmentsForTherapist(Long therapistProfileId, User currentUser) {
        TherapistProfile therapist = therapistProfileRepository.findById(therapistProfileId)
                .orElseThrow(() -> new ResourceNotFoundException("Profil thérapeute non trouvé avec l'ID: " + therapistProfileId));

        if (currentUser.getRole() == Role.ROLE_ADMIN) {
            // L'admin peut continuer
        } else if (currentUser.getRole() == Role.ROLE_THERAPIST) {
            if (!therapist.getUser().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("Vous n'êtes pas autorisé à voir les rendez-vous de ce thérapeute.");
            }
        } else {
            throw new AccessDeniedException("Vous n'êtes pas autorisé à accéder à cette ressource.");
        }

        // MAPPING MANUEL
        return appointmentRepository.findByTherapistId(therapistProfileId).stream()
                .map(this::mapToAppointmentDTO) // Utilise notre méthode manuelle
                .collect(Collectors.toList());
    }

    private void assertCanAccessAppointment(Appointment appointment, User user) {
        // ... (cette méthode reste la même) ...
        if (user.getRole() == Role.ROLE_ADMIN) { return; }
        if (user.getRole() == Role.ROLE_PATIENT) {
            if (!appointment.getPatient().getUser().getId().equals(user.getId())) {
                throw new AccessDeniedException("Accès refusé");
            }
        }
        if (user.getRole() == Role.ROLE_THERAPIST) {
            if (!appointment.getTherapist().getUser().getId().equals(user.getId())) {
                throw new AccessDeniedException("Accès refusé");
            }
        }
    }

    // --- NOTRE NOUVELLE MÉTHODE DE MAPPING MANUEL ---
    private AppointmentDTO mapToAppointmentDTO(Appointment appointment) {
        // 1. Mapper le Patient
        PatientInfoDTO patientInfo = new PatientInfoDTO();
        patientInfo.setId(appointment.getPatient().getId());
        patientInfo.setFirstName(appointment.getPatient().getFirstName());
        patientInfo.setLastName(appointment.getPatient().getLastName());

        // 2. Mapper le Thérapeute
        TherapistInfoDTO therapistInfo = new TherapistInfoDTO();
        therapistInfo.setId(appointment.getTherapist().getId());
        therapistInfo.setFirstName(appointment.getTherapist().getFirstName());
        therapistInfo.setLastName(appointment.getTherapist().getLastName());
        therapistInfo.setSpecialty(appointment.getTherapist().getSpecialty());

        // 3. Mapper la Note (avec une vérification null)
        ClinicalNoteDTO noteInfo = null;
        if (appointment.getNote() != null) {
            noteInfo = new ClinicalNoteDTO();
            noteInfo.setId(appointment.getNote().getId());
            noteInfo.setSummary(appointment.getNote().getSummary());
            noteInfo.setPatientProgressScore(appointment.getNote().getPatientProgressScore());
        }

        // 4. Construire le DTO principal
        AppointmentDTO dto = new AppointmentDTO();
        dto.setId(appointment.getId());
        dto.setSessionDateTime(appointment.getSessionDateTime());
        dto.setStatus(appointment.getStatus());
        dto.setCancellationRiskScore(appointment.getCancellationRiskScore());
        dto.setPatient(patientInfo);
        dto.setTherapist(therapistInfo);
        dto.setNote(noteInfo);

        return dto;
    }
    @Transactional
    public AppointmentDTO cancelAppointment(Long appointmentId, User currentUser) {
        // 1. Trouver le RDV
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Rendez-vous non trouvé avec l'ID: " + appointmentId));

        // 2. Vérifier que l'utilisateur est bien le patient de ce RDV
        if (!appointment.getPatient().getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Accès refusé : vous n'êtes pas le patient pour ce rendez-vous.");
        }

        // 3. Vérifier si le RDV n'est pas déjà terminé
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new IllegalStateException("Impossible d'annuler un rendez-vous déjà terminé.");
        }

        // 4. Mettre à jour le statut
        appointment.setStatus(AppointmentStatus.CANCELLED_BY_PATIENT);
        Appointment savedAppointment = appointmentRepository.save(appointment);

        return mapToAppointmentDTO(savedAppointment); // Utilise notre mapping manuel
    }

    /**
     * Permet à un thérapeute de marquer un rendez-vous comme terminé.
     */
    @Transactional
    public AppointmentDTO completeAppointment(Long appointmentId, User currentUser) {
        // 1. Trouver le RDV
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Rendez-vous non trouvé avec l'ID: " + appointmentId));

        // 2. Vérifier que l'utilisateur est bien le thérapeute de ce RDV
        if (!appointment.getTherapist().getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Accès refusé : vous n'êtes pas le thérapeute pour ce rendez-vous.");
        }

        // 3. Mettre à jour le statut
        appointment.setStatus(AppointmentStatus.COMPLETED);
        Appointment savedAppointment = appointmentRepository.save(appointment);

        return mapToAppointmentDTO(savedAppointment); // Utilise notre mapping manuel
    }
}