package com.equipo.backend.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtFilter {

    /**
     * Aquí leo mi clave secreta desde el yaml
     */
    @Value("${security.jwt.secret-key}")
    private String secretKey;

    /**
     * Este es el tiempo de expiración del token que leo de mi configuración.
     * Por defecto le dejé 600000 ms, es decir, 10 minutos
     */
    @Value("${security.jwt.token-expiration}")
    private long tokenExpiration;

    /**
     * Con este método convierto el string de mi clave secreta en un objeto SecretKey
     *
     * @return El SecretKey listo para firmar o verificar los tokens.
     */
    private SecretKey buildSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Aquí es donde genero el token JWT firmado 
     *
     * @param email el email del usuario (lo guardo como subject de mi token)
     * @param role  el rol del usuario (lo meto como claim personalizado)
     * @return el token JWT listo en formato String
     */
    public String generateToken(String email, String role) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + tokenExpiration);

        return Jwts.builder()
                // Meto el email en el subject (es el identificador principal)
                .subject(email)
                // Meto el rol como un claim personalizado
                .claim("role", role)
                // Le pongo la fecha de emisión de ahora mismo
                .issuedAt(now)
                // Le configuro la fecha de expiración
                .expiration(expirationDate)
                // Lo firmo usando mi SecretKey con HMAC-SHA256
                .signWith(buildSigningKey())
                .compact(); // Con esto armo el String final del token
    }

    /**
     * Con este método valido si el token que me llega es correcto y no ha expirado
     *
     * @param token el token que voy a revisar
     * @return true si está todo bien, false si el token no sirve
     */
    public boolean validateToken(String token) {
        try {
            // El método parseSignedClaims() se encarga de verificar la firma y la expiración por mí.
            Jwts.parser()
                    .verifyWith(buildSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // Si salta alguna excepción (firma mala, expirado, etc.), devuelvo false.
            return false;
        }
    }

    /**
     * Aquí saco el email (subject) de un token que ya sé que es válido
     *
     * @param token el token del que quiero sacar el email
     * @return el email del usuario
     */
    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    /**
     * Con esto extraigo el rol del usuario que guardé en el token
     *
     * @param token el token del que quiero sacar el rol
     * @return el rol del usuario (JUGADOR o ENTRENADOR)
     */
    public String extractRole(String token) {
        return extractClaims(token).get("role", String.class);
    }

    /**
     * Este método privado lo hice para parsear el token y sacar todos sus claims
     *
     * @param token el token JWT
     * @return Claims con todos los datos que contiene el token
     */
    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(buildSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
