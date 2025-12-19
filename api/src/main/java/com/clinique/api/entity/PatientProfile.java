package com.clinique.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * Entité représentant les informations de profil d'un patient.
 * Ceci est séparé de l'entité User pour une meilleure organisation (Auth vs Profile).
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "patient_profiles")
public class PatientProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * C'est le lien clé.
     * Relation "Un-à-Un" : Un profil patient est lié à UN seul compte User.
     * fetch = FetchType.LAZY : Ne charge l'objet User que si on y accède (bonne pratique).
     * unique = true : Garantit qu'un User ne peut avoir qu'un seul profil patient.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    private LocalDate dateOfBirth;

    private String phoneNumber;

    /**
     * Relation "Un-à-Plusieurs" : Un patient peut avoir plusieurs rendez-vous.
     * mappedBy = "patient" : Indique à JPA que la relation est gérée
     * de l'autre côté (dans le champ "patient" de l'entité Appointment).
     * cascade = CascadeType.ALL : Si on supprime un patient, ses rendez-vous sont aussi supprimés.
     */
    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Appointment> appointments;
}