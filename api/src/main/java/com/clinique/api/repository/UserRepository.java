package com.clinique.api.repository;

import com.clinique.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repository pour l'entité User.
 * JpaRepository nous donne
 * - findAll(), findById(), save(), delete()
 * - et bien plus encore, gratuitement.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Spring Data JPA va automatiquement créer la requête SQL
     * juste à partir du nom de cette méthode.
     * C'est essentiel pour notre service de sécurité.
     */
    Optional<User> findByEmail(String email);

    /**
     * Une autre méthode "magique" pour vérifier si un email
     * existe déjà lors de l'inscription.
     */
    boolean existsByEmail(String email);
}