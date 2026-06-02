package com.equipo.backend.service;

import java.util.List;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.equipo.backend.DTO.request.LoginRequestDTO;
import com.equipo.backend.DTO.request.UserRequestDTO;
import com.equipo.backend.DTO.response.LoginResponseDTO;
import com.equipo.backend.DTO.response.UserResponseDTO;
import com.equipo.backend.entity.User;
import com.equipo.backend.entity.enums.Role;
import com.equipo.backend.repository.UserRepository;
import com.equipo.backend.security.JwtFilter;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtFilter jwtUtil;

    /**
     * Instancié a mano el BCryptPasswordEncoder
     */
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Aquí creo y registro a un nuevo usuario en el sistema.
     *
     * Le dejé por defecto el rol JUGADOR. Si quiero asignarle ENTRENADOR,
     * tengo que enviarlo explícitamente en el DTO.
     *
     * @param requestDTO los datos que me mandan para el registro
     * @return el DTO con la info del usuario creado
     * @throws IllegalArgumentException si el correo ya está registrado en la base de datos
     */
    public UserResponseDTO createUser(UserRequestDTO requestDTO) {

        if (userRepository.existsByEmail(requestDTO.getEmail())) {
            throw new IllegalArgumentException(
                    "Ya existe un usuario con el email: " + requestDTO.getEmail());
        }

        User user = new User();
        user.setName(requestDTO.getName());
        user.setEmail(requestDTO.getEmail());
        // Encripto la contraseña usando mi passwordEncoder antes de guardarla
        user.setPassword(passwordEncoder.encode(requestDTO.getPassword()));

        // Si no me mandaron rol, le pongo JUGADOR por defecto
        if (requestDTO.getRole() != null && !requestDTO.getRole().isBlank()) {
            user.setRole(Role.valueOf(requestDTO.getRole().toUpperCase()));
        } else {
            user.setRole(Role.JUGADOR);
        }

        User savedUser = userRepository.save(user);
        return toResponse(savedUser);
    }

    /**
     * Este método lo utilizo para autenticar a un usuario y generarle su token JWT en el login.
     *
     * Hago el siguiente flujo
     * Busco el usuario por su email.
     * Verifico la contraseña usando mi passwordEncoder (matches).
     * Genero el token JWT usando mi JwtUtil.
     * Armo y devuelvo el DTO de respuesta con el token y sus datos básicos.
     *
     * @param loginDTO las credenciales (email y password)
     * @return LoginResponseDTO con el token JWT
     * @throws IllegalArgumentException si el usuario no existe o la contraseña no coincide
     */
    public LoginResponseDTO login(LoginRequestDTO loginDTO) {

        // 1. Busco el usuario por email
        User user = userRepository.findByEmail(loginDTO.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Credenciales incorrectas"));

        // 2. Verifico que coincida la contraseña (matches)
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Credenciales incorrectas");
        }

        // 3. Genero el token con mi JwtUtil
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        // 4. Armo la respuesta final del login
        return LoginResponseDTO.builder()
                .token(token)
                .user(toResponse(user))
                .build();
    }

    /**
     * Aquí busco a un usuario por su ID en la base de datos.
     *
     * @param id el ID del usuario
     * @return el DTO de respuesta
     */
    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + id));
        return toResponse(user);
    }

    /**
     * Con este método busco a un usuario por su email.
     * Lo utilizo sobre todo para que los jugadores puedan consultar su propio perfil.
     *
     * @param email el email del usuario autenticado
     * @return el DTO con su perfil
     */
    public UserResponseDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return toResponse(user);
    }

    /**
     * Con esto devuelvo a todos los usuarios de la base de datos.
     * Sé que solo el ENTRENADOR debería poder hacer esto.
     *
     * @return la lista de DTOs de todos los usuarios
     */
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Aquí borro a un usuario buscando por su ID.
     *
     * @param id el ID del usuario que voy a borrar
     */
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + id));
        userRepository.delete(user);
    }

    /**
     * Aquí actualizo los datos de un usuario existente.
     * Encripto de nuevo la contraseña por seguridad si la cambiaron.
     *
     * @param id         el ID del usuario
     * @param requestDTO los nuevos datos
     * @return el DTO con el usuario ya actualizado
     */
    public UserResponseDTO updateUser(Long id, UserRequestDTO requestDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + id));

        user.setName(requestDTO.getName());
        user.setEmail(requestDTO.getEmail());
        user.setPassword(passwordEncoder.encode(requestDTO.getPassword()));

        if (requestDTO.getRole() != null && !requestDTO.getRole().isBlank()) {
            user.setRole(Role.valueOf(requestDTO.getRole().toUpperCase()));
        }

        User savedUser = userRepository.save(user);
        return toResponse(savedUser);
    }

    /**
     * Este método privado lo creé para mapear mi entidad User a un DTO de respuesta UserResponseDTO.
     * Así me aseguro de que la contraseña NUNCA viaje en la respuesta y se mantenga segura.
     *
     * @param user la entidad
     * @return el DTO mapeado
     */
    private UserResponseDTO toResponse(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}
