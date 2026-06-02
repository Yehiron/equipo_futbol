package com.equipo.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.equipo.backend.entity.User;

/**
 * Este es el repositorio de acceso a datos que hice para la entidad User
 * Spring Data JPA me genera la implementación de forma automática al correr la app 
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Aquí verifico si ya existe un usuario registrado con el email dado.
     * Lo uso al registrar usuarios para evitar que se creen cuentas duplicadas
     */
    boolean existsByEmail(String email);

    /**
     * Con este método busco a un usuario en base a su email
     * Me sirve sobre todo para el proceso de login, para recuperar su información
     */
    Optional<User> findByEmail(String email);
}
