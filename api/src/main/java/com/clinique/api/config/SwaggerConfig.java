package com.clinique.api.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Configure la documentation OpenAPI (Swagger) pour qu'elle
 * gère l'authentification JWT (Bearer Token).
 */
@Configuration
// 1. Définit un "schéma de sécurité" nommé "bearerAuth"
@SecurityScheme(
        name = "bearerAuth", // Un nom pour y faire référence
        type = SecuritySchemeType.HTTP, // C'est un schéma de type HTTP
        bearerFormat = "JWT", // Le format du token
        scheme = "bearer" // Le type de schéma (Bearer)
)
// 2. Définit les informations générales de l'API
@OpenAPIDefinition(
        info = @Info(title = "API Clinique", version = "v1.1", description = "API avancée pour assistant clinique"),
        // 3. Applique le schéma "bearerAuth" à TOUS les endpoints
        security = {
                @SecurityRequirement(name = "bearerAuth")
        }
)
public class SwaggerConfig {
}