package com.equipo.backend.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Esta entidad representa un entrenamiento individual dentro de una semana
 */
@Entity
@Table(name = "entrenamiento")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Training {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Muchos entrenamientos pertenecen a una sola semana.
     * Le configuré FetchType.LAZY para que la semana no se cargue de la base de datos
     * a menos que realmente acceda a este atributo
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "week_id", nullable = false)
    private TrainingWeek trainingWeek;

    /**
     * Este es el número de entrenamiento dentro de la semana (1, 2 o 3)
     * En el servicio me encargo de validar que no se metan más de 3 por semana
     */
    @Column(name = "training_number", nullable = false)
    private Integer trainingNumber;

    @Column(name = "fecha", nullable = false)
    private LocalDate date;
}
