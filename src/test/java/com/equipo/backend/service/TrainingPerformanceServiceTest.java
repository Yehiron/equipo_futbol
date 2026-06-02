package com.equipo.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.equipo.backend.DTO.request.TrainingPerformanceRequestDTO;
import com.equipo.backend.DTO.request.TrainingPerformanceRequestDTO.PlayerPerformanceDTO;
import com.equipo.backend.DTO.response.TrainingPerformanceResponseDTO;
import com.equipo.backend.entity.Training;
import com.equipo.backend.entity.TrainingPerformance;
import com.equipo.backend.entity.User;
import com.equipo.backend.entity.enums.Role;
import com.equipo.backend.repository.TraininPerformanceRepository;
import com.equipo.backend.repository.TrainingRepository;
import com.equipo.backend.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para TrainingPerformanceService.
 *
 * Se valida especialmente la fórmula de cálculo del resultado.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TrainingPerformanceService - Tests de cálculo de rendimiento")
class TrainingPerformanceServiceTest {

    @Mock
    private TraininPerformanceRepository performanceRepository;

    @Mock
    private TrainingRepository trainingRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TrainingPerformanceService performanceService;

    private Training entrenamiento;
    private User jugador;

    @BeforeEach
    void setUp() {
        entrenamiento = Training.builder().id(1L).trainingNumber(1).build();

        jugador = new User();
        jugador.setId(1L);
        jugador.setName("Jugador3");
        jugador.setEmail("jugador3@test.com");
        jugador.setRole(Role.JUGADOR);
        jugador.setPassword("hash");
    }

    // =========================================================
    // TESTS DE FÓRMULA
    // =========================================================

    @Test
    @DisplayName("Debe calcular el resultado con la fórmula del enunciado: 20% + 30% + 50%")
    void registerPerformances_debeCalcularResultadoCorrectamente() {
        // ARRANGE: Jugador3 del enunciado: shotPower=15, velocity=3, pases=30
        // Resultado esperado = (15*0.20) + (3*0.30) + (30*0.50) = 3.0 + 0.9 + 15.0 = 18.9
        PlayerPerformanceDTO playerDTO = new PlayerPerformanceDTO();
        ReflectionTestUtils.setField(playerDTO, "playerId", 1L);
        ReflectionTestUtils.setField(playerDTO, "shotPower", 15.0);
        ReflectionTestUtils.setField(playerDTO, "velocity", 3.0);
        ReflectionTestUtils.setField(playerDTO, "effectivePasses", 30);

        TrainingPerformanceRequestDTO requestDTO = new TrainingPerformanceRequestDTO();
        ReflectionTestUtils.setField(requestDTO, "trainingId", 1L);
        ReflectionTestUtils.setField(requestDTO, "performances", List.of(playerDTO));

        when(trainingRepository.findById(1L)).thenReturn(Optional.of(entrenamiento));
        when(userRepository.findById(1L)).thenReturn(Optional.of(jugador));

        // Capturar el objeto que se guarda para verificar el resultado calculado
        when(performanceRepository.save(any(TrainingPerformance.class)))
                .thenAnswer(invocation -> {
                    TrainingPerformance saved = invocation.getArgument(0);
                    saved = TrainingPerformance.builder()
                            .id(1L)
                            .training(entrenamiento)
                            .user(jugador)
                            .shotPower(saved.getShotPower())
                            .velocity(saved.getVelocity())
                            .effectivePasses(saved.getEffectivePasses())
                            .result(saved.getResult())
                            .build();
                    return saved;
                });

        // ACT
        List<TrainingPerformanceResponseDTO> resultados =
                performanceService.registerPerformances(requestDTO);

        // ASSERT
        assertEquals(1, resultados.size(), "Debe guardarse 1 rendimiento");
        assertEquals(18.9, resultados.get(0).getResult(),
                "El resultado de Jugador3 debe ser 18.9 según el enunciado");
    }

