package com.clinique.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO pour la requête de connexion (login).
 * Utilise les annotations de validation pour s'assurer
 * que les données envoyées sont valides.
 */
@Data // Génère getters, setters, etc.
public class AuthRequest {

    @Email(message = "L'email doit être valide")
    @NotBlank(message = "L'email ne peut pas être vide")
    private String email;

    @NotBlank(message = "Le mot de passe ne peut pas être vide")
    private String password;
}