package com.equipo.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.equipo.backend.DTO.response.StarterTeamResponseDTO;
import com.equipo.backend.entity.enums.Role;
import com.equipo.backend.security.RequiresRole;
import com.equipo.backend.service.StarterTeamService;

import lombok.RequiredArgsConstructor;

/**
 * Controller que implementé para calcular y devolver el equipo titular
 *
 * Utilizo @RequiresRole para permitir que tanto jugadores como entrenadores
 * puedan ver el resultado del algoritmo
 */
@RestController
@RequestMapping("/equipo-titular")
@RequiredArgsConstructor
public class StarterTeamController {

    private final StarterTeamService starterTeamService;

    /**
     * Aquí devuelvo el equipo titular de la semana, que son los 5 jugadores
     * con mejor promedio cualquiera en el equipo puede consultar esto
     */
    @GetMapping("/{weekId}")
    @RequiresRole({Role.ENTRENADOR, Role.JUGADOR})
    public ResponseEntity<StarterTeamResponseDTO> getStarterTeam(@PathVariable Long weekId) {
        StarterTeamResponseDTO response = starterTeamService.getStarterTeam(weekId);
        return ResponseEntity.ok(response);
    }
}
