package com.equipo.backend.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.equipo.backend.entity.enums.Role;

/**
 * Anotación personalizada para restringir el acceso a endpoints
 * según el rol del usuario autenticado.
 * 
 * Se puede aplicar a nivel de método en los Controllers.
 * El JwtInterceptor validará si el usuario tiene al menos uno
 * de los roles especificados.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresRole {
    
    /**
     * Lista de roles permitidos para acceder al endpoint.
     * Si no se especifica, por defecto requerirá ENTRENADOR.
     */
    Role[] value() default {Role.ENTRENADOR};
}
