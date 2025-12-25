package com.clinique.api.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour l'entité PatientProfile.
 */
@DisplayName("PatientProfile Entity - Tests du profil patient")
class PatientProfileTest {

    /**
     * Vérifie que le constructeur par défaut crée un profil vide avec toutes les
     * propriétés à null.
     */
    @Test
    @DisplayName("Doit créer un profil patient vide")
    void shouldCreateEmptyPatientProfile() {
        // When
        PatientProfile profile = new PatientProfile();

        // Then
        assertNotNull(profile);
        assertNull(profile.getId());
        assertNull(profile.getUser());
        assertNull(profile.getFirstName());
        assertNull(profile.getLastName());
        assertNull(profile.getDateOfBirth());
        assertNull(profile.getPhoneNumber());
    }

    /**
     * Teste les méthodes getter et setter pour l'ID du profil.
     */
    @Test
    @DisplayName("Doit définir et récupérer l'ID")
    void shouldSetAndGetId() {
        // Given
        PatientProfile profile = new PatientProfile();

        // When
        profile.setId(1L);

        // Then
        assertEquals(1L, profile.getId());
    }

    /**
     * Teste les méthodes getter et setter pour le prénom du patient.
     */
    @Test
    @DisplayName("Doit définir et récupérer le prénom")
    void shouldSetAndGetFirstName() {
        // Given
        PatientProfile profile = new PatientProfile();

        // When
        profile.setFirstName("Mohammed");

        // Then
        assertEquals("Mohammed", profile.getFirstName());
    }

    /**
     * Teste les méthodes getter et setter pour le nom de famille du patient.
     */
    @Test
    @DisplayName("Doit définir et récupérer le nom de famille")
    void shouldSetAndGetLastName() {
        // Given
        PatientProfile profile = new PatientProfile();

        // When
        profile.setLastName("Alami");

        // Then
        assertEquals("Alami", profile.getLastName());
    }

    /**
     * Teste les méthodes getter et setter pour la date de naissance.
     */
    @Test
    @DisplayName("Doit définir et récupérer la date de naissance")
    void shouldSetAndGetDateOfBirth() {
        // Given
        PatientProfile profile = new PatientProfile();
        LocalDate birthDate = LocalDate.of(1990, 5, 15);

        // When
        profile.setDateOfBirth(birthDate);

        // Then
        assertEquals(birthDate, profile.getDateOfBirth());
    }

    /**
     * Teste les méthodes getter et setter pour le numéro de téléphone.
     */
    @Test
    @DisplayName("Doit définir et récupérer le numéro de téléphone")
    void shouldSetAndGetPhoneNumber() {
        // Given
        PatientProfile profile = new PatientProfile();

        // When
        profile.setPhoneNumber("+212612345678");

        // Then
        assertEquals("+212612345678", profile.getPhoneNumber());
    }

    /**
     * Vérifie l'association entre le profil patient et l'entité User.
     */
    @Test
    @DisplayName("Doit lier un utilisateur au profil")
    void shouldLinkUserToProfile() {
        // Given
        PatientProfile profile = new PatientProfile();
        User user = User.builder()
                .id(1L)
                .email("patient@clinique.com")
                .password("pass")
                .role(Role.ROLE_PATIENT)
                .build();

        // When
        profile.setUser(user);

        // Then
        assertNotNull(profile.getUser());
        assertEquals(user, profile.getUser());
        assertEquals("patient@clinique.com", profile.getUser().getEmail());
    }

    /**
     * Vérifie que le profil peut gérer une liste de rendez-vous.
     */
    @Test
    @DisplayName("Doit gérer une liste de rendez-vous")
    void shouldHandleAppointmentsList() {
        // Given
        PatientProfile profile = new PatientProfile();
        List<Appointment> appointments = new ArrayList<>();

        // When
        profile.setAppointments(appointments);

        // Then
        assertNotNull(profile.getAppointments());
        assertTrue(profile.getAppointments().isEmpty());
    }

    /**
     * Vérifie la création d'un profil complet avec toutes les informations
     * renseignées.
     */
    @Test
    @DisplayName("Doit créer un profil patient complet")
    void shouldCreateCompletePatientProfile() {
        // Given
        User user = User.builder()
                .id(1L)
                .email("patient@clinique.com")
                .password("hashedPass")
                .role(Role.ROLE_PATIENT)
                .build();

        PatientProfile profile = new PatientProfile();
        profile.setId(1L);
        profile.setUser(user);
        profile.setFirstName("Fatima");
        profile.setLastName("Zahra");
        profile.setDateOfBirth(LocalDate.of(1995, 3, 20));
        profile.setPhoneNumber("+212698765432");

        // Then
        assertEquals(1L, profile.getId());
        assertEquals("Fatima", profile.getFirstName());
        assertEquals("Zahra", profile.getLastName());
        assertEquals(LocalDate.of(1995, 3, 20), profile.getDateOfBirth());
        assertEquals("+212698765432", profile.getPhoneNumber());
        assertEquals(user, profile.getUser());
    }

