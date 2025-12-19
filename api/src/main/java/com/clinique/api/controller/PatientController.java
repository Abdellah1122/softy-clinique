package com.clinique.api.controller;

import com.clinique.api.dto.PatientInfoDTO;
import com.clinique.api.entity.User;
import com.clinique.api.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    /**
     * Endpoint pour récupérer la liste de tous les patients
     * associés au thérapeute actuellement connecté.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_THERAPIST')")
    public ResponseEntity<List<PatientInfoDTO>> getMyPatients(
            @AuthenticationPrincipal User currentUser) {
        List<PatientInfoDTO> patients = patientService.getMyPatients(currentUser);
        return ResponseEntity.ok(patients);
    }

    @GetMapping("/{id}/churn-risk")
    @PreAuthorize("hasAuthority('ROLE_THERAPIST')")
    public ResponseEntity<com.clinique.api.dto.ChurnRiskDTO> getChurnRisk(
            @org.springframework.web.bind.annotation.PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(patientService.getPatientChurnRisk(id, currentUser));
    }
}