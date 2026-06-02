package com.equipo.backend.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.equipo.backend.DTO.request.TrainingPerformanceRequestDTO;
import com.equipo.backend.DTO.request.TrainingPerformanceRequestDTO.PlayerPerformanceDTO;
import com.equipo.backend.DTO.response.TrainingPerformanceResponseDTO;
import com.equipo.backend.entity.Training;
import com.equipo.backend.entity.TrainingPerformance;
import com.equipo.backend.entity.User;
import com.equipo.backend.repository.TraininPerformanceRepository;
import com.equipo.backend.repository.TrainingRepository;
import com.equipo.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TrainingPerformanceService {

    /** Aquí configuro los pesos de cada métrica tal como me los pidieron en el enunciado. */
    private static final double SHOT_POWER_WEIGHT = 0.20;
    private static final double VELOCITY_WEIGHT = 0.30;
    private static final double EFFECTIVE_PASSES_WEIGHT = 0.50;

    private final TraininPerformanceRepository performanceRepository;
    private final TrainingRepository trainingRepository;
    private final UserRepository userRepository;

    /**
     * Con este método registro los rendimientos de todos los jugadores para un entrenamiento específico en un solo lote (POST)
     * El cálculo final de la puntuación de cada jugador se hace automáticamente antes de guardar
     *
     * @param requestDTO el DTO que tiene el ID del entrenamiento y la lista de los rendimientos
     * @return la lista con los DTOs de rendimiento ya guardados en BD
     * @throws IllegalArgumentException si el entrenamiento o alguno de los jugadores de la lista no existe
     */
    public List<TrainingPerformanceResponseDTO> registerPerformances(
            TrainingPerformanceRequestDTO requestDTO) {

        // 1. Busco el entrenamiento por su ID
        Training training = trainingRepository.findById(requestDTO.getTrainingId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Entrenamiento no encontrado con id: " + requestDTO.getTrainingId()));

        List<TrainingPerformance> savedPerformances = new ArrayList<>();

        // 2. Recorro y proceso el rendimiento de cada jugador de la lista
        for (PlayerPerformanceDTO playerDTO : requestDTO.getPerformances()) {

            // Busco al jugador por su ID
            User player = userRepository.findById(playerDTO.getPlayerId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Jugador no encontrado con id: " + playerDTO.getPlayerId()));

            // Calculo el resultado de forma automática con mi fórmula
            double result = calculateResult(
                    playerDTO.getShotPower(),
                    playerDTO.getVelocity(),
                    playerDTO.getEffectivePasses()
            );

            // Armo la entidad y la guardo en la base de datos
            TrainingPerformance performance = TrainingPerformance.builder()
                    .training(training)
                    .user(player)
                    .shotPower(playerDTO.getShotPower())
                    .velocity(playerDTO.getVelocity())
                    .effectivePasses(playerDTO.getEffectivePasses())
                    .result(result)
                    .build();

            savedPerformances.add(performanceRepository.save(performance));
        }

        // 3. Mapeo todo a mis DTOs de respuesta y lo devuelvo
        return savedPerformances.stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Con esto busco y devuelvo los rendimientos de un entrenamiento específico
     * Sé que solo el ENTRENADOR debería poder hacer esto
     *
     * @param trainingId el ID del entrenamiento
     * @return la lista de rendimientos
     */
    public List<TrainingPerformanceResponseDTO> getPerformancesByTraining(Long trainingId) {
        return performanceRepository.findByTrainingId(trainingId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Aquí saco los rendimientos de un jugador en toda una semana.
     * Me sirve para que los jugadores consulten SOLO sus propios resultados y no los de los demás
     *
     * @param weekId   el ID de la semana
     * @param playerId el ID del jugador autenticado
     * @return la lista de rendimientos de ese jugador en la semana
     */
    public List<TrainingPerformanceResponseDTO> getMyPerformancesByWeek(
            Long weekId, Long playerId) {
        return performanceRepository.findByWeekIdAndPlayerId(weekId, playerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Este es el método donde aplico la fórmula para calcular el resultado final de cada jugador
     *
     * @param shotPower       potencia de tiro en Km/h
     * @param velocity        velocidad en Km/h
     * @param effectivePasses número de pases efectivos
     * @return el resultado calculado redondeado a 1 decimal
     */
    private double calculateResult(Double shotPower, Double velocity, Integer effectivePasses) {
        double raw = (shotPower * SHOT_POWER_WEIGHT)
                + (velocity * VELOCITY_WEIGHT)
                + (effectivePasses * EFFECTIVE_PASSES_WEIGHT);

        // Lo redondeo a 1 decimal para que coincida exactamente con los ejemplos del enunciado
        return Math.round(raw * 10.0) / 10.0;
    }

    /**
     * Este método privado lo hice para mapear mi entidad TrainingPerformance a un DTO de respuesta
     *
     * @param performance la entidad
     * @return el DTO con su información mapeada
     */
    private TrainingPerformanceResponseDTO toResponse(TrainingPerformance performance) {
        return TrainingPerformanceResponseDTO.builder()
                .id(performance.getId())
                .trainingId(performance.getTraining().getId())
                .playerId(performance.getUser().getId())
                .playerName(performance.getUser().getName())
                .shotPower(performance.getShotPower())
                .velocity(performance.getVelocity())
                .effectivePasses(performance.getEffectivePasses())
                .result(performance.getResult())
                .build();
    }
}
