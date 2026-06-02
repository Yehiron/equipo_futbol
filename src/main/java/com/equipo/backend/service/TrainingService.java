package com.equipo.backend.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.equipo.backend.DTO.request.TrainingRequestDTO;
import com.equipo.backend.DTO.response.TrainingResponseDTO;
import com.equipo.backend.entity.Training;
import com.equipo.backend.entity.TrainingWeek;
import com.equipo.backend.repository.TrainingRepository;
import com.equipo.backend.repository.TrainingWeekRepository;

import lombok.RequiredArgsConstructor;

/**
 * Este es el servicio que creé para gestionar los entrenamientos individuales de mi app.
 */
@Service
@RequiredArgsConstructor
public class TrainingService {

    private static final int MAX_TRAININGS_PER_WEEK = 3;

    private final TrainingRepository trainingRepository;
    private final TrainingWeekRepository trainingWeekRepository;

    /**
     * Aquí creo un nuevo entrenamiento dentro de una semana específica
     *
     * @param requestDTO los datos del entrenamiento
     * @return el DTO del entrenamiento creado
     * @throws IllegalArgumentException si la semana no existe o si ya completamos los 3 entrenamientos permitidos
     */
    public TrainingResponseDTO createTraining(TrainingRequestDTO requestDTO) {

        // Busco que la semana exista de verdad en la base de datos
        TrainingWeek week = trainingWeekRepository.findById(requestDTO.getWeekId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Semana no encontrada con id: " + requestDTO.getWeekId()));

        // Cuento cuántos entrenamientos ya tiene registrados esa semana
        long existingCount = trainingRepository.countByTrainingWeekId(week.getId());

        // Valido que no me pase del límite máximo (que son 3 entrenamientos a la semana)
        if (existingCount >= MAX_TRAININGS_PER_WEEK) {
            throw new IllegalArgumentException(
                    "La semana ya tiene " + MAX_TRAININGS_PER_WEEK +
                    " entrenamientos. No se pueden agregar más.");
        }

        // El número del entrenamiento lo calculo como el siguiente disponible (1, 2 o 3)
        int trainingNumber = (int) existingCount + 1;

        Training training = Training.builder()
                .trainingWeek(week)
                .trainingNumber(trainingNumber)
                .date(LocalDate.parse(requestDTO.getDate()))
                .build();

        Training savedTraining = trainingRepository.save(training);
        return toResponse(savedTraining);
    }

    /**
     * Con esto busco y devuelvo todos los entrenamientos de una semana específica.
     *
     * @param weekId el ID de la semana
     * @return la lista de entrenamientos de esa semana
     */
    public List<TrainingResponseDTO> getTrainingsByWeek(Long weekId) {
        return trainingRepository.findByTrainingWeekId(weekId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Este método privado lo hice para convertir mi entidad Training a un DTO de respuesta
     *
     * @param training la entidad
     * @return el DTO con su información mapeada
     */
    private TrainingResponseDTO toResponse(Training training) {
        return TrainingResponseDTO.builder()
                .id(training.getId())
                .weekId(training.getTrainingWeek().getId())
                .trainingNumber(training.getTrainingNumber())
                .date(training.getDate())
                .build();
    }
}
