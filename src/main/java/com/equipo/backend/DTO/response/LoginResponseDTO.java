package com.equipo.backend.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Este DTO lo diseñé para responder cuando el login de un usuario es exitoso.
 *
 * Le meto el token JWT que generé a mano y los datos básicos del usuario que se acaba de autenticar.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseDTO {

    /** Este es el token JWT que el cliente debe meter en el header Authorization como Bearer token. */
    private String token;

    /** Aquí meto los datos básicos del usuario autenticado. */
    private UserResponseDTO user;
}
