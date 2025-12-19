package com.clinique.api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Entité représentant un utilisateur pour l'authentification.
 * Elle implémente UserDetails pour s'intégrer directement avec Spring Security.
 */
@Data // Génère getters, setters, toString, equals, hashCode
@Builder // Pattern de construction pratique
@NoArgsConstructor // Constructeur sans arguments (requis par JPA)
@AllArgsConstructor // Constructeur avec tous les arguments (utilisé par @Builder)
@Entity // Marque cette classe comme une table de BDD
@Table(name = "users") // Spécifie le nom de la table (évite "user" qui est un mot-clé SQL)
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password; // Sera stocké haché

    @Enumerated(EnumType.STRING) // Stocke l'enum par son nom (ex: "ROLE_PATIENT")
    @Column(nullable = false)
    private Role role;

    // --- Méthodes de l'interface UserDetails ---
    // Spring Security utilisera ces méthodes pour gérer l'authentification et l'autorisation.

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Retourne la liste des rôles (autorisations)
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getUsername() {
        // Nous utilisons l'email comme nom d'utilisateur
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        // On peut ajouter une logique ici plus tard (ex: compte expiré)
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // On peut ajouter une logique de bannissement ici
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // On peut forcer un changement de mot de passe ici
        return true;
    }

    @Override
    public boolean isEnabled() {
        // On peut désactiver un compte ici
        return true;
    }
}