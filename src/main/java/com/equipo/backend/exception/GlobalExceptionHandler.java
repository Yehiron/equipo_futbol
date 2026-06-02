package com.equipo.backend.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 *
 * Con @RestControllerAdvice intercepto las excepciones lanzadas por cualquier
 * Controller y las convierto en respuestas HTTP con un cuerpo JSON 
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Aquí controlo los errores de validación de Bean Validation (como @NotNull, @NotBlank, etc.)
     * cuando me mandan DTOs con campos vacíos o inválidos
     *
     * Devuelvo un HTTP 400 Bad Request con el mapa detallado de qué falló en cada campo
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException ex) {

        Map<String, String> fieldErrors = new HashMap<>();

        // Recorro cada error de campo y lo voy metiendo en mi mapa
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Error de validación", fieldErrors);
    }

    /**
     * Aquí controlo los errores de lógica de negocio (IllegalArgumentException) de mi app
     * (por ejemplo intentar registrar un 4to entrenamiento en la misma semana)
     *
     * Devuelvo un HTTP 400 Bad Request
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
    }

    /**
     * Aquí controlo los errores de estado (IllegalStateException) (como intentar calcular
     * el equipo titular en una semana que todavía no tiene sus 3 entrenamientos obligatorios)
     *
     * Devuelvo un HTTP 422 Unprocessable Entity porque la petición es correcta pero el estado no lo permite
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(
            IllegalStateException ex) {
        return buildErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), null);
    }

    /**
     * Este es mi manejador de último recurso para capturar cualquier excepción inesperada o
     * que no haya controlado explícitamente
     *
     * Devuelvo un HTTP 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno del servidor: " + ex.getMessage(),
                null
        );
    }

    /**
     * Este método privado lo hice para construir todas las respuestas de error en un formato JSON
     *
     * @param status  el código de estado HTTP
     * @param message el mensaje principal de lo que salió mal
     * @param details detalles adicionales si los hay (como los errores de validación)
     * @return la ResponseEntity lista con su cuerpo JSON
     */
    private ResponseEntity<Map<String, Object>> buildErrorResponse(
            HttpStatus status, String message, Object details) {

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);

        if (details != null) {
            body.put("details", details);
        }

        return ResponseEntity.status(status).body(body);
    }
}
