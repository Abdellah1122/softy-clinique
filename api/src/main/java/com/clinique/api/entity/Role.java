package com.clinique.api.entity;

/**
 * Définit les rôles possibles pour un utilisateur.
 * Spring Security utilisera ces rôles préfixés par "ROLE_".
 */
public enum Role {
    /**
     * Un utilisateur qui reçoit un traitement.
     */
    ROLE_PATIENT,

    /**
     * Un professionnel de la clinique (médecin, thérapeute).
     */
    ROLE_THERAPIST,

    /**
     * Un utilisateur avec des droits administratifs (gestion, etc.).
     */
    ROLE_ADMIN
}