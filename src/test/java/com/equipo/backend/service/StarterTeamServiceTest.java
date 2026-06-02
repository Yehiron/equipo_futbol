package com.equipo.backend.service;

import com.equipo.backend.DTO.response.StarterTeamResponseDTO;
import com.equipo.backend.entity.Training;
import com.equipo.backend.entity.TrainingPerformance;
import com.equipo.backend.entity.TrainingWeek;
import com.equipo.backend.entity.User;
import com.equipo.backend.entity.enums.Role;
import com.equipo.backend.repository.TraininPerformanceRepository;
import com.equipo.backend.repository.TrainingRepository;
import com.equipo.backend.repository.TrainingWeekRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para StarterTeamService.
 *
 * Usa Mockito para simular los repositorios sin necesidad de base de datos.
 * @ExtendWith(MockitoExtension.class) habilita las anotaciones de Mockito.
 * @Mock crea mocks automáticos de los repositorios.
 * @InjectMocks crea StarterTeamService inyectando los mocks.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StarterTeamService - Tests del algoritmo de equipo titular")
class StarterTeamServiceTest {

    @Mock
    private TrainingRepository trainingRepository;

    @Mock
    private TrainingWeekRepository trainingWeekRepository;

    @Mock
    private TraininPerformanceRepository performanceRepository;

    @InjectMocks
    private StarterTeamService starterTeamService;

    // =========================================================
    // DATOS DE PRUEBA
    // =========================================================

    private TrainingWeek semana;
    private Training entrenamiento1;
    private Training entrenamiento2;
    private Training entrenamiento3;

    private User jugador1, jugador2, jugador3, jugador4, jugador5, jugador6, jugador7;

    @BeforeEach
    void setUp() {
        semana = TrainingWeek.builder()
                .id(1L)
                .startingDate(LocalDate.of(2025, 6, 2))
                .finishDate(LocalDate.of(2025, 6, 6))
                .build();

        entrenamiento1 = Training.builder().id(1L).trainingWeek(semana).trainingNumber(1).build();
        entrenamiento2 = Training.builder().id(2L).trainingWeek(semana).trainingNumber(2).build();
        entrenamiento3 = Training.builder().id(3L).trainingWeek(semana).trainingNumber(3).build();

        jugador1 = crearJugador(1L, "Jugador1");
        jugador2 = crearJugador(2L, "Jugador2");
        jugador3 = crearJugador(3L, "Jugador3");
        jugador4 = crearJugador(4L, "Jugador4");
        jugador5 = crearJugador(5L, "Jugador5");
        jugador6 = crearJugador(6L, "Jugador6");
        jugador7 = crearJugador(7L, "Jugador7");
    }

    // =========================================================
    // TESTS PRINCIPALES
    // =========================================================

    @Test
    @DisplayName("Con 3 entrenamientos completos debe retornar los 5 mejores jugadores")
    void getStarterTeam_con3Entrenamientos_debeRetornar5Titulares() {
        // ARRANGE: configurar mocks
        when(trainingWeekRepository.findById(1L)).thenReturn(Optional.of(semana));
        when(trainingRepository.countByTrainingWeekId(1L)).thenReturn(3L);
        when(performanceRepository.findAllByWeekId(1L))
                .thenReturn(crearRendimientosDelEnunciado());

        // ACT: ejecutar el método bajo prueba
        StarterTeamResponseDTO resultado = starterTeamService.getStarterTeam(1L);

        // ASSERT: verificar resultados
        assertNotNull(resultado, "El resultado no debe ser nulo");
        assertEquals(1L, resultado.getWeekId(), "El weekId debe coincidir");
        assertEquals(5, resultado.getStarters().size(),
                "Deben retornarse exactamente 5 titulares");
    }

