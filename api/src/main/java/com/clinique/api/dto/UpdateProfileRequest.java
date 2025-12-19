package com.clinique.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import lombok.Data;
import java.time.LocalDate;

@Data
public class UpdateProfileRequest {

    @NotBlank(message = "First name cannot be blank")
    private String firstName;

    @NotBlank(message = "Last name cannot be blank")
    private String lastName;

    // Patient-specific
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;
    private String phoneNumber;

    // Therapist-specific
    private String specialty;
    private String credentials;
}