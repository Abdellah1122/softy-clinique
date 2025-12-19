package com.clinique.api.repository;

import com.clinique.api.entity.TherapistProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repository pour l'entité TherapistProfile.
 */
public interface TherapistProfileRepository extends JpaRepository<TherapistProfile, Long> {

    /**
     * Permet de trouver le profil d'un thérapeute
     * en utilisant l'ID de son compte User associé.
     */
    Optional<TherapistProfile> findByUserId(Long userId);
}