    @Test
    @DisplayName("Los titulares deben estar ordenados de mayor a menor promedio")
    void getStarterTeam_titulares_debenEstarOrdenadosDescendente() {
        // ARRANGE
        when(trainingWeekRepository.findById(1L)).thenReturn(Optional.of(semana));
        when(trainingRepository.countByTrainingWeekId(1L)).thenReturn(3L);
        when(performanceRepository.findAllByWeekId(1L))
                .thenReturn(crearRendimientosDelEnunciado());

        // ACT
        StarterTeamResponseDTO resultado = starterTeamService.getStarterTeam(1L);

        // ASSERT: verificar que el promedio va de mayor a menor
        List<StarterTeamResponseDTO.PlayerScoreDTO> starters = resultado.getStarters();
        for (int i = 0; i < starters.size() - 1; i++) {
            assertTrue(
                    starters.get(i).getAverageScore() >= starters.get(i + 1).getAverageScore(),
                    "Los titulares deben estar ordenados de mayor a menor promedio"
            );
        }
    }

    @Test
    @DisplayName("Con menos de 3 entrenamientos debe lanzar IllegalStateException")
    void getStarterTeam_conMenosDe3Entrenamientos_debeLanzarExcepcion() {
        // ARRANGE: solo 2 entrenamientos
        when(trainingWeekRepository.findById(1L)).thenReturn(Optional.of(semana));
        when(trainingRepository.countByTrainingWeekId(1L)).thenReturn(2L);

        // ACT & ASSERT
        IllegalStateException excepcion = assertThrows(
                IllegalStateException.class,
                () -> starterTeamService.getStarterTeam(1L),
                "Debe lanzar IllegalStateException si faltan entrenamientos"
        );

        assertTrue(excepcion.getMessage().contains("No hay suficiente información"),
                "El mensaje debe indicar falta de información");
    }

    @Test
    @DisplayName("Con 0 entrenamientos debe lanzar IllegalStateException con mensaje correcto")
    void getStarterTeam_con0Entrenamientos_debeLanzarExcepcionConMensajeEspecifico() {
        // ARRANGE
        when(trainingWeekRepository.findById(1L)).thenReturn(Optional.of(semana));
        when(trainingRepository.countByTrainingWeekId(1L)).thenReturn(0L);

        // ACT & ASSERT
        IllegalStateException excepcion = assertThrows(
                IllegalStateException.class,
                () -> starterTeamService.getStarterTeam(1L)
        );

        // Verificar que el mensaje menciona cuántos entrenamientos faltan
        assertTrue(excepcion.getMessage().contains("0 de 3"),
                "El mensaje debe indicar que tiene 0 de 3 entrenamientos");
    }

    @Test
    @DisplayName("El cálculo del promedio debe ser correcto con el ejemplo del enunciado")
    void getStarterTeam_calculoDePromedio_debeSerCorrecto() {
        // El enunciado tiene un solo entrenamiento, creamos 3 iguales para probar el promedio
        // Jugador3: resultado = 18.9 en cada entrenamiento → promedio = 18.9
        when(trainingWeekRepository.findById(1L)).thenReturn(Optional.of(semana));
        when(trainingRepository.countByTrainingWeekId(1L)).thenReturn(3L);
        when(performanceRepository.findAllByWeekId(1L))
                .thenReturn(crearRendimientosDelEnunciado());

        StarterTeamResponseDTO resultado = starterTeamService.getStarterTeam(1L);

        // El jugador con mayor promedio debe ser Jugador3 (18.9)
        StarterTeamResponseDTO.PlayerScoreDTO mejorJugador = resultado.getStarters().get(0);
        assertEquals("Jugador3", mejorJugador.getPlayerName(),
                "Jugador3 debe ser el mejor según el ejemplo del enunciado");
    }

