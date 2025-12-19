package com.clinique.api.service;

import com.clinique.api.dto.TherapistInfoDTO;
import com.clinique.api.entity.TherapistProfile;
import com.clinique.api.repository.TherapistProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TherapistService {

    private final TherapistProfileRepository therapistProfileRepository;

    @Transactional(readOnly = true)
    public List<TherapistInfoDTO> getAllTherapists() {
        return therapistProfileRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private TherapistInfoDTO mapToDTO(TherapistProfile profile) {
        TherapistInfoDTO dto = new TherapistInfoDTO();
        dto.setId(profile.getUser().getId()); // Use User ID for consistency with Auth
        dto.setFirstName(profile.getFirstName());
        dto.setLastName(profile.getLastName());
        dto.setSpecialty(profile.getSpecialty());
        return dto;
    }
}
