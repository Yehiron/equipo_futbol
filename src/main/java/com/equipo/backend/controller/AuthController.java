package com.equipo.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.equipo.backend.DTO.request.LoginRequestDTO;
import com.equipo.backend.DTO.request.UserRequestDTO;
import com.equipo.backend.DTO.response.LoginResponseDTO;
import com.equipo.backend.DTO.response.UserResponseDTO;
import com.equipo.backend.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller de autenticación
 *
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    /**
     * Registra un nuevo usuario.
     *
     *
     * @param requestDTO datos del usuario a registrar
     * @return HTTP 201 Created con los datos del usuario
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody UserRequestDTO requestDTO) {
        UserResponseDTO response = userService.createUser(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Autentica un usuario y retorna un token jwt
     *
     * @param loginDTO credenciales del usuario
     * @return HTTP 200 OK con el token JWT y datos del usuario
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginDTO) {
        LoginResponseDTO response = userService.login(loginDTO);
        return ResponseEntity.ok(response);
    }
}
