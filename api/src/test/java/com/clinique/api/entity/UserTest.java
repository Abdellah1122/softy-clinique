package com.clinique.api.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour l'entité User.
 * Ces tests vérifient l'implémentation de UserDetails et les comportements de
 * base.
 */
@DisplayName("User Entity - Tests de l'entité utilisateur")
class UserTest {

    /**
     * Vérifie que le builder crée correctement un utilisateur avec tous les champs.
     */
    @Test
    @DisplayName("Doit créer un utilisateur avec le builder")
    void shouldCreateUserWithBuilder() {
        // When
        User user = User.builder()
                .id(1L)
                .email("test@clinique.com")
                .password("hashedPassword")
                .role(Role.ROLE_PATIENT)
                .build();

        // Then
        assertNotNull(user);
        assertEquals(1L, user.getId());
        assertEquals("test@clinique.com", user.getEmail());
        assertEquals("hashedPassword", user.getPassword());
        assertEquals(Role.ROLE_PATIENT, user.getRole());
    }

    /**
     * Vérifie que la méthode getUsername() retourne bien l'email de l'utilisateur.
     * Dans cette implémentation, l'email sert d'identifiant unique.
     */
    @Test
    @DisplayName("Doit retourner l'email comme username")
    void shouldReturnEmailAsUsername() {
        // Given
        User user = User.builder()
                .email("patient@clinique.com")
                .password("pass")
                .role(Role.ROLE_PATIENT)
                .build();

        // When
        String username = user.getUsername();

        // Then
        assertEquals("patient@clinique.com", username);
    }

