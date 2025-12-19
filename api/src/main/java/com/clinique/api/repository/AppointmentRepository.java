package com.clinique.api.repository;

import com.clinique.api.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository pour l'entité Appointment.
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    /**
     * Trouve tous les rendez-vous associés à un ID de profil patient spécifique.
     */
    @Query("SELECT a FROM Appointment a WHERE a.patient.id = :patientId")
    List<Appointment> findByPatientId(@Param("patientId") Long patientId);

    /**
     * Trouve tous les rendez-vous associés à un ID de profil thérapeute spécifique.
     */
    @Query("SELECT a FROM Appointment a WHERE a.therapist.id = :therapistId")
    List<Appointment> findByTherapistId(@Param("therapistId") Long therapistId);

    /**
     * Trouve le score de progression le plus récent pour un patient donné,
     * en ne regardant que les RDV qui ont une note et un score.
     */
    @Query("SELECT cn.patientProgressScore " +
            "FROM Appointment a " +
            "JOIN a.note cn " +
            "WHERE a.patient.id = :patientId " +
            "AND cn.patientProgressScore IS NOT NULL " +
            "ORDER BY a.sessionDateTime DESC " +
            "LIMIT 1")
    Integer findLastProgressScoreByPatientId(@Param("patientId") Long patientId);
}