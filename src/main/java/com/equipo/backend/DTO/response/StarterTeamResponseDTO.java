package com.equipo.backend.DTO.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Este DTO lo creé para devolver el equipo titular de una semana.
 *
 * Aquí meto la lista de los 5 jugadores que obtuvieron el mayor promedio de rendimiento semanal.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StarterTeamResponseDTO {

    /** El ID de la semana que acabo de evaluar. */
    private Long weekId;

    /** Los 5 jugadores titulares que ordené por promedio de mayor a menor. */
    private List<PlayerScoreDTO> starters;

    /**
     * Esta subclase estática representa la información y el puntaje promedio semanal de cada titular.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PlayerScoreDTO {

        private Long playerId;

        private String playerName;

        /** El promedio final de los 3 entrenamientos de la semana. */
        private Double averageScore;
    }
}
