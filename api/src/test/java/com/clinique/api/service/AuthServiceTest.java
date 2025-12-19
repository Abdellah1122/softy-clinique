package com.clinique.api.service;

import com.clinique.api.dto.AuthRequest;
import com.clinique.api.dto.AuthResponse;
import com.clinique.api.dto.RegisterRequest;
import com.clinique.api.entity.*;
import com.clinique.api.repository.PatientProfileRepository;
import com.clinique.api.repository.TherapistProfileRepository;
import com.clinique.api.repository.UserRepository;
import com.clinique.api.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour AuthService.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService - Tests du service d'authentification")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PatientProfileRepository patientProfileRepository;

    @Mock
    private TherapistProfileRepository therapistProfileRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private PatientProfile patientProfile;
    private TherapistProfile therapistProfile;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@clinique.com")
                .password("hashedPassword")
                .role(Role.ROLE_PATIENT)
                .build();

        patientProfile = new PatientProfile();
        patientProfile.setId(1L);
        patientProfile.setUser(testUser);
        patientProfile.setFirstName("Ahmed");
        patientProfile.setLastName("Bennani");

        therapistProfile = new TherapistProfile();
        therapistProfile.setId(1L);
        therapistProfile.setFirstName("Dr. Fatima");
        therapistProfile.setLastName("Zahra");
        therapistProfile.setSpecialty("Kinésithérapeute");
    }

    @Test
    @DisplayName("Doit enregistrer un nouveau patient avec succès")
    void shouldRegisterNewPatientSuccessfully() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setEmail("patient@test.com");
        request.setPassword("password123");
        request.setFirstName("Mohammed");
        request.setLastName("Alami");
        request.setRole(Role.ROLE_PATIENT);

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(patientProfileRepository.save(any(PatientProfile.class))).thenReturn(patientProfile);
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt.token.here");

        // When
        AuthResponse response = authService.register(request);

        // Then
        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals("jwt.token.here", response.getToken());
        assertNotNull(response.getUser());
        assertEquals(testUser.getId(), response.getUser().getId());
        assertEquals(1L, response.getUser().getProfileId());
        verify(userRepository).save(any(User.class));
        verify(patientProfileRepository).save(any(PatientProfile.class));
    }

    @Test
    @DisplayName("Doit enregistrer un nouveau thérapeute avec succès")
    void shouldRegisterNewTherapistSuccessfully() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setEmail("therapist@test.com");
        request.setPassword("password123");
        request.setFirstName("Dr. Fatima");
        request.setLastName("Zahra");
        request.setRole(Role.ROLE_THERAPIST);
        request.setSpecialty("Kinésithérapeute");

        User therapistUser = User.builder()
                .id(2L)
                .email("therapist@test.com")
                .role(Role.ROLE_THERAPIST)
                .build();

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(therapistUser);
        when(therapistProfileRepository.save(any(TherapistProfile.class))).thenReturn(therapistProfile);
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt.token.here");

        // When
        AuthResponse response = authService.register(request);

        // Then
        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals(1L, response.getUser().getProfileId());
        verify(therapistProfileRepository).save(any(TherapistProfile.class));
    }

    @Test
    @DisplayName("Doit lancer une exception si l'email existe déjà")
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@test.com");
        request.setPassword("password123");
        request.setRole(Role.ROLE_PATIENT);

        when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            authService.register(request);
        });
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Doit lancer une exception pour un rôle non valide")
    void shouldThrowExceptionForInvalidRole() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setEmail("user@test.com");
        request.setPassword("password123");
        request.setRole(Role.ROLE_ADMIN); // Admin role not allowed for registration

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            authService.register(request);
        });
    }

    @Test
    @DisplayName("Doit authentifier un utilisateur avec succès")
    void shouldLoginUserSuccessfully() {
        // Given
        AuthRequest request = new AuthRequest();
        request.setEmail("test@clinique.com");
        request.setPassword("password123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByEmail("test@clinique.com")).thenReturn(Optional.of(testUser));
        when(patientProfileRepository.findByUserId(1L)).thenReturn(Optional.of(patientProfile));
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt.token.here");

        // When
        AuthResponse response = authService.login(request);

        // Then
        assertNotNull(response);
        assertEquals("jwt.token.here", response.getToken());
        assertNotNull(response.getUser());
        assertEquals(testUser.getEmail(), response.getUser().getEmail());
        assertEquals(1L, response.getUser().getProfileId());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("Doit lancer une exception si l'utilisateur n'existe pas lors du login")
    void shouldThrowExceptionWhenUserNotFoundDuringLogin() {
        // Given
        AuthRequest request = new AuthRequest();
        request.setEmail("nonexistent@test.com");
        request.setPassword("password123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByEmail("nonexistent@test.com")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UsernameNotFoundException.class, () -> {
            authService.login(request);
        });
    }

    @Test
    @DisplayName("Doit authentifier un thérapeute et récupérer son profileId")
    void shouldLoginTherapistAndGetProfileId() {
        // Given
        AuthRequest request = new AuthRequest();
        request.setEmail("therapist@test.com");
        request.setPassword("password123");

        User therapistUser = User.builder()
                .id(2L)
                .email("therapist@test.com")
                .role(Role.ROLE_THERAPIST)
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByEmail("therapist@test.com")).thenReturn(Optional.of(therapistUser));
        when(therapistProfileRepository.findByUserId(2L)).thenReturn(Optional.of(therapistProfile));
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt.token.here");

        // When
        AuthResponse response = authService.login(request);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getUser().getProfileId());
        verify(therapistProfileRepository).findByUserId(2L);
    }

    @Test
    @DisplayName("Doit retourner null comme profileId pour un admin")
    void shouldReturnNullProfileIdForAdmin() {
        // Given
        AuthRequest request = new AuthRequest();
        request.setEmail("admin@test.com");
        request.setPassword("password123");

        User adminUser = User.builder()
                .id(3L)
                .email("admin@test.com")
                .role(Role.ROLE_ADMIN)
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(adminUser));
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt.token.here");

        // When
        AuthResponse response = authService.login(request);

        // Then
        assertNotNull(response);
        assertNull(response.getUser().getProfileId());
    }

    @Test
    @DisplayName("Doit encoder le mot de passe lors de l'enregistrement")
    void shouldEncodePasswordDuringRegistration() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@test.com");
        request.setPassword("plainPassword");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setRole(Role.ROLE_PATIENT);

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode("plainPassword")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(patientProfileRepository.save(any(PatientProfile.class))).thenReturn(patientProfile);
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt.token");

        // When
        authService.register(request);

        // Then
        verify(passwordEncoder).encode("plainPassword");
    }

    @Test
    @DisplayName("Doit créer un profil patient avec les bonnes informations")
    void shouldCreatePatientProfileWithCorrectInfo() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setEmail("patient@test.com");
        request.setPassword("password");
        request.setFirstName("Youssef");
        request.setLastName("Idrissi");
        request.setRole(Role.ROLE_PATIENT);

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(patientProfileRepository.save(any(PatientProfile.class))).thenReturn(patientProfile);
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt.token");

        // When
        authService.register(request);

        // Then
        verify(patientProfileRepository).save(argThat(profile ->
                profile.getFirstName().equals("Youssef") &&
                        profile.getLastName().equals("Idrissi") &&
                        profile.getUser().equals(testUser)
        ));
    }

    @Test
    @DisplayName("Doit créer un profil thérapeute avec la spécialité")
    void shouldCreateTherapistProfileWithSpecialty() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setEmail("therapist@test.com");
        request.setPassword("password");
        request.setFirstName("Dr. Ali");
        request.setLastName("Mansouri");
        request.setRole(Role.ROLE_THERAPIST);
        request.setSpecialty("Psychologue");

        User therapistUser = User.builder()
                .id(2L)
                .email("therapist@test.com")
                .role(Role.ROLE_THERAPIST)
                .build();

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(therapistUser);
        when(therapistProfileRepository.save(any(TherapistProfile.class))).thenReturn(therapistProfile);
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt.token");

        // When
        authService.register(request);

        // Then
        verify(therapistProfileRepository).save(argThat(profile ->
                profile.getFirstName().equals("Dr. Ali") &&
                        profile.getLastName().equals("Mansouri") &&
                        profile.getSpecialty().equals("Psychologue")
        ));
    }
}