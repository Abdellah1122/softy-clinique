package com.clinique.api.controller;

import com.clinique.api.dto.ProfileDTO;
import com.clinique.api.dto.UpdateProfileRequest;
import com.clinique.api.entity.User;
import com.clinique.api.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    /**
     * Gets the profile of the currently logged-in user.
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()") // Any authenticated user can get their own profile
    public ResponseEntity<ProfileDTO> getMyProfile(
            @AuthenticationPrincipal User currentUser
    ) {
        ProfileDTO profile = profileService.getMyProfile(currentUser);
        return ResponseEntity.ok(profile);
    }

    /**
     * Updates the profile of the currently logged-in user.
     */
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProfileDTO> updateMyProfile(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        ProfileDTO updatedProfile = profileService.updateMyProfile(currentUser, request);
        return ResponseEntity.ok(updatedProfile);
    }
}