    /**
     * Vérifie que l'entité accepte les noms contenant des caractères spéciaux
     * (tirets, apostrophes).
     */
    @Test
    @DisplayName("Doit gérer les noms avec des caractères spéciaux")
    void shouldHandleNamesWithSpecialCharacters() {
        // Given
        PatientProfile profile = new PatientProfile();

        // When
        profile.setFirstName("José-María");
        profile.setLastName("O'Brien");

        // Then
        assertEquals("José-María", profile.getFirstName());
        assertEquals("O'Brien", profile.getLastName());
    }

    /**
     * Vérifie que l'entité accepte les dates de naissance passées.
     */
    @Test
    @DisplayName("Doit accepter une date de naissance dans le passé")
    void shouldAcceptPastDateOfBirth() {
        // Given
        PatientProfile profile = new PatientProfile();
        LocalDate pastDate = LocalDate.of(1980, 1, 1);

        // When
        profile.setDateOfBirth(pastDate);

        // Then
        assertEquals(pastDate, profile.getDateOfBirth());
        assertTrue(profile.getDateOfBirth().isBefore(LocalDate.now()));
    }

    /**
     * Vérifie que l'entité peut stocker différents formats de numéros de téléphone.
     */
    @Test
    @DisplayName("Doit gérer différents formats de numéros de téléphone")
    void shouldHandleDifferentPhoneNumberFormats() {
        // Given
        PatientProfile profile1 = new PatientProfile();
        PatientProfile profile2 = new PatientProfile();
        PatientProfile profile3 = new PatientProfile();

        // When
        profile1.setPhoneNumber("0612345678");
        profile2.setPhoneNumber("+212612345678");
        profile3.setPhoneNumber("+33612345678");

        // Then
        assertEquals("0612345678", profile1.getPhoneNumber());
        assertEquals("+212612345678", profile2.getPhoneNumber());
        assertEquals("+33612345678", profile3.getPhoneNumber());
    }

    /**
     * Vérifie que le numéro de téléphone est optionnel (peut être null).
     */
    @Test
    @DisplayName("Doit permettre un numéro de téléphone null")
    void shouldAllowNullPhoneNumber() {
        // Given
        PatientProfile profile = new PatientProfile();

        // When
        profile.setPhoneNumber(null);

        // Then
        assertNull(profile.getPhoneNumber());
    }

    /**
     * Vérifie que la date de naissance est optionnelle (peut être null).
     */
    @Test
    @DisplayName("Doit permettre une date de naissance null")
    void shouldAllowNullDateOfBirth() {
        // Given
        PatientProfile profile = new PatientProfile();

        // When
        profile.setDateOfBirth(null);

        // Then
        assertNull(profile.getDateOfBirth());
    }

    /**
     * Vérifie l'égalité entre deux profils ayant les mêmes données (basé sur
     * Lombok @Data).
     */
    @Test
    @DisplayName("Deux profils avec le même ID doivent être égaux")
    void twoProfilesWithSameDataShouldBeEqual() {
        // Given
        User user = User.builder()
                .id(1L)
                .email("test@clinique.com")
                .password("pass")
                .role(Role.ROLE_PATIENT)
                .build();

        PatientProfile profile1 = new PatientProfile();
        profile1.setId(1L);
        profile1.setUser(user);
        profile1.setFirstName("Ahmed");
        profile1.setLastName("Bennani");

        PatientProfile profile2 = new PatientProfile();
        profile2.setId(1L);
        profile2.setUser(user);
        profile2.setFirstName("Ahmed");
        profile2.setLastName("Bennani");

        // Then
        assertEquals(profile1, profile2);
    }

    /**
     * Vérifie que la méthode toString() contient les informations lisibles du
     * profil.
     */
    @Test
    @DisplayName("toString doit contenir les informations du profil")
    void toStringShouldContainProfileInfo() {
        // Given
        PatientProfile profile = new PatientProfile();
        profile.setId(1L);
        profile.setFirstName("Youssef");
        profile.setLastName("Idrissi");

        // When
        String profileString = profile.toString();

        // Then
        assertTrue(profileString.contains("Youssef"));
        assertTrue(profileString.contains("Idrissi"));

    }
}