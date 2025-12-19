package com.clinique.api.config;

import com.clinique.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Contient la configuration des "Beans" de l'application,
 * principalement pour la sécurité.
 */
@Configuration // Indique à Spring que c'est une classe de configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    private final UserRepository userRepository;

    /**
     * C'est le "Bean" que Spring Security utilise pour charger les données d'un utilisateur.
     * C'est ici qu'on fait le lien avec notre UserRepository.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec l'email: " + username));
    }

    /**
     * C'est le "Bean" qui encode les mots de passe.
     * Nous utilisons BCrypt, le standard actuel.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * C'est le "fournisseur" d'authentification.
     * On lui dit d'utiliser notre 'userDetailsService' (pour trouver l'utilisateur)
     * et notre 'passwordEncoder' (pour vérifier le mot de passe).
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Le gestionnaire d'authentification (AuthenticationManager).
     * Nous en aurons besoin dans notre AuthService pour faire le "login".
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    @Bean
    public org.springframework.web.reactive.function.client.WebClient mlWebClient() {
        return org.springframework.web.reactive.function.client.WebClient.builder()
                .baseUrl("http://localhost:8001") // L'adresse de votre service Python
                .defaultHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE,
                        org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}