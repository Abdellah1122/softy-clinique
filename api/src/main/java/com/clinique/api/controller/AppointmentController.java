package com.clinique.api.controller;

import com.clinique.api.dto.AppointmentDTO;
import com.clinique.api.dto.CreateAppointmentRequest;
import com.clinique.api.dto.PredictionTimingResponse;
import com.clinique.api.dto.UpdateNoteRequest;
import com.clinique.api.entity.User;
import com.clinique.api.service.AppointmentService;
import com.clinique.api.service.ClinicalNoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur gérant les endpoints pour les rendez-vous (protégés).
 */
@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final ClinicalNoteService clinicalNoteService;

    /**
     * Endpoint pour créer un rendez-vous.
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_THERAPIST')")
    public ResponseEntity<AppointmentDTO> createAppointment(
            @Valid @RequestBody CreateAppointmentRequest request
    ) {
        AppointmentDTO appointment = appointmentService.createAppointment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(appointment);
    }

    /**
     * Endpoint pour récupérer un rendez-vous par son ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AppointmentDTO> getAppointmentById(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        AppointmentDTO appointment = appointmentService.getAppointmentById(id, currentUser);
        return ResponseEntity.ok(appointment);
    }

    /**
     * Endpoint pour récupérer tous les rendez-vous d'un PATIENT spécifique.
     */
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AppointmentDTO>> getAppointmentsForPatient(
            @PathVariable Long patientId,
            @AuthenticationPrincipal User currentUser
    ) {
        List<AppointmentDTO> appointments = appointmentService.getAppointmentsForPatient(patientId, currentUser);
        return ResponseEntity.ok(appointments);
    }

    /**
     * Endpoint pour récupérer tous les rendez-vous d'un THÉRAPEUTE spécifique.
     */
    @GetMapping("/therapist/{therapistId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AppointmentDTO>> getAppointmentsForTherapist(
            @PathVariable Long therapistId,
            @AuthenticationPrincipal User currentUser
    ) {
        List<AppointmentDTO> appointments = appointmentService.getAppointmentsForTherapist(therapistId, currentUser);
        return ResponseEntity.ok(appointments);
    }

    /**
     * Endpoint pour créer ou mettre à jour la note clinique d'un RDV.
     */
    @PutMapping("/{appointmentId}/note")
    @PreAuthorize("hasAuthority('ROLE_THERAPIST')")
    public ResponseEntity<AppointmentDTO> addOrUpdateNote(
            @PathVariable Long appointmentId,
            @Valid @RequestBody UpdateNoteRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        AppointmentDTO updatedAppointment = clinicalNoteService.addOrUpdateNote(appointmentId, request, currentUser);
        return ResponseEntity.ok(updatedAppointment);
    }

    /**
     * Endpoint pour obtenir une recommandation de timing
     * pour le prochain RDV d'un patient.
     */
    @GetMapping("/patient/{patientId}/recommendation")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_THERAPIST')")
    public ResponseEntity<PredictionTimingResponse> getTimingRecommendation(
            @PathVariable Long patientId
    ) {
        PredictionTimingResponse recommendation = appointmentService.getTimingRecommendation(patientId);
        return ResponseEntity.ok(recommendation);
    }
    @PutMapping("/{appointmentId}/cancel")
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    public ResponseEntity<AppointmentDTO> cancelAppointment(
            @PathVariable Long appointmentId,
            @AuthenticationPrincipal User currentUser
    ) {
        AppointmentDTO updatedAppointment = appointmentService.cancelAppointment(appointmentId, currentUser);
        return ResponseEntity.ok(updatedAppointment);
    }

    /**
     * Endpoint pour qu'un THÉRAPEUTE termine un rendez-vous.
     */
    @PutMapping("/{appointmentId}/complete")
    @PreAuthorize("hasAuthority('ROLE_THERAPIST')")
    public ResponseEntity<AppointmentDTO> completeAppointment(
            @PathVariable Long appointmentId,
            @AuthenticationPrincipal User currentUser
    ) {
        AppointmentDTO updatedAppointment = appointmentService.completeAppointment(appointmentId, currentUser);
        return ResponseEntity.ok(updatedAppointment);
    }
}