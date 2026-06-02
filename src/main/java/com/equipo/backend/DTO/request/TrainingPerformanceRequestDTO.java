package com.equipo.backend.DTO.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Este DTO lo diseñé para registrar el rendimiento de TODOS los jugadores en un entrenamiento
 * de un solo golpe (mediante un único POST con la lista completa).
 *
 * Ejemplo de JSON:
 * {
 *   "trainingId": 1,
 *   "performances": [
 *     { "playerId": 1, "shotPower": 10.0, "velocity": 5.0, "effectivePasses": 25 },
 *     { "playerId": 2, "shotPower": 16.0, "velocity": 5.0, "effectivePasses": 20 }
 *   ]
 * }
 *
 * Nota: A propósito NO incluí el campo "result" aquí, porque me encargo de calcularlo de forma
 * automática en el backend.
 */
@Data
public class TrainingPerformanceRequestDTO {

    @NotNull(message = "El ID del entrenamiento es obligatorio")
    private Long trainingId;

    @NotEmpty(message = "Debe incluir al menos un jugador")
    @Valid
    private List<PlayerPerformanceDTO> performances;

    /**
     * Esta subclase estática la utilizo para representar el rendimiento individual de cada jugador.
     */
    @Data
    public static class PlayerPerformanceDTO {

        @NotNull(message = "El ID del jugador es obligatorio")
        private Long playerId;

        @NotNull(message = "La potencia de tiro es obligatoria")
        private Double shotPower;

        @NotNull(message = "La velocidad es obligatoria")
        private Double velocity;

        @NotNull(message = "Los pases efectivos son obligatorios")
        private Integer effectivePasses;
    }
}
