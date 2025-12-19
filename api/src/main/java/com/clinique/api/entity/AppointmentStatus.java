package com.clinique.api.entity;

/**
 * Définit les statuts possibles pour un rendez-vous.
 */
public enum AppointmentStatus {
    /**
     * Le rendez-vous est planifié et à venir.
     */
    SCHEDULED,

    /**
     * Le rendez-vous a eu lieu et est terminé.
     */
    COMPLETED,

    /**
     * Le rendez-vous a été annulé par le patient.
     */
    CANCELLED_BY_PATIENT,

    /**
     * Le rendez-vous a été annulé par le thérapeute.
     */
    CANCELLED_BY_THERAPIST
}