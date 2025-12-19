package com.clinique.api.dto;

import lombok.Data;

/**
 * Sous-DTO contenant les informations publiques d'un patient.
 * Utilisé à l'intérieur de AppointmentDTO.
 */
@Data
public class PatientInfoDTO {

    /**
     * L'ID du *profil* patient (PatientProfile).
     */
    private Long id;

    private String firstName;
    private String lastName;
}