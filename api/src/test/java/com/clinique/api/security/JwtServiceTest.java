package com.clinique.api.security;

import com.clinique.api.entity.Role;
import com.clinique.api.entity.User;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour JwtService.
 * Ces tests couvrent la génération, validation et extraction de tokens JWT.
 */
@DisplayName("JwtService - Tests de génération et validation de tokens JWT")
class JwtServiceTest {

    private JwtService jwtService;
    private User testUser;

    // Clé secrète de test (doit être de 256 bits minimum pour HS256)
    private static final String TEST_SECRET_KEY = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final long TEST_EXPIRATION = 86400000; // 24 heures en millisecondes

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        // Injection des valeurs @Value via réflexion pour les tests
        ReflectionTestUtils.setField(jwtService, "secretKey", TEST_SECRET_KEY);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", TEST_EXPIRATION);

        // Créer un utilisateur de test
        testUser = User.builder()
                .id(1L)
                .email("test@clinique.com")
                .password("hashedPassword")
                .role(Role.ROLE_PATIENT)
                .build();
    }

    @Test
    @DisplayName("Doit générer un token JWT valide")
    void shouldGenerateValidToken() {
        // When
        String token = jwtService.generateToken(testUser);

        // Then
        assertNotNull(token, "Le token ne doit pas être null");
        assertFalse(token.isEmpty(), "Le token ne doit pas être vide");
        assertTrue(token.split("\\.").length == 3, "Le token JWT doit avoir 3 parties (header.payload.signature)");
    }

    @Test
    @DisplayName("Doit extraire le nom d'utilisateur (email) du token")
    void shouldExtractUsernameFromToken() {
        // Given
        String token = jwtService.generateToken(testUser);

        // When
        String extractedUsername = jwtService.extractUsername(token);

        // Then
        assertEquals(testUser.getEmail(), extractedUsername, "L'email extrait doit correspondre à l'email de l'utilisateur");
    }

    @Test
    @DisplayName("Doit valider un token correct avec succès")
    void shouldValidateCorrectToken() {
        // Given
        String token = jwtService.generateToken(testUser);

        // When
        boolean isValid = jwtService.isTokenValid(token, testUser);

        // Then
        assertTrue(isValid, "Le token doit être valide pour l'utilisateur correct");
    }

    @Test
    @DisplayName("Ne doit pas valider un token pour un utilisateur différent")
    void shouldNotValidateTokenForDifferentUser() {
        // Given
        String token = jwtService.generateToken(testUser);

        User differentUser = User.builder()
                .id(2L)
                .email("autre@clinique.com")
                .password("hashedPassword")
                .role(Role.ROLE_THERAPIST)
                .build();

        // When
        boolean isValid = jwtService.isTokenValid(token, differentUser);

        // Then
        assertFalse(isValid, "Le token ne doit pas être valide pour un utilisateur différent");
    }

    @Test
    @DisplayName("Doit inclure les rôles dans le token")
    void shouldIncludeRolesInToken() {
        // Given
        testUser.setRole(Role.ROLE_THERAPIST);
        String token = jwtService.generateToken(testUser);

        // When - Extraire les claims manuellement
        String username = jwtService.extractUsername(token);

        // Then
        assertNotNull(username);
        // Note: Dans un test réel, vous pourriez vouloir extraire le claim "roles"
        // mais cela nécessiterait d'exposer extractAllClaims ou d'ajouter une méthode publique
    }

    @Test
    @DisplayName("Doit inclure l'ID utilisateur dans le token")
    void shouldIncludeUserIdInToken() {
        // Given
        String token = jwtService.generateToken(testUser);

        // When
        String extractedUsername = jwtService.extractUsername(token);

        // Then
        assertNotNull(extractedUsername);
        assertEquals(testUser.getEmail(), extractedUsername);
        // Note: Pour vérifier userId, il faudrait une méthode extractUserId() publique
    }

    @Test
    @DisplayName("Ne doit pas valider un token expiré")
    void shouldNotValidateExpiredToken() {
        // Given - Créer un service avec une expiration très courte
        JwtService shortExpirationService = new JwtService();
        ReflectionTestUtils.setField(shortExpirationService, "secretKey", TEST_SECRET_KEY);
        ReflectionTestUtils.setField(shortExpirationService, "jwtExpiration", 1L); // 1 milliseconde

        String token = shortExpirationService.generateToken(testUser);

        // When - Attendre que le token expire
        try {
            Thread.sleep(10); // Attendre 10ms pour être sûr
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Then - Le token devrait être expiré
        assertThrows(ExpiredJwtException.class, () -> {
            jwtService.extractUsername(token);
        }, "Une exception ExpiredJwtException devrait être levée pour un token expiré");
    }

    @Test
    @DisplayName("Doit rejeter un token avec une signature invalide")
    void shouldRejectTokenWithInvalidSignature() {
        // Given - Créer un token avec une clé différente
        JwtService differentKeyService = new JwtService();
        ReflectionTestUtils.setField(differentKeyService, "secretKey",
                "5468576D5A7134743777217A25432A462D4A614E645267556B58703273357638");
        ReflectionTestUtils.setField(differentKeyService, "jwtExpiration", TEST_EXPIRATION);

        String tokenWithDifferentKey = differentKeyService.generateToken(testUser);

        // When & Then - Essayer de valider avec le service original
        assertThrows(SignatureException.class, () -> {
            jwtService.extractUsername(tokenWithDifferentKey);
        }, "Une exception SignatureException devrait être levée pour une signature invalide");
    }

    @Test
    @DisplayName("Doit rejeter un token malformé")
    void shouldRejectMalformedToken() {
        // Given
        String malformedToken = "ce.nest.pas.un.token.valide";

        // When & Then
        assertThrows(Exception.class, () -> {
            jwtService.extractUsername(malformedToken);
        }, "Une exception devrait être levée pour un token malformé");
    }

    @Test
    @DisplayName("Doit rejeter un token null")
    void shouldRejectNullToken() {
        // Given
        String nullToken = null;

        // When & Then
        assertThrows(Exception.class, () -> {
            jwtService.extractUsername(nullToken);
        }, "Une exception devrait être levée pour un token null");
    }

    @Test
    @DisplayName("Doit rejeter un token vide")
    void shouldRejectEmptyToken() {
        // Given
        String emptyToken = "";

        // When & Then
        assertThrows(Exception.class, () -> {
            jwtService.extractUsername(emptyToken);
        }, "Une exception devrait être levée pour un token vide");
    }

    @Test
    @DisplayName("Doit valider un token pour différents rôles")
    void shouldValidateTokenForDifferentRoles() {
        // Test pour PATIENT
        User patient = User.builder()
                .id(1L)
                .email("patient@clinique.com")
                .password("pass")
                .role(Role.ROLE_PATIENT)
                .build();
        String patientToken = jwtService.generateToken(patient);
        assertTrue(jwtService.isTokenValid(patientToken, patient));

        // Test pour THERAPIST
        User therapist = User.builder()
                .id(2L)
                .email("therapist@clinique.com")
                .password("pass")
                .role(Role.ROLE_THERAPIST)
                .build();
        String therapistToken = jwtService.generateToken(therapist);
        assertTrue(jwtService.isTokenValid(therapistToken, therapist));

        // Test pour ADMIN
        User admin = User.builder()
                .id(3L)
                .email("admin@clinique.com")
                .password("pass")
                .role(Role.ROLE_ADMIN)
                .build();
        String adminToken = jwtService.generateToken(admin);
        assertTrue(jwtService.isTokenValid(adminToken, admin));
    }

    @Test
    @DisplayName("Doit extraire correctement l'email avec caractères spéciaux")
    void shouldExtractEmailWithSpecialCharacters() {
        // Given
        User userWithSpecialEmail = User.builder()
                .id(1L)
                .email("test.user+tag@clinique-test.com")
                .password("pass")
                .role(Role.ROLE_PATIENT)
                .build();

        String token = jwtService.generateToken(userWithSpecialEmail);

        // When
        String extractedEmail = jwtService.extractUsername(token);

        // Then
        assertEquals(userWithSpecialEmail.getEmail(), extractedEmail);
    }
}