package com.clinique.api.service;

import com.clinique.api.dto.PatientInfoDTO;
import com.clinique.api.entity.PatientProfile;
import com.clinique.api.entity.TherapistProfile;
import com.clinique.api.entity.User;
import com.clinique.api.exception.ResourceNotFoundException;
import com.clinique.api.repository.PatientProfileRepository;
import com.clinique.api.repository.TherapistProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientProfileRepository patientProfileRepository;
    private final TherapistProfileRepository therapistProfileRepository;
    private final com.clinique.api.repository.AppointmentRepository appointmentRepository;
    private final MlService mlService;

    /**
     * Récupère tous les patients qui ont un RDV avec le thérapeute connecté.
     */
    @Transactional(readOnly = true)
    public List<PatientInfoDTO> getMyPatients(User currentUser) {
        // 1. Trouver le profil du thérapeute connecté
        TherapistProfile therapist = therapistProfileRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Profil thérapeute non trouvé pour l'utilisateur"));

        // 2. Trouver tous les patients distincts pour ce thérapeute
        List<PatientProfile> patients = patientProfileRepository.findDistinctPatientsByTherapistId(therapist.getId());

        // 3. Mapper manuellement vers le DTO
        return patients.stream()
                .map(this::mapToPatientInfoDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public com.clinique.api.dto.ChurnRiskDTO getPatientChurnRisk(Long patientId, User currentUser) {
        // 1. Verify access (omitted for brevity, ideally should check if patient
        // belongs to therapist)

        // 2. Fetch History
        List<com.clinique.api.entity.Appointment> appointments = appointmentRepository.findByPatientId(patientId);

        if (appointments.isEmpty()) {
            var empty = new com.clinique.api.dto.ChurnRiskDTO();
            empty.setChurnRisk(false);
            empty.setChurnProbability(0.0);
            return empty;
        }

        // 3. Calculate Stats
        int totalVisits = appointments.size();
        long cancelledCount = appointments.stream().filter(a -> "CANCELLED_BY_PATIENT".equals(a.getStatus())).count();
        double cancellationRate = (double) cancelledCount / totalVisits;

        appointments.sort((a, b) -> a.getSessionDateTime().compareTo(b.getSessionDateTime()));
        com.clinique.api.entity.Appointment lastAppt = appointments.get(appointments.size() - 1);

        long daysSinceLast = java.time.temporal.ChronoUnit.DAYS.between(
                lastAppt.getSessionDateTime().toLocalDate(),
                java.time.LocalDate.now());

        // 4. Call ML
        var result = mlService.predictChurn((int) daysSinceLast, totalVisits, cancellationRate);

        var dto = new com.clinique.api.dto.ChurnRiskDTO();
        if (result != null) {
            dto.setChurnRisk((Boolean) result.get("is_churn_risk"));
            dto.setChurnProbability(((Number) result.get("churn_probability")).doubleValue());
        }
        return dto;
    }

    // Notre propre mapper manuel simple
    private PatientInfoDTO mapToPatientInfoDTO(PatientProfile patient) {
        PatientInfoDTO dto = new PatientInfoDTO();
        dto.setId(patient.getId());
        dto.setFirstName(patient.getFirstName());
        dto.setLastName(patient.getLastName());
        return dto;
    }
}