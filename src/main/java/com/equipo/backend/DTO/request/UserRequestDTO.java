package com.equipo.backend.DTO.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Este DTO lo diseñé para registrar o actualizar la información de un usuario.
 *
 * El campo "role" lo dejé opcional; si no me lo mandan en la petición, me encargo de
 * asignarle JUGADOR por defecto en mi servicio.
 */
@Data
public class UserRequestDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String name;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

    /** Este rol es opcional. Si no me lo envían, le asigno JUGADOR automáticamente. */
    private String role;
}
