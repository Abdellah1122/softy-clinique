package com.clinique.api.security;

import com.clinique.api.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service pour gérer toutes les opérations JWT :
 * - Génération
 * - Validation
 * - Extraction des informations
 */
@Service
public class JwtService {

    // Injecte la clé secrète depuis application.properties
    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    // Injecte la durée d'expiration depuis application.properties
    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    /**
     * Extrait l'email de l'utilisateur (le "subject") du token.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrait une information spécifique (un "claim") du token.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Génère un token pour un utilisateur (UserDetails).
     */
    public String generateToken(UserDetails userDetails) {
        // Ajoute des informations supplémentaires ("claims") au token.
        // C'est une bonne pratique d'y inclure les rôles.
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("roles", userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));

        // Si vous voulez aussi l'ID de l'utilisateur (peut être utile pour le front-end)
        if (userDetails instanceof User) {
            extraClaims.put("userId", ((User) userDetails).getId());
        }

        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    /**
     * Construit le token JWT.
     * CORRIGÉ pour jjwt 0.12.x
     */
    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ) {
        return Jwts.builder()
                .claims(extraClaims) // .setClaims() devient .claims()
                .subject(userDetails.getUsername()) // .setSubject() devient .subject()
                .issuedAt(new Date(System.currentTimeMillis())) // .setIssuedAt() devient .issuedAt()
                .expiration(new Date(System.currentTimeMillis() + expiration)) // .setExpiration() devient .expiration()
                .signWith(getSignInKey()) // On retire SignatureAlgorithm.HS256
                .compact();
    }

    /**
     * Vérifie si un token est valide (email correct + non expiré).
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Parse le token et vérifie sa signature pour extraire tous les "claims".
     * CORRIGÉ pour jjwt 0.12.x
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith((javax.crypto.SecretKey) getSignInKey()) // API correcte pour jjwt 0.12.x
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * C'est ici que la clé Base64 de votre .properties est décodée
     * pour devenir une vraie clé de signature cryptographique.
     */
    private javax.crypto.SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}