package com.clinique.api.dto;

import com.clinique.api.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO pour la requête d'inscription (création de compte).
 * Inclut la validation pour tous les champs requis.
 */
@Data
public class RegisterRequest {

    // --- Champs pour l'entité User ---

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, message = "Le mot de passe doit faire au moins 8 caractères")
    private String password;

    @NotNull(message = "Le rôle (ROLE_PATIENT ou ROLE_THERAPIST) est obligatoire")
    private Role role;

    // --- Champs pour l'entité Profile (Patient ou Thérapeute) ---

    @NotBlank(message = "Le prénom est obligatoire")
    private String firstName;

    @NotBlank(message = "Le nom de famille est obligatoire")
    private String lastName;

    // --- Champ optionnel, spécifique au thérapeute ---

    /**
     * Spécialité (ex: "Kinésithérapeute").
     * Ne sera utilisé que si le rôle est ROLE_THERAPIST.
     */
    private String specialty;
}