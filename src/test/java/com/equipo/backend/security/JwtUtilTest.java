package com.equipo.backend.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para JwtUtil.
 *
 * ReflectionTestUtils.setField() se usa para inyectar los valores de
 * @Value directamente en el objeto (sin levantar el contexto de Spring).
 * Esto hace los tests mucho más rápidos.
 */
@DisplayName("JwtUtil - Tests de generación y validación de JWT")
class JwtUtilTest {

    private JwtFilter jwtUtil;

    /** Clave secreta de prueba (mínimo 256 bits = 32 caracteres) */
    private static final String TEST_SECRET =
            "test-secret-key-for-unit-tests-only-minimum-32chars";

    /** Expiración de 10 minutos en milisegundos */
    private static final long EXPIRATION_10_MIN = 600000L;

    /** Expiración de 1 milisegundo para simular token expirado */
    private static final long EXPIRATION_INMEDIATA = 1L;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtFilter();
        // Inyectar valores de @Value manualmente sin Spring Context
        ReflectionTestUtils.setField(jwtUtil, "secretKey", TEST_SECRET);
        ReflectionTestUtils.setField(jwtUtil, "tokenExpiration", EXPIRATION_10_MIN);
    }

    // =========================================================
    // TESTS DE GENERACIÓN
    // =========================================================

    @Test
    @DisplayName("Debe generar un token no nulo y no vacío")
    void generateToken_debeRetornarTokenNoNulo() {
        String token = jwtUtil.generateToken("jugador@test.com", "JUGADOR");

        assertNotNull(token, "El token no debe ser nulo");
        assertFalse(token.isBlank(), "El token no debe estar vacío");
    }

    @Test
    @DisplayName("El token generado debe tener 3 partes separadas por puntos (formato JWT)")
    void generateToken_debeSerFormatoJWT() {
        String token = jwtUtil.generateToken("jugador@test.com", "JUGADOR");

        String[] partes = token.split("\\.");
        assertEquals(3, partes.length,
                "Un JWT debe tener exactamente 3 partes: header.payload.signature");
    }

    // =========================================================
    // TESTS DE VALIDACIÓN
    // =========================================================

    @Test
    @DisplayName("Token recién generado debe ser válido")
    void validateToken_tokenValido_debeRetornarTrue() {
        String token = jwtUtil.generateToken("entrenador@test.com", "ENTRENADOR");

        assertTrue(jwtUtil.validateToken(token),
                "Un token recién generado debe ser válido");
    }

    @Test
    @DisplayName("Token manipulado (firma incorrecta) debe ser inválido")
    void validateToken_tokenManipulado_debeRetornarFalse() {
        String token = jwtUtil.generateToken("jugador@test.com", "JUGADOR");

        // Manipular el token cambiando el último carácter
        String tokenManipulado = token.substring(0, token.length() - 1) + "X";

        assertFalse(jwtUtil.validateToken(tokenManipulado),
                "Un token con firma modificada debe ser inválido");
    }

    @Test
    @DisplayName("Token con formato incorrecto debe ser inválido")
    void validateToken_tokenMalformado_debeRetornarFalse() {
        assertFalse(jwtUtil.validateToken("esto.no.es.un.jwt.valido"),
                "Un token con formato incorrecto debe ser inválido");
    }

    @Test
    @DisplayName("Token nulo o vacío debe ser inválido")
    void validateToken_tokenVacio_debeRetornarFalse() {
        assertFalse(jwtUtil.validateToken(""),
                "Un token vacío debe ser inválido");
    }

    @Test
    @DisplayName("Token expirado debe ser inválido")
    void validateToken_tokenExpirado_debeRetornarFalse() throws InterruptedException {
        // Configurar expiración de 1ms
        ReflectionTestUtils.setField(jwtUtil, "tokenExpiration", EXPIRATION_INMEDIATA);

        String token = jwtUtil.generateToken("jugador@test.com", "JUGADOR");

        // Esperar a que el token expire
        Thread.sleep(10);

        assertFalse(jwtUtil.validateToken(token),
                "Un token expirado debe ser inválido");
    }

    // =========================================================
    // TESTS DE EXTRACCIÓN DE CLAIMS
    // =========================================================

    @Test
    @DisplayName("Debe extraer el email (subject) correctamente del token")
    void extractEmail_debeRetornarEmailCorrecto() {
        String emailEsperado = "jugador@futbol.com";
        String token = jwtUtil.generateToken(emailEsperado, "JUGADOR");

        String emailExtraido = jwtUtil.extractEmail(token);

        assertEquals(emailEsperado, emailExtraido,
                "El email extraído debe coincidir con el email con que se generó el token");
    }

    @Test
    @DisplayName("Debe extraer el rol correctamente del token")
    void extractRole_debeRetornarRolCorrecto() {
        String rolEsperado = "ENTRENADOR";
        String token = jwtUtil.generateToken("entrenador@futbol.com", rolEsperado);

        String rolExtraido = jwtUtil.extractRole(token);

        assertEquals(rolEsperado, rolExtraido,
                "El rol extraído debe coincidir con el rol con que se generó el token");
    }

    @Test
    @DisplayName("Tokens de diferentes usuarios deben ser distintos")
    void generateToken_usuariosDiferentes_debenGenerarTokensDiferentes() {
        String token1 = jwtUtil.generateToken("jugador1@test.com", "JUGADOR");
        String token2 = jwtUtil.generateToken("jugador2@test.com", "JUGADOR");

        assertNotEquals(token1, token2,
                "Tokens de diferentes usuarios deben ser distintos");
    }
}
