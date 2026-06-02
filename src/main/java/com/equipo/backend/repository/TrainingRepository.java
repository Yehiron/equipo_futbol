package com.equipo.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.equipo.backend.entity.Training;

/**
 * Este es el repositorio de acceso a datos que hice para la entidad Training
 */
@Repository
public interface TrainingRepository extends JpaRepository<Training, Long> {

    /**
     * Con este método busco y devuelvo todos los entrenamientos de una semana específica.
     * Spring Data JPA interpreta "TrainingWeekId" como el ID de la relación de forma automática
     */
    List<Training> findByTrainingWeekId(Long weekId);

    /**
     * Aquí cuento cuántos entrenamientos tiene ya guardados una semana.
     * Me sirve para validar en el servicio que no nos pasemos del límite de 3
     */
    long countByTrainingWeekId(Long weekId);
}
