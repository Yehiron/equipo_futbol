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

import com.equipo.backend.DTO.request.TrainingPerformanceRequestDTO;
import com.equipo.backend.DTO.request.TrainingRequestDTO;
import com.equipo.backend.DTO.response.TrainingPerformanceResponseDTO;
import com.equipo.backend.DTO.response.TrainingResponseDTO;
import com.equipo.backend.entity.enums.Role;
import com.equipo.backend.security.RequiresRole;
import com.equipo.backend.service.TrainingPerformanceService;
import com.equipo.backend.service.TrainingService;
import com.equipo.backend.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller que hice para gestionar los entrenamientos.
 *
 * Aquí agrupo los endpoints de entrenamientos y rendimientos, y
 * los protejo con la anotación @RequiresRole según corresponda.
 */
@RestController
@RequestMapping("/entrenamientos")
@RequiredArgsConstructor
public class TrainingController {

    private final TrainingService trainingService;
    private final TrainingPerformanceService performanceService;
    private final UserService userService;

    /**
     * Aquí creo un nuevo entrenamiento dentro de una semana.
     * Le puse la restricción para que solo el ENTRENADOR pueda hacerlo.
     */
    @PostMapping
    @RequiresRole(Role.ENTRENADOR)
    public ResponseEntity<TrainingResponseDTO> createTraining(
            @Valid @RequestBody TrainingRequestDTO requestDTO) {
        TrainingResponseDTO response = trainingService.createTraining(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Muestro los entrenamientos de una semana específica.
     * Como jugador también me interesa ver esto, así que le doy
     * acceso a ambos roles.
     */
    @GetMapping("/semana/{weekId}")
    @RequiresRole({Role.ENTRENADOR, Role.JUGADOR})
    public ResponseEntity<List<TrainingResponseDTO>> getTrainingsByWeek(
            @PathVariable Long weekId) {
        return ResponseEntity.ok(trainingService.getTrainingsByWeek(weekId));
    }

    /**
     * Este endpoint lo hice para registrar el rendimiento de TODOS los jugadores.
     * Obviamente, es tarea exclusiva del ENTRENADOR.
     */
    @PostMapping("/rendimientos")
    @RequiresRole(Role.ENTRENADOR)
    public ResponseEntity<List<TrainingPerformanceResponseDTO>> registerPerformances(
            @Valid @RequestBody TrainingPerformanceRequestDTO requestDTO) {
        List<TrainingPerformanceResponseDTO> response =
                performanceService.registerPerformances(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retorno los rendimientos de un entrenamiento específico.
     * Preferí dejarlo solo para el ENTRENADOR para que vea todo el panorama.
     */
    @GetMapping("/{trainingId}/rendimientos")
    @RequiresRole(Role.ENTRENADOR)
    public ResponseEntity<List<TrainingPerformanceResponseDTO>> getPerformancesByTraining(
            @PathVariable Long trainingId) {
        return ResponseEntity.ok(performanceService.getPerformancesByTraining(trainingId));
    }

    /**
     * Este es especial: retorno los rendimientos del jugador autenticado.
     * Saco el email del request y busco sus datos, así aseguro que el
     * JUGADOR solo vea sus propios resultados.
     */
    @GetMapping("/semana/{weekId}/mis-resultados")
    @RequiresRole({Role.JUGADOR})
    public ResponseEntity<List<TrainingPerformanceResponseDTO>> getMyResults(
            @PathVariable Long weekId,
            HttpServletRequest request) {

        String email = (String) request.getAttribute("authenticatedEmail");
        Long playerId = userService.getUserByEmail(email).getId();

        return ResponseEntity.ok(
                performanceService.getMyPerformancesByWeek(weekId, playerId));
    }
}
