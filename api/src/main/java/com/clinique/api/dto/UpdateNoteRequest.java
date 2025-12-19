package com.clinique.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateNoteRequest {

    @NotBlank(message = "Le résumé ne peut pas être vide")
    private String summary;

    private String privateNotes;

    // --- NOUVEAU CHAMP ---
    /**
     * Score de 1 à 10.
     */
    @Min(value = 1, message = "Le score doit être au minimum 1")
    @Max(value = 10, message = "Le score doit être au maximum 10")
    private Integer patientProgressScore;
}