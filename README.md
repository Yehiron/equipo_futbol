# ⚽ Equipo de Fútbol 5 — API REST

API REST desarrollada en **Spring Boot 3 + Java 21** para gestionar los entrenamientos semanales de un equipo de fútbol 5 y determinar el equipo titular mediante un algoritmo de puntuación.

## 🚀 Tecnologías

| Tecnología | Versión | Uso |
|---|---|---|
| Java | 21 | Lenguaje principal |
| Spring Boot | 3.4.5 | Framework web |
| Spring Data JPA | 3.4.5 | Persistencia |
| MySQL | 8.x | Base de datos |
| JJWT | 0.12.6 | JWT manual (sin Spring Security) |
| Spring Security Crypto | (BOM) | BCrypt para contraseñas |
| Lombok | latest | Reducción de boilerplate |
| JUnit 5 + Mockito | (BOM) | Tests unitarios |

---

## 📦 Requisitos previos

- Java 21+
- Maven 3.8+ (o usar `./mvnw` incluido)
- MySQL 8.x corriendo localmente
- Git

---

## 🛠 Configuración de la base de datos

1. Crea la base de datos en MySQL:

```sql
CREATE DATABASE futbol_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'java'@'localhost' IDENTIFIED BY 'Theburgery1!';
GRANT ALL PRIVILEGES ON futbol_db.* TO 'java'@'localhost';
FLUSH PRIVILEGES;
```

2. Las tablas se crean automáticamente con `ddl-auto: update`.

---

## ▶️ Ejecución

```bash
# Clonar el repositorio
git clone <https://github.com/Yehiron/equipo_futbol>
cd backend

# Ejecutar con Maven Wrapper (no requiere Maven instalado)
./mvnw spring-boot:run
```

La API estará disponible en: `http://localhost:8080/api/v1/equipo`

---

## 🧪 Tests unitarios

```bash
./mvnw test
```

---

## 📋 Endpoints

### 🔓 Autenticación (Públicos — No requieren token)

#### Registrar usuario
```
POST /api/v1/equipo/auth/register
```
**Body:**
```json
{
  "name": "Juan Pérez",
  "email": "juan@futbol.com",
  "password": "123456"
}
```
> Por defecto se asigna rol **JUGADOR**. Para asignar **ENTRENADOR**, agregar `"role": "ENTRENADOR"`.

**Respuesta (201 Created):**
```json
{
  "id": 1,
  "name": "Juan Pérez",
  "email": "juan@futbol.com",
  "role": "JUGADOR"
}
```

---

#### Login
```
POST /api/v1/equipo/auth/login
```
**Body:**
```json
{
  "email": "juan@futbol.com",
  "password": "123456"
}
```