    @Test
    @DisplayName("Semana inexistente debe lanzar RuntimeException")
    void getStarterTeam_semanaInexistente_debeLanzarRuntimeException() {
        // ARRANGE: la semana no existe
        when(trainingWeekRepository.findById(99L)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThrows(
                RuntimeException.class,
                () -> starterTeamService.getStarterTeam(99L),
                "Debe lanzar RuntimeException si la semana no existe"
        );
    }

    // =========================================================
    // MÉTODOS DE APOYO
    // =========================================================

    /**
     * Crea rendimientos del entrenamiento #1 del enunciado replicados en los 3 entrenamientos.
     * Esto nos permite probar el promedio usando datos reales del problema.
     *
     * Jugador | Potencia | Velocidad | Pases | Resultado
     * Jugador1|    10    |     5     |   25  |   16.0
     * Jugador2|    16    |     5     |   20  |   14.7
     * Jugador3|    15    |     3     |   30  |   18.9
     * Jugador4|    12    |     4     |   18  |   12.6
     * Jugador5|    11    |     3     |   19  |   12.6
     * Jugador6|     9    |     3     |   22  |   13.7
     * Jugador7|    10    |     2     |   24  |   14.6
     */
    private List<TrainingPerformance> crearRendimientosDelEnunciado() {
        return Arrays.asList(
                // Entrenamiento 1
                crearRendimiento(jugador1, entrenamiento1, 10.0, 5.0, 25, 16.0),
                crearRendimiento(jugador2, entrenamiento1, 16.0, 5.0, 20, 14.7),
                crearRendimiento(jugador3, entrenamiento1, 15.0, 3.0, 30, 18.9),
                crearRendimiento(jugador4, entrenamiento1, 12.0, 4.0, 18, 12.6),
                crearRendimiento(jugador5, entrenamiento1, 11.0, 3.0, 19, 12.6),
                crearRendimiento(jugador6, entrenamiento1, 9.0,  3.0, 22, 13.7),
                crearRendimiento(jugador7, entrenamiento1, 10.0, 2.0, 24, 14.6),
                // Entrenamiento 2 (mismos valores para simplificar el test de promedio)
                crearRendimiento(jugador1, entrenamiento2, 10.0, 5.0, 25, 16.0),
                crearRendimiento(jugador2, entrenamiento2, 16.0, 5.0, 20, 14.7),
                crearRendimiento(jugador3, entrenamiento2, 15.0, 3.0, 30, 18.9),
                crearRendimiento(jugador4, entrenamiento2, 12.0, 4.0, 18, 12.6),
                crearRendimiento(jugador5, entrenamiento2, 11.0, 3.0, 19, 12.6),
                crearRendimiento(jugador6, entrenamiento2, 9.0,  3.0, 22, 13.7),
                crearRendimiento(jugador7, entrenamiento2, 10.0, 2.0, 24, 14.6),
                // Entrenamiento 3 (mismos valores para simplificar el test de promedio)
                crearRendimiento(jugador1, entrenamiento3, 10.0, 5.0, 25, 16.0),
                crearRendimiento(jugador2, entrenamiento3, 16.0, 5.0, 20, 14.7),
                crearRendimiento(jugador3, entrenamiento3, 15.0, 3.0, 30, 18.9),
                crearRendimiento(jugador4, entrenamiento3, 12.0, 4.0, 18, 12.6),
                crearRendimiento(jugador5, entrenamiento3, 11.0, 3.0, 19, 12.6),
                crearRendimiento(jugador6, entrenamiento3, 9.0,  3.0, 22, 13.7),
                crearRendimiento(jugador7, entrenamiento3, 10.0, 2.0, 24, 14.6)
        );
    }

    private User crearJugador(Long id, String nombre) {
        User user = new User();
        user.setId(id);
        user.setName(nombre);
        user.setEmail(nombre.toLowerCase() + "@futbol.com");
        user.setPassword("encriptado");
        user.setRole(Role.JUGADOR);
        return user;
    }

    private TrainingPerformance crearRendimiento(User jugador, Training entrenamiento,
            double shotPower, double velocity, int pases, double resultado) {
        return TrainingPerformance.builder()
                .training(entrenamiento)
                .user(jugador)
                .shotPower(shotPower)
                .velocity(velocity)
                .effectivePasses(pases)
                .result(resultado)
                .build();
    }
}
