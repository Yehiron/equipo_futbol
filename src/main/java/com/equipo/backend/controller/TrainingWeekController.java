package com.equipo.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.equipo.backend.DTO.request.TrainingWeekRequestDTO;
import com.equipo.backend.DTO.response.TrainingWeekResponseDTO;
import com.equipo.backend.entity.enums.Role;
import com.equipo.backend.security.RequiresRole;
import com.equipo.backend.service.TrainingWeekService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller que armé para gestionar las semanas de entrenamiento.
 *
 * utilizo la anotación @RequiresRole para proteger los endpoints
 * según el rol necesario
 */
@RestController
@RequestMapping("/semanas")
@RequiredArgsConstructor
public class TrainingWeekController {

    private final TrainingWeekService trainingWeekService;

    /**
     * Aquí creo una nueva semana de entrenamiento
     * le puse la anotación para que SOLO el entrenador pueda hacerlo
     */
    @PostMapping
    @RequiresRole(Role.ENTRENADOR)
    public ResponseEntity<TrainingWeekResponseDTO> createWeek(
            @Valid @RequestBody TrainingWeekRequestDTO requestDTO) {
        TrainingWeekResponseDTO response = trainingWeekService.createWeek(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Listo todas las semanas de entrenamiento.
     * Como tanto entrenadores como jugadores necesitan ver esto,
     * permito el acceso a ambos roles
     */
    @GetMapping
    @RequiresRole({Role.ENTRENADOR, Role.JUGADOR})
    public ResponseEntity<List<TrainingWeekResponseDTO>> getAllWeeks() {
        return ResponseEntity.ok(trainingWeekService.getAllWeeks());
    }

    /**
     * Obtengo una semana de entrenamiento específica por su id
     * igual que el anterior, accesible para ambos roles
     */
    @GetMapping("/{id}")
    @RequiresRole({Role.ENTRENADOR, Role.JUGADOR})
    public ResponseEntity<TrainingWeekResponseDTO> getWeekById(@PathVariable Long id) {
        return ResponseEntity.ok(trainingWeekService.getWeekById(id));
    }
}
