package com.equipo.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.equipo.backend.entity.TrainingWeek;

/**
 * Este es el repositorio de acceso a datos que hice para la entidad TrainingWeek.
 */
@Repository
public interface TrainingWeekRepository extends JpaRepository<TrainingWeek, Long> {

}
