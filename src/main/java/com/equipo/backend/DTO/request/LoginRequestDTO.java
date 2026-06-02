package com.equipo.backend.DTO.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Este DTO lo creé para recibir las credenciales cuando un usuario quiere hacer login.
 */
@Data
public class LoginRequestDTO {

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
}
