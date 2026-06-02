package com.equipo.backend.DTO.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Este es el DTO que creé para recibir los datos al registrar un entrenamiento.
 *
 * El campo weekId me sirve para identificar a qué semana pertenece este entrenamiento.
 */
@Data
public class TrainingRequestDTO {

    @NotNull(message = "El ID de la semana es obligatorio")
    private Long weekId;

    @NotNull(message = "La fecha del entrenamiento es obligatoria")
    @NotBlank(message = "La fecha no puede estar vacía")
    private String date; // Aquí espero la fecha en formato "yyyy-MM-dd"
}
