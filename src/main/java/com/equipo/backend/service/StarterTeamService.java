package com.equipo.backend.service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.equipo.backend.DTO.response.StarterTeamResponseDTO;
import com.equipo.backend.DTO.response.StarterTeamResponseDTO.PlayerScoreDTO;
import com.equipo.backend.entity.TrainingPerformance;
import com.equipo.backend.repository.TraininPerformanceRepository;
import com.equipo.backend.repository.TrainingRepository;
import com.equipo.backend.repository.TrainingWeekRepository;

import lombok.RequiredArgsConstructor;

/**
 * Este es el servicio estrella que me inventé para calcular el equipo titular
 */
@Service
@RequiredArgsConstructor
public class StarterTeamService {

    /** Aquí defino cuántos titulares van a jugar */
    private static final int STARTERS_COUNT = 5;

    /** La cantidad de entrenamientos obligatorios semanales (según el cliente, son 3). */
    private static final int REQUIRED_TRAININGS = 3;

    private final TrainingRepository trainingRepository;
    private final TrainingWeekRepository trainingWeekRepository;
    private final TraininPerformanceRepository performanceRepository;

    /**
     * Con este método calculo el equipo titular para la semana que me pidan.
     *
     * @param weekId el ID de la semana de entrenamiento
     * @return el DTO con los 5 jugadores titulares y su promedio semanal
     * @throws RuntimeException      si la semana no existe
     * @throws IllegalStateException si la semana no cuenta con los 3 entrenamientos completados
     */
    public StarterTeamResponseDTO getStarterTeam(Long weekId) {

        // Primero me aseguro de que la semana exista
        trainingWeekRepository.findById(weekId)
                .orElseThrow(() -> new RuntimeException(
                        "Semana no encontrada con id: " + weekId));

        // Valido que tengan registrados sus 3 entrenamientos completos
        long trainingCount = trainingRepository.countByTrainingWeekId(weekId);

        if (trainingCount < REQUIRED_TRAININGS) {
            throw new IllegalStateException(
                    "No hay suficiente información. La semana tiene " + trainingCount +
                    " de " + REQUIRED_TRAININGS + " entrenamientos requeridos.");
        }

        //  Saco de la base de datos todos los rendimientos individuales de esa semana
        List<TrainingPerformance> allPerformances =
                performanceRepository.findAllByWeekId(weekId);

        if (allPerformances.isEmpty()) {
            throw new IllegalStateException(
                    "No hay suficiente información. No se han registrado rendimientos esta semana.");
        }

        //  Agrupo por jugador y calculo su promedio semanal.
        //    Usé groupingBy con averagingDouble, que hace toda la matemática por mí basándose en el campo result.
        Map<Long, Double> averageByPlayer = allPerformances.stream()
                .collect(Collectors.groupingBy(
                        tp -> tp.getUser().getId(),
                        Collectors.averagingDouble(TrainingPerformance::getResult)
                ));

        // Armo un mapa temporal para tener a la mano el nombre de cada jugador mapeado a su ID
        Map<Long, String> nameByPlayer = allPerformances.stream()
                .collect(Collectors.toMap(
                        tp -> tp.getUser().getId(),
                        tp -> tp.getUser().getName(),
                        (existing, replacement) -> existing // Si hay duplicados, me quedo con el primero
                ));

        // Convierto todo a DTOs, ordeno de mayor a menor por el promedio y me quedo con los 5 mejores
        List<PlayerScoreDTO> starters = averageByPlayer.entrySet().stream()
                // Los ordeno de forma descendente usando el promedio
                .sorted(Comparator.comparingDouble(Map.Entry<Long, Double>::getValue).reversed())
                // Me quedo solo con los mejores 5 (STARTERS_COUNT)
                .limit(STARTERS_COUNT)
                // Mapeo los datos al PlayerScoreDTO redondeando a 2 decimales para que se vea limpio
                .map(entry -> PlayerScoreDTO.builder()
                        .playerId(entry.getKey())
                        .playerName(nameByPlayer.get(entry.getKey()))
                        .averageScore(Math.round(entry.getValue() * 100.0) / 100.0)
                        .build())
                .toList();

        // Por último, armo y devuelvo el DTO final con el resultado del cálculo
        return StarterTeamResponseDTO.builder()
                .weekId(weekId)
                .starters(starters)
                .build();
    }
}
