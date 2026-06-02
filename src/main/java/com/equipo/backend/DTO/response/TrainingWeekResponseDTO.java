package com.equipo.backend.DTO.response;
import java.time.LocalDate;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingWeekResponseDTO {
    
    private Long id;

    private LocalDate startingDate;

    private LocalDate finishDate;

}
