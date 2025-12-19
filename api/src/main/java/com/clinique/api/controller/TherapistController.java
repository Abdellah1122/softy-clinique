package com.clinique.api.controller;

import com.clinique.api.dto.TherapistInfoDTO;
import com.clinique.api.service.TherapistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/therapists")
@RequiredArgsConstructor
public class TherapistController {

    private final TherapistService therapistService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TherapistInfoDTO>> getAllTherapists() {
        return ResponseEntity.ok(therapistService.getAllTherapists());
    }
}
