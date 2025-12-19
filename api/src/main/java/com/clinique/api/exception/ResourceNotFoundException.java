package com.clinique.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception personnalisée pour les cas "Non Trouvé" (404).
 * @ResponseStatus(HttpStatus.NOT_FOUND) : Dit à Spring de
 * renvoyer automatiquement une erreur 404 lorsqu'il voit cette exception.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}