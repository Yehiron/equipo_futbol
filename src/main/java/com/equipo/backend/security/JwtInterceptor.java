package com.equipo.backend.security;

import java.io.IOException;
import java.util.Arrays;

import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import com.equipo.backend.entity.enums.Role;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Interceptor JWT que cree para proteger los endpoints
 * 
 * Lo utilizo como filtro antes de que la petición llegue al Controller
 * Aquí valido el token y además reviso si el método tiene la anotación
 * @RequiresRole para verificar los permisos.
 */
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtFilter jwtUtil;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws IOException {

        // Extraigo el header de autorización
        String authHeader = request.getHeader("Authorization");

        // Verifico que venga el token y tenga el formato correcto
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendUnauthorizedResponse(response, "Falta el token de autorización o el formato es incorrecto");
            return false;
        }

        // Saco solo el token, quitando el "Bearer "
        String token = authHeader.substring(7);

        // Valido el token usando mi JwtFilter
        if (!jwtUtil.validateToken(token)) {
            sendUnauthorizedResponse(response, "El token es inválido o ya expiró");
            return false;
        }

        // Extraigo los datos del usuario y los guardo en el request
        String email = jwtUtil.extractEmail(token);
        String roleStr = jwtUtil.extractRole(token);
        Role userRole = Role.valueOf(roleStr);

        request.setAttribute("authenticatedEmail", email);
        request.setAttribute("authenticatedRole", roleStr);

        // Verifico permisos con la anotación @RequiresRole
        if (handler instanceof HandlerMethod handlerMethod) {
            RequiresRole requiresRole = handlerMethod.getMethodAnnotation(RequiresRole.class);
            
            if (requiresRole != null) {
                boolean hasPermission = Arrays.asList(requiresRole.value()).contains(userRole);
                
                if (!hasPermission) {
                    sendForbiddenResponse(response, "No tienes permisos suficientes para esta acción");
                    return false;
                }
            }
        }

        // Todo en orden, dejo pasar la petición
        return true;
    }

    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        var errorBody = java.util.Map.of(
                "status", 401,
                "error", "No autorizado",
                "message", message
        );

        response.getWriter().write(objectMapper.writeValueAsString(errorBody));
    }
    
    private void sendForbiddenResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");

        var errorBody = java.util.Map.of(
                "status", 403,
                "error", "Prohibido",
                "message", message
        );

        response.getWriter().write(objectMapper.writeValueAsString(errorBody));
    }
}
