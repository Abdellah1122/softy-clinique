package com.clinique.api.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * DTO pour la requête de création d'un rendez-vous.
 */
@Data
public class CreateAppointmentRequest {

    /**
     * L'ID du *profil* patient (PatientProfile).
     */
    @NotNull(message = "L'ID du patient est obligatoire")
    private Long patientId;

    /**
     * L'ID du *profil* thérapeute (TherapistProfile).
     */
    @NotNull(message = "L'ID du thérapeute est obligatoire")
    private Long therapistId;

    /**
     * La date et l'heure du rendez-vous.
     * @Future : Valide que la date est bien dans le futur.
     */
    @NotNull(message = "La date et l'heure sont obligatoires")
    @Future(message = "La date du rendez-vous doit être dans le futur")
    private LocalDateTime sessionDateTime;
}