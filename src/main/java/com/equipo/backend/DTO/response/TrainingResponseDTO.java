package com.equipo.backend.DTO.response;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para un entrenamiento individual.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingResponseDTO {

    private Long id;

    private Long weekId;

    private Integer trainingNumber;

    private LocalDate date;
}
