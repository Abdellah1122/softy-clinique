package com.clinique.api.dto;

import lombok.Data;

/**
 * Sous-DTO contenant les informations publiques d'un thérapeute.
 * Utilisé à l'intérieur de AppointmentDTO.
 */
@Data
public class TherapistInfoDTO {

    /**
     * L'ID du *profil* thérapeute (TherapistProfile).
     */
    private Long id;

    private String firstName;
    private String lastName;
    private String specialty;
}