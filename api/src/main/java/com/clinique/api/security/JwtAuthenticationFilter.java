package com.clinique.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtre qui s'exécute UNE FOIS par requête (grâce à OncePerRequestFilter).
 * Il intercepte chaque requête pour valider le token JWT.
 */
@Component // Marque cette classe comme un Bean Spring
@RequiredArgsConstructor // Crée un constructeur avec les champs 'final' (injection)
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    // UserDetailsService est une interface de Spring Security que nous
    // implémenterons
    // pour lui dire comment charger notre 'User' depuis la BDD.
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain // C'est la "chaîne" des filtres de sécurité
    ) throws ServletException, IOException {

        // 1. Récupérer l'en-tête "Authorization"
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Vérifier si l'en-tête existe et commence par "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // Si non, on passe au filtre suivant et on s'arrête là.
            // (La requête sera probablement bloquée plus tard si la route est protégée)
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extraire le token (en enlevant "Bearer ")
        jwt = authHeader.substring(7);

        try {
            // 4. Extraire l'email du token
            userEmail = jwtService.extractUsername(jwt);

            // 5. Vérifier si l'email est valide ET si l'utilisateur n'est pas déjà
            // authentifié
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // 6. Charger les détails de l'utilisateur depuis la BDD
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                // 7. Vérifier si le token est valide (bonne signature, non expiré, bon
                // utilisateur)
                if (jwtService.isTokenValid(jwt, userDetails)) {

                    // 8. CRÉER le token d'authentification pour Spring Security
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null, // On n'a pas besoin des credentials (mot de passe) ici
                            userDetails.getAuthorities() // Les rôles (PATIENT, THERAPIST...)
                    );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));

                    // 9. METTRE À JOUR le Contexte de Sécurité
                    // C'est cette ligne qui "connecte" l'utilisateur pour cette requête
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

            // 10. Passer au filtre suivant
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            // Gérer les tokens invalides (expirés, malformés, etc.)
            // On envoie une réponse 401 (Non autorisé)
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter()
                    .write("{\"error\": \"Token JWT Invalide ou Expiré\", \"message\": \"" + e.getMessage() + "\"}");
        }
    }
}