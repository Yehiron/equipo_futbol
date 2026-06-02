package com.equipo.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.equipo.backend.DTO.request.UserRequestDTO;
import com.equipo.backend.DTO.response.UserResponseDTO;
import com.equipo.backend.entity.enums.Role;
import com.equipo.backend.security.RequiresRole;
import com.equipo.backend.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller que hice para gestionar a los usuarios (jugadores y entrenadores).
 *
 * Todos estos endpoints requieren token JWT porque el interceptor los protege.
 * Utilizo mi propia anotación @RequiresRole para definir quién puede entrar
 * a cada ruta.
 */
@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Endpoint para que cualquier usuario autenticado vea su propio perfil.
     * Saqué el email del request (lo guarda el interceptor) y busco sus datos
     */
    @GetMapping("/perfil")
    @RequiresRole({Role.ENTRENADOR, Role.JUGADOR})
    public ResponseEntity<UserResponseDTO> getMyProfile(HttpServletRequest request) {
        String email = (String) request.getAttribute("authenticatedEmail");
        UserResponseDTO response = userService.getUserByEmail(email);
        return ResponseEntity.ok(response);
    }

    /**
     * Aquí devuelvo todos los jugadores, pero le puse @RequiresRole para
     * que SOLO el ENTRENADOR pueda ver este listado
     */
    @GetMapping
    @RequiresRole(Role.ENTRENADOR)
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * Busco a un jugador por su ID. Nuevamente, restringido solo
     * al entrenador con mi anotación
     */
    @GetMapping("/{id}")
    @RequiresRole(Role.ENTRENADOR)
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    /**
     * Actualizo los datos de un jugador, exclusivo para el entrenador
     */
    @PutMapping("/{id}")
    @RequiresRole(Role.ENTRENADOR)
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequestDTO requestDTO) {
        return ResponseEntity.ok(userService.updateUser(id, requestDTO));
    }

    /**
     * Elimino a un jugador
     */
    @DeleteMapping("/{id}")
    @RequiresRole(Role.ENTRENADOR)
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
