package com.ecom.serviceflotte.exception;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Format unique des erreurs JSON de l'API, identique a celui de service-locations.
 */
public record ApiError(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<String> details
) {
}
