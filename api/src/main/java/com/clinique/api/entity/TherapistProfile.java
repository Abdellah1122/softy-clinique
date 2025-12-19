package com.clinique.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Entité représentant les informations de profil d'un thérapeute/professionnel.
 * Également séparé de l'entité User.
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "therapist_profiles")
public class TherapistProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Relation "Un-à-Un" : Un profil thérapeute est lié à UN seul compte User.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    /**
     * Spécialité du thérapeute (ex: "Kinésithérapeute", "Psychologue")
     */
    private String specialty;

    /**
     * Diplômes ou certifications (ex: "INPE, Diplôme d'état")
     */
    private String credentials;

    /**
     * Relation "Un-à-Plusieurs" : Un thérapeute peut avoir plusieurs rendez-vous.
     * mappedBy = "therapist" : Géré par le champ "therapist" dans l'entité Appointment.
     */
    @OneToMany(mappedBy = "therapist", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Appointment> appointments;
}