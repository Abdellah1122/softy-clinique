package com.clinique.api.security;

import com.clinique.api.entity.Role;
import com.clinique.api.entity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour JwtAuthenticationFilter.
 * Ces tests vérifient que le filtre valide correctement les tokens JWT.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter - Tests de validation des tokens")
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private User testUser;
    private String validToken;

    @BeforeEach
    void setUp() {
        // Nettoyer le contexte de sécurité avant chaque test
        SecurityContextHolder.clearContext();

        // Créer un utilisateur de test
        testUser = User.builder()
                .id(1L)
                .email("test@clinique.com")
                .password("hashedPassword")
                .role(Role.ROLE_PATIENT)
                .build();

        validToken = "valid.jwt.token";
    }

    @Test
    @DisplayName("Doit authentifier l'utilisateur avec un token valide")
    void shouldAuthenticateUserWithValidToken() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtService.extractUsername(validToken)).thenReturn(testUser.getEmail());
        when(userDetailsService.loadUserByUsername(testUser.getEmail())).thenReturn(testUser);
        when(jwtService.isTokenValid(validToken, testUser)).thenReturn(true);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication(),
                "L'authentification doit être définie dans le contexte");
        assertEquals(testUser.getEmail(),
                SecurityContextHolder.getContext().getAuthentication().getName(),
                "L'utilisateur authentifié doit correspondre");
    }

    @Test
    @DisplayName("Doit passer au filtre suivant si aucun header Authorization")
    void shouldContinueFilterChainWhenNoAuthorizationHeader() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn(null);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).extractUsername(anyString());
        assertNull(SecurityContextHolder.getContext().getAuthentication(),
                "Aucune authentification ne doit être définie");
    }

    @Test
    @DisplayName("Doit passer au filtre suivant si le header ne commence pas par 'Bearer '")
    void shouldContinueFilterChainWhenHeaderDoesNotStartWithBearer() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Basic sometoken");

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).extractUsername(anyString());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("Ne doit pas authentifier si le token est invalide")
    void shouldNotAuthenticateWhenTokenIsInvalid() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtService.extractUsername(validToken)).thenReturn(testUser.getEmail());
        when(userDetailsService.loadUserByUsername(testUser.getEmail())).thenReturn(testUser);
        when(jwtService.isTokenValid(validToken, testUser)).thenReturn(false);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication(),
                "L'authentification ne doit pas être définie pour un token invalide");
    }

    @Test
    @DisplayName("Doit retourner 401 si le token est malformé")
    void shouldReturn401WhenTokenIsMalformed() throws ServletException, IOException {
        // Given
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        when(request.getHeader("Authorization")).thenReturn("Bearer malformed.token");
        when(jwtService.extractUsername("malformed.token"))
                .thenThrow(new RuntimeException("Token malformé"));
        when(response.getWriter()).thenReturn(printWriter);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType("application/json");
        verify(filterChain, never()).doFilter(request, response);
        assertTrue(stringWriter.toString().contains("Token JWT Invalide ou Expiré"));
    }

    @Test
    @DisplayName("Doit retourner 401 si l'utilisateur n'existe pas")
    void shouldReturn401WhenUserDoesNotExist() throws ServletException, IOException {
        // Given
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtService.extractUsername(validToken)).thenReturn("nonexistent@clinique.com");
        when(userDetailsService.loadUserByUsername("nonexistent@clinique.com"))
                .thenThrow(new RuntimeException("Utilisateur introuvable"));
        when(response.getWriter()).thenReturn(printWriter);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("Ne doit pas re-authentifier si l'utilisateur est déjà authentifié")
    void shouldNotReAuthenticateIfAlreadyAuthenticated() throws ServletException, IOException {
        // Given - Créer un contexte d'authentification existant
        org.springframework.security.authentication.UsernamePasswordAuthenticationToken existingAuth =
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        testUser, null, testUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtService.extractUsername(validToken)).thenReturn(testUser.getEmail());

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(jwtService, never()).isTokenValid(anyString(), any());
    }

    @Test
    @DisplayName("Doit extraire le token correctement après 'Bearer '")
    void shouldExtractTokenCorrectlyAfterBearer() throws ServletException, IOException {
        // Given
        String authHeader = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.signature";
        String expectedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.signature";

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtService.extractUsername(expectedToken)).thenReturn(testUser.getEmail());
        when(userDetailsService.loadUserByUsername(testUser.getEmail())).thenReturn(testUser);
        when(jwtService.isTokenValid(expectedToken, testUser)).thenReturn(true);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtService).extractUsername(expectedToken);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Doit gérer les tokens avec espaces supplémentaires")
    void shouldHandleTokensWithExtraSpaces() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Bearer  " + validToken);
        // Note: Le filtre prend tout après "Bearer " donc cela inclura l'espace supplémentaire
        when(jwtService.extractUsername(" " + validToken))
                .thenThrow(new RuntimeException("Token avec espace"));

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    @DisplayName("Doit définir les détails d'authentification correctement")
    void shouldSetAuthenticationDetailsCorrectly() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtService.extractUsername(validToken)).thenReturn(testUser.getEmail());
        when(userDetailsService.loadUserByUsername(testUser.getEmail())).thenReturn(testUser);
        when(jwtService.isTokenValid(validToken, testUser)).thenReturn(true);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals(testUser.getEmail(), authentication.getName());
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PATIENT")));
        assertNotNull(authentication.getDetails());
    }

    @Test
    @DisplayName("Doit gérer différents types d'exceptions")
    void shouldHandleDifferentExceptionTypes() throws ServletException, IOException {
        // Given
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtService.extractUsername(validToken))
                .thenThrow(new IllegalArgumentException("Erreur de parsing"));
        when(response.getWriter()).thenReturn(printWriter);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType("application/json");
        String responseBody = stringWriter.toString();
        assertTrue(responseBody.contains("Token JWT Invalide ou Expiré"));
        assertTrue(responseBody.contains("Erreur de parsing"));
    }

    @Test
    @DisplayName("Doit gérer un header Authorization vide")
    void shouldHandleEmptyAuthorizationHeader() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("");

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).extractUsername(anyString());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("Doit gérer Bearer sans token")
    void shouldHandleBearerWithoutToken() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Bearer ");

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);
        when(jwtService.extractUsername("")).thenThrow(new RuntimeException("Token vide"));

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }
}