package com.equipo.backend.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.equipo.backend.DTO.request.TrainingWeekRequestDTO;
import com.equipo.backend.DTO.response.TrainingWeekResponseDTO;
import com.equipo.backend.entity.TrainingWeek;
import com.equipo.backend.repository.TrainingWeekRepository;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class TrainingWeekService {

    private final TrainingWeekRepository trainingWeekRepository;

    /**
     * Con este método creo una nueva semana de entrenamiento en la base de datos
     *
     * @param requestDTO las fechas de inicio y fin que me mandan en la petición
     * @return el DTO de la semana recién creada
     * @throws IllegalArgumentException si la fecha de fin es anterior a la de inicio (sería un error lógico)
     */
    public TrainingWeekResponseDTO createWeek(TrainingWeekRequestDTO requestDTO) {

        LocalDate startingDate = LocalDate.parse(requestDTO.getStartingDate());
        LocalDate finishDate = LocalDate.parse(requestDTO.getFinishDate());

        if (finishDate.isBefore(startingDate)) {
            throw new IllegalArgumentException(
                    "La fecha de fin no puede ser anterior a la fecha de inicio");
        }

        TrainingWeek week = TrainingWeek.builder()
                .startingDate(startingDate)
                .finishDate(finishDate)
                .build();

        TrainingWeek savedWeek = trainingWeekRepository.save(week);
        return toResponse(savedWeek);
    }

    /**
     * Aquí busco una semana de entrenamiento utilizando su id
     *
     * @param id el ID de la semana
     * @return el DTO con los datos de la semana
     * @throws RuntimeException si no llego a encontrar la semana
     */
    public TrainingWeekResponseDTO getWeekById(Long id) {
        TrainingWeek week = trainingWeekRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Semana no encontrada con id: " + id));
        return toResponse(week);
    }

    /**
     * Con esto devuelvo la lista de todas las semanas de entrenamiento que hemos registrado
     *
     * @return la lista de DTOs de las semanas
     */
    public List<TrainingWeekResponseDTO> getAllWeeks() {
        return trainingWeekRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Este método privado lo escribí para mapear la entidad TrainingWeek a su respectivo DTO de respuesta
     *
     * @param week la entidad
     * @return el DTO con su información mapeada
     */
    private TrainingWeekResponseDTO toResponse(TrainingWeek week) {
        return TrainingWeekResponseDTO.builder()
                .id(week.getId())
                .startingDate(week.getStartingDate())
                .finishDate(week.getFinishDate())
                .build();
    }
}
