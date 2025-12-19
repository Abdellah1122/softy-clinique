package com.clinique.api.repository;

import com.clinique.api.entity.ClinicalNote;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repository pour l'entité ClinicalNote.
 */
public interface ClinicalNoteRepository extends JpaRepository<ClinicalNote, Long> {

    /**
     * Permet de trouver une note clinique en utilisant l'ID
     * du rendez-vous auquel elle est associée.
     */
    Optional<ClinicalNote> findByAppointmentId(Long appointmentId);
}