    @Test
    @DisplayName("Verificar fórmula con Jugador1 del enunciado: resultado = 16.0")
    void registerPerformances_jugador1_debeCalcular16() {
        // Jugador1: shotPower=10, velocity=5, pases=25
        // Resultado = (10*0.20) + (5*0.30) + (25*0.50) = 2.0 + 1.5 + 12.5 = 16.0
        assertResultadoCalculado(10.0, 5.0, 25, 16.0);
    }

    @Test
    @DisplayName("Verificar fórmula con Jugador2 del enunciado: resultado = 14.7")
    void registerPerformances_jugador2_debeCalcular14_7() {
        // Jugador2: shotPower=16, velocity=5, pases=20
        // Resultado = (16*0.20) + (5*0.30) + (20*0.50) = 3.2 + 1.5 + 10.0 = 14.7
        assertResultadoCalculado(16.0, 5.0, 20, 14.7);
    }

    @Test
    @DisplayName("Entrenamiento inexistente debe lanzar IllegalArgumentException")
    void registerPerformances_entrenamientoInexistente_debeLanzarExcepcion() {
        // ARRANGE
        PlayerPerformanceDTO playerDTO = new PlayerPerformanceDTO();
        ReflectionTestUtils.setField(playerDTO, "playerId", 1L);
        ReflectionTestUtils.setField(playerDTO, "shotPower", 10.0);
        ReflectionTestUtils.setField(playerDTO, "velocity", 5.0);
        ReflectionTestUtils.setField(playerDTO, "effectivePasses", 25);

        TrainingPerformanceRequestDTO requestDTO = new TrainingPerformanceRequestDTO();
        ReflectionTestUtils.setField(requestDTO, "trainingId", 999L);
        ReflectionTestUtils.setField(requestDTO, "performances", List.of(playerDTO));

        when(trainingRepository.findById(999L)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThrows(
                IllegalArgumentException.class,
                () -> performanceService.registerPerformances(requestDTO),
                "Debe lanzar IllegalArgumentException si el entrenamiento no existe"
        );
    }

    // =========================================================
    // MÉTODO AUXILIAR
    // =========================================================

    /**
     * Helper para probar la fórmula con diferentes valores sin repetir código.
     */
    private void assertResultadoCalculado(double shotPower, double velocity,
            int pases, double resultadoEsperado) {

        PlayerPerformanceDTO playerDTO = new PlayerPerformanceDTO();
        ReflectionTestUtils.setField(playerDTO, "playerId", 1L);
        ReflectionTestUtils.setField(playerDTO, "shotPower", shotPower);
        ReflectionTestUtils.setField(playerDTO, "velocity", velocity);
        ReflectionTestUtils.setField(playerDTO, "effectivePasses", pases);

        TrainingPerformanceRequestDTO requestDTO = new TrainingPerformanceRequestDTO();
        ReflectionTestUtils.setField(requestDTO, "trainingId", 1L);
        ReflectionTestUtils.setField(requestDTO, "performances", List.of(playerDTO));

        when(trainingRepository.findById(1L)).thenReturn(Optional.of(entrenamiento));
        when(userRepository.findById(1L)).thenReturn(Optional.of(jugador));
        when(performanceRepository.save(any(TrainingPerformance.class)))
                .thenAnswer(invocation -> {
                    TrainingPerformance saved = invocation.getArgument(0);
                    return TrainingPerformance.builder()
                            .id(1L).training(entrenamiento).user(jugador)
                            .shotPower(saved.getShotPower()).velocity(saved.getVelocity())
                            .effectivePasses(saved.getEffectivePasses()).result(saved.getResult())
                            .build();
                });

        List<TrainingPerformanceResponseDTO> resultados =
                performanceService.registerPerformances(requestDTO);

        assertEquals(resultadoEsperado, resultados.get(0).getResult(),
                String.format("Para shotPower=%.1f, velocity=%.1f, pases=%d → esperado=%.1f",
                        shotPower, velocity, pases, resultadoEsperado));
    }
}
