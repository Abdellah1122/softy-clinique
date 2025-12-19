package com.clinique.api.dto;

import lombok.Builder;
import lombok.Data;

/**
 * DTO pour la réponse d'authentification.
 * Contient le token JWT et les informations de base de l'utilisateur.
 */
@Data // Génère getters, setters, etc.
@Builder // Permet de construire l'objet facilement (ex: AuthResponse.builder().token(...).build())
public class AuthResponse {

    /**
     * Le token JWT que Flutter doit stocker et renvoyer
     * dans les en-têtes de ses prochaines requêtes.
     */
    private String token;

    /**
     * Les informations sur l'utilisateur qui vient de se connecter.
     * (Nous allons créer UserDTO juste après).
     */
    private UserDTO user;
}