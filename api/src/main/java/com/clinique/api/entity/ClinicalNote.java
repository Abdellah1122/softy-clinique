package com.clinique.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "clinical_notes")
public class ClinicalNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false, unique = true)
    private Appointment appointment;

    // --- CORRECTION IMPORTANTE ---
    @Lob
    @Column(columnDefinition = "TEXT") // Force le type TEXT au lieu de OID
    private String summary;

    @Lob
    @Column(columnDefinition = "TEXT") // Force le type TEXT au lieu de OID
    private String privateNotes;
    // --- FIN CORRECTION ---

    @Column
    private Integer patientProgressScore;

    @Column
    private Double sentimentScore;

    @Column
    private String sentimentLabel;
}