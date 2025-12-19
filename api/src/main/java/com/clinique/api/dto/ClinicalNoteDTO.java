package com.clinique.api.dto;

import lombok.Data;

@Data
public class ClinicalNoteDTO { // Assurez-vous que cette classe n'est pas 'private'
    private Long id;
    private String summary;

    // --- NOUVEAU CHAMP ---
    private Integer patientProgressScore;
    private Double sentimentScore;
    private String sentimentLabel;
}