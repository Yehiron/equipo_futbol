package com.equipo.backend.entity;

import java.time.LocalDate;

import com.equipo.backend.entity.enums.Status;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Esta entidad representa una semana de entrenamiento.
 *
 * Decidí que una semana empiece como ABIERTA por defecto mientras registramos los entrenamientos,
 * y luego pasará a CERRADA cuando ya calculemos el equipo titular de los 5 mejores
 */
@Entity
@Table(name = "semana_entrenamiento")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingWeek {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "starting_date", nullable = false)
    private LocalDate startingDate;

    @Column(name = "finish_date", nullable = false)
    private LocalDate finishDate;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private Status status = Status.ABIERTA;
}
