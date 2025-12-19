package com.clinique.api.dto;

import com.clinique.api.entity.AppointmentStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AppointmentDTO {
    private Long id;
    private LocalDateTime sessionDateTime;
    private AppointmentStatus status;
    private Double cancellationRiskScore;
    private PatientInfoDTO patient;
    private TherapistInfoDTO therapist;
    private ClinicalNoteDTO note;
}



