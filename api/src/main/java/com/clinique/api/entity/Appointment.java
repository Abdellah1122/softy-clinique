package com.clinique.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp; // <-- IMPORTEZ CECI

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_profile_id", nullable = false)
    private PatientProfile patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "therapist_profile_id", nullable = false)
    private TherapistProfile therapist;

    @Column(nullable = false)
    private LocalDateTime sessionDateTime;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AppointmentStatus status;

    @OneToOne(mappedBy = "appointment", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private ClinicalNote note;

    // --- NOUVEAUX CHAMPS ---

    /**
     * Stocke la date et l'heure de création du rendez-vous.
     * @CreationTimestamp : Rempli automatiquement par Hibernate lors de la création.
     * updatable = false : Garantit que ce champ ne sera jamais modifié.
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Stocke le score de risque de prédiction (ex: 0.85 pour 85%).
     * Ce champ sera 'null' jusqu'à ce que le service de ML l'ait calculé.
     */
    @Column
    private Double cancellationRiskScore;
}