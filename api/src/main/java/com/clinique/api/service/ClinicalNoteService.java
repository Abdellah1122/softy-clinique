package com.clinique.api.service;

import com.clinique.api.dto.*;
import com.clinique.api.entity.Appointment;
import com.clinique.api.entity.ClinicalNote;
import com.clinique.api.entity.User;
import com.clinique.api.exception.ResourceNotFoundException;
// PAS DE MAPPER
import com.clinique.api.repository.AppointmentRepository;
import com.clinique.api.repository.ClinicalNoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClinicalNoteService {

    private final AppointmentRepository appointmentRepository;
    private final ClinicalNoteRepository clinicalNoteRepository;
    private final MlService mlService; // Inject MlService
    // PAS DE MAPPER

    @Transactional
    public AppointmentDTO addOrUpdateNote(Long appointmentId, UpdateNoteRequest request, User currentUser) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Rendez-vous non trouvé avec l'ID: " + appointmentId));

        if (!appointment.getTherapist().getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Vous n'êtes pas autorisé à écrire une note pour ce rendez-vous.");
        }

        ClinicalNote note = clinicalNoteRepository.findByAppointmentId(appointmentId)
                .orElse(new ClinicalNote());

        note.setAppointment(appointment);
        note.setSummary(request.getSummary());
        note.setPrivateNotes(request.getPrivateNotes());
        note.setPatientProgressScore(request.getPatientProgressScore());

        // --- SENTIMENT ANALYSIS ---
        if (request.getSummary() != null && !request.getSummary().isEmpty()) {
            java.util.Map<String, Object> sentiment = mlService.analyzeSentiment(request.getSummary());
            if (sentiment != null) {
                if (sentiment.get("polarity") instanceof Double) {
                    note.setSentimentScore((Double) sentiment.get("polarity"));
                } else {
                    // Handle possibly different number format in JSON response
                }
                note.setSentimentLabel((String) sentiment.get("sentiment_label"));
            }
        }

        ClinicalNote savedNote = clinicalNoteRepository.save(note);
        appointment.setNote(savedNote);

        // MAPPING MANUEL
        return mapToAppointmentDTO(appointment);
    }

    // --- NOTRE NOUVELLE MÉTHODE DE MAPPING MANUEL ---
    private AppointmentDTO mapToAppointmentDTO(Appointment appointment) {
        PatientInfoDTO patientInfo = new PatientInfoDTO();
        patientInfo.setId(appointment.getPatient().getId());
        patientInfo.setFirstName(appointment.getPatient().getFirstName());
        patientInfo.setLastName(appointment.getPatient().getLastName());

        TherapistInfoDTO therapistInfo = new TherapistInfoDTO();
        therapistInfo.setId(appointment.getTherapist().getId());
        therapistInfo.setFirstName(appointment.getTherapist().getFirstName());
        therapistInfo.setLastName(appointment.getTherapist().getLastName());
        therapistInfo.setSpecialty(appointment.getTherapist().getSpecialty());

        ClinicalNoteDTO noteInfo = null;
        if (appointment.getNote() != null) {
            noteInfo = new ClinicalNoteDTO();
            noteInfo.setId(appointment.getNote().getId());
            noteInfo.setSummary(appointment.getNote().getSummary());
            noteInfo.setPatientProgressScore(appointment.getNote().getPatientProgressScore());
            noteInfo.setSentimentScore(appointment.getNote().getSentimentScore());
            noteInfo.setSentimentLabel(appointment.getNote().getSentimentLabel());
        }

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
}