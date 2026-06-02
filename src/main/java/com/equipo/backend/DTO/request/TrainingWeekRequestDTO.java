package com.equipo.backend.DTO.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Este DTO lo creé para recibir las fechas cuando voy a dar de alta una nueva semana de entrenamiento.
 */
@Data
public class TrainingWeekRequestDTO {

    @NotBlank(message = "La fecha de inicio es obligatoria")
    private String startingDate; // La fecha inicial en formato "yyyy-MM-dd"

    @NotBlank(message = "La fecha de fin es obligatoria")
    private String finishDate; // La fecha final en formato "yyyy-MM-dd"
}
