package com.clinique.api.repository;

import com.clinique.api.entity.PatientProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PatientProfileRepository extends JpaRepository<PatientProfile, Long> {

    Optional<PatientProfile> findByUserId(Long userId);

    /**
     * NOUVELLE MÉTHODE: Trouve tous les patients uniques (distincts) qui ont eu un
     * rendez-vous avec un thérapeute spécifique.
     */
    @Query("SELECT DISTINCT a.patient FROM Appointment a WHERE a.therapist.id = :therapistId")
    List<PatientProfile> findDistinctPatientsByTherapistId(@Param("therapistId") Long therapistId);
}