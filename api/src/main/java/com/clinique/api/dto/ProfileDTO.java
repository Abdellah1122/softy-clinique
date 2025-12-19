package com.clinique.api.dto;

import com.clinique.api.entity.Role;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ProfileDTO {
    // Common fields
    private Long id; // The Profile ID (not the User ID)
    private Long userId;
    private String email;
    private Role role;
    private String firstName;
    private String lastName;

    // Patient-specific fields
    private LocalDate dateOfBirth;
    private String phoneNumber;

    // Therapist-specific fields
    private String specialty;
    private String credentials;
}