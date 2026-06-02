package com.equipo.backend.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Este DTO lo creé para devolver el rendimiento de un jugador en un entrenamiento.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingPerformanceResponseDTO {

    private Long id;

    private Long trainingId;

    private Long playerId;

    private String playerName;

    private Double shotPower;

    private Double velocity;

    private Integer effectivePasses;

    /** Este resultado lo calculo automáticamente en mi backend usando mi fórmula de pesos. */
    private Double result;
}
