package com.clinique.api.dto;

import com.clinique.api.entity.Role;
import lombok.Data;

/**
 * DTO représentant les informations publiques d'un utilisateur.
 * C'est une version "sûre" de l'entité User à exposer à l'extérieur.
 */
@Data
public class UserDTO {

    /**
     * L'ID de l'entité User (authentification).
     */
    private Long id;

    private String email;

    private Role role;

    /**
     * L'ID du PROFIL (PatientProfile ou TherapistProfile)
     * associé à ce compte User. C'est crucial pour Flutter,
     * pour qu'il sache quel profil aller chercher.
     */
    private Long profileId;
}