package com.equipo.backend.entity;

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
 * Esta entidad guarda el rendimiento de un jugador en un entrenamiento específico
 *
 * Su resultado lo calculo de manera automática en mi servicio aplicando los
 * porcentajes correspondientes: 20% potencia, 30% velocidad y 50% pases efectivos
 */
@Entity
@Table(name = "rendimiento_entrenamiento")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingPerformance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Muchos rendimientos pertenecen a un entrenamiento específico
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "training_id", nullable = false)
    private Training training;

    /**
     * Muchos rendimientos pertenecen a un único jugador
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private User user;

    /** Aquí guardo la potencia de tiro en Km/h (vale un 20%) */
    @Column(name = "shot_power", nullable = false)
    private Double shotPower;

    /** Aquí guardo la velocidad del jugador en Km/h (vale un 30%) */
    @Column(nullable = false)
    private Double velocity;

    /** Aquí guardo la cantidad de pases efectivos (vale un 50%) */
    @Column(name = "effective_passes", nullable = false)
    private Integer effectivePasses;

    /** Este es el resultado final que calculo usando los pesos de cada métrica */
    @Column(nullable = false)
    private Double result;
}
