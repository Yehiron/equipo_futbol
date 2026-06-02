package com.equipo.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.equipo.backend.entity.TrainingPerformance;

/**
 * Este es el repositorio de acceso a datos que hice para la entidad TrainingPerformance.
 */
@Repository
public interface TraininPerformanceRepository extends JpaRepository<TrainingPerformance, Long> {

    /**
     * Con este método busco y devuelvo todos los rendimientos de un entrenamiento específico.
     */
    List<TrainingPerformance> findByTrainingId(Long trainingId);

    /**
     * Esta consulta JPQL la programé a mano para obtener todos los rendimientos de todos los
     * entrenamientos registrados en una semana en específico.
     *
     */
    @Query("SELECT tp FROM TrainingPerformance tp WHERE tp.training.trainingWeek.id = :weekId")
    List<TrainingPerformance> findAllByWeekId(@Param("weekId") Long weekId);

    /**
     * Con esta consulta obtengo los rendimientos de un jugador específico en una semana determinada
     *
     * La creé para que los jugadores consulten únicamente sus propios resultados semanales sin
     * meter la nariz en los datos de los demás
     */
    @Query("SELECT tp FROM TrainingPerformance tp " +
           "WHERE tp.training.trainingWeek.id = :weekId " +
           "AND tp.user.id = :playerId")
    List<TrainingPerformance> findByWeekIdAndPlayerId(
        @Param("weekId") Long weekId,
        @Param("playerId") Long playerId
    );
}