**Respuesta (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "user": {
    "id": 1,
    "name": "Juan Pérez",
    "email": "juan@futbol.com",
    "role": "JUGADOR"
  }
}
```
> Usa el `token` en el header de todas las peticiones protegidas:
> `Authorization: Bearer <token>`

---

### 🔒 Semanas de entrenamiento (Requieren token)

#### Crear semana (solo ENTRENADOR)
```
POST /api/v1/equipo/semanas
Authorization: Bearer <token>
```
**Body:**
```json
{
  "startingDate": "2025-06-02",
  "finishDate": "2025-06-06"
}
```

#### Listar semanas
```
GET /api/v1/equipo/semanas
Authorization: Bearer <token>
```

#### Ver semana por ID
```
GET /api/v1/equipo/semanas/{id}
Authorization: Bearer <token>
```

---

### 🔒 Entrenamientos (Endpoint #1 del enunciado)

#### Crear entrenamiento (solo ENTRENADOR)
```
POST /api/v1/equipo/entrenamientos
Authorization: Bearer <token>
```
**Body:**
```json
{
  "weekId": 1,
  "date": "2025-06-02"
}
```
> Se asigna automáticamente el número de entrenamiento (1, 2 o 3).
> Si la semana ya tiene 3 entrenamientos → Error 400.

---

#### Registrar rendimientos de todos los jugadores (solo ENTRENADOR)
```
POST /api/v1/equipo/entrenamientos/rendimientos
Authorization: Bearer <token>
```
**Body:**
```json
{
  "trainingId": 1,
  "performances": [
    { "playerId": 1, "shotPower": 10.0, "velocity": 5.0, "effectivePasses": 25 },
    { "playerId": 2, "shotPower": 16.0, "velocity": 5.0, "effectivePasses": 20 },
    { "playerId": 3, "shotPower": 15.0, "velocity": 3.0, "effectivePasses": 30 },
    { "playerId": 4, "shotPower": 12.0, "velocity": 4.0, "effectivePasses": 18 },
    { "playerId": 5, "shotPower": 11.0, "velocity": 3.0, "effectivePasses": 19 },
    { "playerId": 6, "shotPower": 9.0,  "velocity": 3.0, "effectivePasses": 22 },
    { "playerId": 7, "shotPower": 10.0, "velocity": 2.0, "effectivePasses": 24 }
  ]
}
```
> El campo `result` **no se envía**, el backend lo calcula automáticamente:
> `result = (shotPower × 0.20) + (velocity × 0.30) + (effectivePasses × 0.50)`

---

#### Mis resultados en la semana (solo JUGADOR)
```
GET /api/v1/equipo/entrenamientos/semana/{weekId}/mis-resultados
Authorization: Bearer <token>
```

---

### 🏆 Equipo Titular (Endpoint #2 del enunciado)

```
GET /api/v1/equipo/equipo-titular/{weekId}
Authorization: Bearer <token>
```

**Respuesta exitosa (200 OK — semana con 3 entrenamientos):**
```json
{
  "weekId": 1,
  "starters": [
    { "playerId": 3, "playerName": "Jugador3", "averageScore": 18.9 },
    { "playerId": 1, "playerName": "Jugador1", "averageScore": 16.0 },
    { "playerId": 2, "playerName": "Jugador2", "averageScore": 14.7 },
    { "playerId": 7, "playerName": "Jugador7", "averageScore": 14.6 },
    { "playerId": 6, "playerName": "Jugador6", "averageScore": 13.7 }
  ]
}
```

**Respuesta si faltan entrenamientos (422 Unprocessable Entity):**
```json
{
  "status": 422,
  "message": "No hay suficiente información. La semana tiene 2 de 3 entrenamientos requeridos."
}
```

---

## 🔑 Cómo funciona el JWT Manual

1. El cliente hace `POST /auth/login` → recibe un token JWT
2. En cada petición protegida incluye: `Authorization: Bearer <token>`
3. El `JwtInterceptor` intercepta la petición, valida el token y extrae email + rol
4. Si el token es inválido → responde **401 Unauthorized**
5. Si es válido → la petición llega al Controller

---

## 🏗️ Estructura del proyecto

```
src/main/java/com/equipo/backend/
├── config/
│   └── WebMvcConfig.java          ← Registra el interceptor JWT
├── controller/
│   ├── AuthController.java        ← /auth (público)
│   ├── UserController.java        ← /usuarios (protegido)
│   ├── TrainingWeekController.java ← /semanas
│   ├── TrainingController.java    ← /entrenamientos (endpoint #1)
│   └── StarterTeamController.java ← /equipo-titular (endpoint #2)
├── DTO/
│   ├── request/
│   └── response/
├── entity/
│   ├── enums/
│   ├── User.java
│   ├── TrainingWeek.java
│   ├── Training.java
│   └── TrainingPerformance.java
├── exception/
│   └── GlobalExceptionHandler.java
├── repository/
├── security/
│   ├── JwtUtil.java               ← Generación/validación JWT
│   └── JwtInterceptor.java        ← Interceptor de peticiones
└── service/
    ├── UserService.java
    ├── TrainingWeekService.java
    ├── TrainingService.java
    ├── TrainingPerformanceService.java
    └── StarterTeamService.java    ← Algoritmo del equipo titular
```

---

## 📐 Fórmula de puntuación

```
resultado = (potencia_tiro × 0.20) + (velocidad × 0.30) + (pases_efectivos × 0.50)
```

### Ejemplo (Jugador3):
```
resultado = (15 × 0.20) + (3 × 0.30) + (30 × 0.50)
          = 3.0 + 0.9 + 15.0
          = 18.9 ✓
```
