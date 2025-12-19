package com.clinique.api.controller;

import com.clinique.api.dto.AuthRequest;
import com.clinique.api.dto.AuthResponse;
import com.clinique.api.dto.RegisterRequest;
import com.clinique.api.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contrôleur gérant les endpoints d'authentification (publics).
 */
@RestController // Combine @Controller et @ResponseBody (renvoie du JSON)
@RequestMapping("/api/v1/auth") // Préfixe toutes les routes de ce contrôleur
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Endpoint pour l'inscription.
     * @Valid : Déclenche la validation des annotations (ex: @NotBlank)
     * sur l'objet RegisterRequest.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        AuthResponse response = authService.register(request);
        // Renvoie un statut 201 (Created) avec le token et l'user
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Endpoint pour la connexion.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody AuthRequest request
    ) {
        // Renvoie un statut 200 (OK) avec le token et l'user
        return ResponseEntity.ok(authService.login(request));
    }
}