    /**
     * Vérifie que les autorités retournées pour un patient incluent "ROLE_PATIENT".
     */
    @Test
    @DisplayName("Doit retourner les authorities avec le rôle PATIENT")
    void shouldReturnAuthoritiesWithPatientRole() {
        // Given
        User user = User.builder()
                .email("patient@clinique.com")
                .password("pass")
                .role(Role.ROLE_PATIENT)
                .build();

        // When
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        // Then
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertTrue(authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PATIENT")));
    }

    /**
     * Vérifie que les autorités retournées pour un thérapeute incluent
     * "ROLE_THERAPIST".
     */
    @Test
    @DisplayName("Doit retourner les authorities avec le rôle THERAPIST")
    void shouldReturnAuthoritiesWithTherapistRole() {
        // Given
        User user = User.builder()
                .email("therapist@clinique.com")
                .password("pass")
                .role(Role.ROLE_THERAPIST)
                .build();

        // When
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        // Then
        assertEquals(1, authorities.size());
        assertTrue(authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_THERAPIST")));
    }

    /**
     * Vérifie que les autorités retournées pour un administrateur incluent
     * "ROLE_ADMIN".
     */
    @Test
    @DisplayName("Doit retourner les authorities avec le rôle ADMIN")
    void shouldReturnAuthoritiesWithAdminRole() {
        // Given
        User user = User.builder()
                .email("admin@clinique.com")
                .password("pass")
                .role(Role.ROLE_ADMIN)
                .build();

        // When
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        // Then
        assertEquals(1, authorities.size());
        assertTrue(authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    /**
     * Vérifie que le compte n'est pas expiré par défaut (toujours true dans
     * l'implémentation actuelle).
     */
    @Test
    @DisplayName("Le compte ne doit pas être expiré par défaut")
    void accountShouldNotBeExpiredByDefault() {
        // Given
        User user = User.builder()
                .email("test@clinique.com")
                .password("pass")
                .role(Role.ROLE_PATIENT)
                .build();

        // Then
        assertTrue(user.isAccountNonExpired());
    }

    /**
     * Vérifie que le compte n'est pas verrouillé par défaut (toujours true dans
     * l'implémentation actuelle).
     */
    @Test
    @DisplayName("Le compte ne doit pas être verrouillé par défaut")
    void accountShouldNotBeLockedByDefault() {
        // Given
        User user = User.builder()
                .email("test@clinique.com")
                .password("pass")
                .role(Role.ROLE_PATIENT)
                .build();

        // Then
        assertTrue(user.isAccountNonLocked());
    }

    /**
     * Vérifie que les identifiants ne sont pas expirés par défaut (toujours true
     * dans l'implémentation actuelle).
     */
    @Test
    @DisplayName("Les credentials ne doivent pas être expirées par défaut")
    void credentialsShouldNotBeExpiredByDefault() {
        // Given
        User user = User.builder()
                .email("test@clinique.com")
                .password("pass")
                .role(Role.ROLE_PATIENT)
                .build();

        // Then
        assertTrue(user.isCredentialsNonExpired());
    }

    /**
     * Vérifie que le compte est activé par défaut (toujours true dans
     * l'implémentation actuelle).
     */
    @Test
    @DisplayName("Le compte doit être activé par défaut")
    void accountShouldBeEnabledByDefault() {
        // Given
        User user = User.builder()
                .email("test@clinique.com")
                .password("pass")
                .role(Role.ROLE_PATIENT)
                .build();

        // Then
        assertTrue(user.isEnabled());
    }

    /**
     * Vérifie que le constructeur sans arguments crée une instance vide mais non
     * nulle.
     */
    @Test
    @DisplayName("Doit créer un utilisateur avec constructeur vide")
    void shouldCreateUserWithNoArgsConstructor() {
        // When
        User user = new User();

        // Then
        assertNotNull(user);
        assertNull(user.getId());
        assertNull(user.getEmail());
        assertNull(user.getPassword());
        assertNull(user.getRole());
    }

    /**
     * Vérifie que le constructeur avec tous les arguments initialise correctement
     * tous les champs.
     */
    @Test
    @DisplayName("Doit créer un utilisateur avec constructeur complet")
    void shouldCreateUserWithAllArgsConstructor() {
        // When
        User user = new User(1L, "test@clinique.com", "pass", Role.ROLE_PATIENT);

        // Then
        assertEquals(1L, user.getId());
        assertEquals("test@clinique.com", user.getEmail());
        assertEquals("pass", user.getPassword());
        assertEquals(Role.ROLE_PATIENT, user.getRole());
    }

    /**
     * Teste les méthodes getter et setter pour l'ID.
     */
    @Test
    @DisplayName("Doit définir et récupérer l'ID")
    void shouldSetAndGetId() {
        // Given
        User user = new User();

        // When
        user.setId(10L);

        // Then
        assertEquals(10L, user.getId());
    }

    /**
     * Teste les méthodes getter et setter pour l'email.
     */
    @Test
    @DisplayName("Doit définir et récupérer l'email")
    void shouldSetAndGetEmail() {
        // Given
        User user = new User();

        // When
        user.setEmail("nouveau@clinique.com");

        // Then
        assertEquals("nouveau@clinique.com", user.getEmail());
    }

    /**
     * Teste les méthodes getter et setter pour le mot de passe.
     */
    @Test
    @DisplayName("Doit définir et récupérer le mot de passe")
    void shouldSetAndGetPassword() {
        // Given
        User user = new User();

        // When
        user.setPassword("newHashedPassword");

        // Then
        assertEquals("newHashedPassword", user.getPassword());
    }

    /**
     * Teste les méthodes getter et setter pour le rôle.
     */
    @Test
    @DisplayName("Doit définir et récupérer le rôle")
    void shouldSetAndGetRole() {
        // Given
        User user = new User();

        // When
        user.setRole(Role.ROLE_THERAPIST);

        // Then
        assertEquals(Role.ROLE_THERAPIST, user.getRole());
    }

    /**
     * Vérifie l'implémentation de la méthode equals() : deux objets avec les mêmes
     * données doivent être égaux.
     */
    @Test
    @DisplayName("Deux utilisateurs identiques doivent être égaux")
    void twoIdenticalUsersShouldBeEqual() {
        // Given
        User user1 = User.builder()
                .id(1L)
                .email("test@clinique.com")
                .password("pass")
                .role(Role.ROLE_PATIENT)
                .build();

        User user2 = User.builder()
                .id(1L)
                .email("test@clinique.com")
                .password("pass")
                .role(Role.ROLE_PATIENT)
                .build();

        // Then
        assertEquals(user1, user2, "Deux utilisateurs avec les mêmes données doivent être égaux");
        assertEquals(user1.hashCode(), user2.hashCode());
    }

    /**
     * Vérifie l'implémentation de la méthode equals() : deux objets avec des
     * données différentes ne doivent pas être égaux.
     */
    @Test
    @DisplayName("Deux utilisateurs différents ne doivent pas être égaux")
    void twoDifferentUsersShouldNotBeEqual() {
        // Given
        User user1 = User.builder()
                .id(1L)
                .email("test1@clinique.com")
                .password("pass")
                .role(Role.ROLE_PATIENT)
                .build();

        User user2 = User.builder()
                .id(2L)
                .email("test2@clinique.com")
                .password("pass")
                .role(Role.ROLE_THERAPIST)
                .build();

        // Then
        assertNotEquals(user1, user2);
    }

    /**
     * Vérifie que la méthode toString() génère une chaîne contenant les
     * informations essentielles de l'utilisateur.
     */
    @Test
    @DisplayName("toString doit contenir les informations de l'utilisateur")
    void toStringShouldContainUserInfo() {
        // Given
        User user = User.builder()
                .id(1L)
                .email("test@clinique.com")
                .password("pass")
                .role(Role.ROLE_PATIENT)
                .build();

        // When
        String userString = user.toString();

        // Then
        assertTrue(userString.contains("test@clinique.com"));
        assertTrue(userString.contains("ROLE_PATIENT"));
    }

    /**
     * Vérifie que l'entité gère correctement les emails contenant des caractères
     * spéciaux autorisés (comme '+').
     */
    @Test
    @DisplayName("Doit gérer les emails avec caractères spéciaux")
    void shouldHandleEmailsWithSpecialCharacters() {
        // Given
        User user = User.builder()
                .email("test.user+tag@clinique-test.com")
                .password("pass")
                .role(Role.ROLE_PATIENT)
                .build();

        // Then
        assertEquals("test.user+tag@clinique-test.com", user.getUsername());
    }

    /**
     * Vérifie que l'entité peut être configurée avec n'importe laquelle des valeurs
     * de l'énumération Role.
     */
    @Test
    @DisplayName("Doit créer des utilisateurs pour tous les rôles")
    void shouldCreateUsersForAllRoles() {
        // Given & When
        User patient = User.builder().role(Role.ROLE_PATIENT).email("p@c.com").password("p").build();
        User therapist = User.builder().role(Role.ROLE_THERAPIST).email("t@c.com").password("p").build();
        User admin = User.builder().role(Role.ROLE_ADMIN).email("a@c.com").password("p").build();

        // Then
        assertEquals("ROLE_PATIENT", patient.getAuthorities().iterator().next().getAuthority());
        assertEquals("ROLE_THERAPIST", therapist.getAuthorities().iterator().next().getAuthority());
        assertEquals("ROLE_ADMIN", admin.getAuthorities().iterator().next().getAuthority());
    }
}