# Pontificia Universidad Javeriana - Facultad de Ingeniería
## Principios de Desarrollo de Software
### Informe de Laboratorio: Implementación de un CRUD para un "Zoo de Mascotas Fantásticas" en Spring Boot

---

**Integrantes del Equipo:** [Nombres de los integrantes]  
**Docente Revisor:** soldash  
**Fecha:** Septiembre 2026  
**Repositorio GitHub:** [URL del repositorio]  
**Tecnologías:** Java 25 (LTS) / Spring Boot 3.4.3 / MySQL 8.4 / Maven / Git & Gitflow  

---

## 1. Descripción del Proyecto

### 1.1. Objetivo y Alcance
El objetivo principal del proyecto es el diseño, construcción y validación de un sistema de gestión empresarial (CRUD) para un **"Zoo de Mascotas Fantásticas"**, aplicando una arquitectura limpia en 3 capas (Presentación, Lógica de Negocio y Acceso a Datos) con Spring Boot y MySQL.

El sistema permite:
- Administrar el ciclo de vida completo (Crear, Leer, Actualizar, Eliminar) de **Criaturas Mágicas** (`Creature`).
- Administrar las **Zonas del Zoológico** (`Zone`) donde habitan las criaturas.
- Garantizar la integridad referencial y el cumplimiento riguroso de reglas de negocio veterinarias y de capacidad física mediante códigos de estado HTTP semánticos (200, 201, 204, 400, 404, 409).

### 1.2. Arquitectura en 3 Capas
El software se estructuró bajo una estricta separación de responsabilidades, garantizando bajo acoplamiento y alta cohesión:
1. **Capa de Presentación (`controller`):** Controladores `@RestController` que exponen endpoints HTTP, validan los datos de entrada con Bean Validation y devuelven DTOs inmutables implementados con Java Records.
2. **Capa de Lógica de Negocio (`service`):** Servicios que encapsulan las reglas del zoológico (control de capacidad, validación de estado crítico de salud) y gestionan transacciones con `@Transactional`.
3. **Capa de Acceso a Datos (`repository` y `entity`):** Entidades JPA mapeadas a tablas de MySQL y repositorios `JpaRepository` con consultas JPQL optimizadas para erradicar el problema de las N+1 consultas.

```
       [ Cliente HTTP / Postman ]
                   │
                   ▼ (HTTP Requests: JSON / DTOs)
   ┌───────────────────────────────┐
   │ Capa de Presentación          │
   │ (CreatureController / Zone...) │
   └───────────────┬───────────────┘
                   │
                   ▼ (Llamadas a interfaces de servicio)
   ┌───────────────────────────────┐
   │ Capa de Lógica de Negocio     │
   │ (CreatureService / Zone...)   │
   └───────────────┬───────────────┘
                   │
                   ▼ (Consultas JPA / Proyecciones)
   ┌───────────────────────────────┐
   │ Capa de Acceso a Datos        │
   │ (CreatureRepository / Zone...)│
   └───────────────┬───────────────┘
                   │
                   ▼ (JDBC / SQL Nativo)
         [ MySQL: zoo_fantastico ]
```

### 1.3. Inyección de Dependencias: Constructor Injection vs. @Autowired
Un aspecto esencial evaluado por el docente es el mecanismo para implementar la **Inversión de Control (IoC)** y la **Inyección de Dependencias (DI)**. En Spring existen 3 enfoques:

| Mecanismo de Inyección | Sintaxis | Evaluación Técnica y Decisión |
| :--- | :--- | :--- |
| **1. Por Atributo (`Field Injection`)** | `@Autowired private ZoneService zoneService;` | **Antipatrón rechazado:** Oculta dependencias, no permite campos `final` (rompe la inmutabilidad) y dificulta los tests unitarios al requerir el contenedor de Spring. |
| **2. Por Setter (`Setter Injection`)** | `public void setZoneService(...)` | **Desaconsejado:** Permite mutar la dependencia en tiempo de ejecución, violando la inmutabilidad. |
| **3. Por Constructor (`Constructor Injection`)** | `@RequiredArgsConstructor` + `private final` | **Estándar de Oro (Implementado):** Garantiza inmutabilidad, promueve el principio *Fail-Fast* en arranque y facilita pruebas unitarias con Mockito (`new ZoneController(mockService)`). |

En el proyecto se aplicó **Inyección por Constructor** asistida por Lombok (`@RequiredArgsConstructor`):
```java
@RestController
@RequestMapping("/api/zones")
@RequiredArgsConstructor // Lombok genera el constructor canónico con los campos 'final'
public class ZoneController {
    private final ZoneService zoneService;        // Inmutable
    private final CreatureService creatureService; // Inmutable
}
```
*Nota:* En Spring Boot (v4.3+), cuando una clase posee un único constructor, Spring inyecta las dependencias automáticamente sin requerir `@Autowired`.

---

## 2. Marco Teórico e Investigación de Herramientas

1. **Spring Boot (v3.4+):** Framework líder en el ecosistema Java para el desarrollo de microservicios y aplicaciones web. Aporta autoconfiguración (*Convention over Configuration*), métricas de producción y un contenedor de inversión de control (IoC) y de inyección de dependencias (DI).
2. **Java 25 (LTS) & Java Records:** Se utilizaron Java Records como DTOs (Data Transfer Objects). Al ser clases inmutables, transparentes y concisas con constructores canónicos automáticos, eliminan el código redundante (*boilerplate*) y protegen a la API contra ataques de asignación masiva (*Mass Assignment Vulnerabilities*).
3. **Spring Data JPA & Hibernate:** Capa de abstracción sobre la persistencia relacional que reduce la necesidad de escribir sentencias JDBC manuales. Permite mapeo objeto-relacional (ORM) y creación dinámica de consultas mediante palabras clave o JPQL.
4. **Bean Validation (Hibernate Validator):** Estándar de Java (JSR-380) para definir restricciones declarativas (`@NotBlank`, `@PositiveOrZero`, `@Min`, `@Max`, `@NotNull`) directamente sobre los DTOs, desacoplando la validación estructural de la lógica de negocio.
5. **Project Lombok:** Librería de procesamiento de anotaciones en tiempo de compilación. Se empleó selectivamente (`@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`) evitando explícitamente `@Data` para prevenir la recursión infinita en relaciones bidireccionales.
6. **MySQL:** Motor de base de datos relacional transaccional (ACID) robusto, garantizando integridad referencial mediante claves foráneas (`zone_id` referenciando a `zones(id)`).
7. **Apache Maven:** Gestor de construcción y dependencias que automatiza el ciclo de vida del software (compilación, pruebas unitarias, empaquetado JAR).

---

## 3. Planificación del Proyecto y Gestión de Versiones con Gitflow

El equipo adoptó la estrategia **Gitflow** para garantizar un flujo de trabajo paralelo, ordenado y trazable:

### 3.1. Estructura de Ramas
- `main`: Rama de producción que almacena versiones estables y probadas del sistema.
- `develop`: Rama de integración donde convergen las nuevas características desarrolladas por cada integrante.
- `feature/*`: Ramas secundarias de corta duración creadas para implementar tareas puntuales:
  - `feature/setup-project`: Configuración de dependencias Maven, propiedades de MySQL y Docker Compose.
  - `feature/creature-crud`: Creación de la entidad `Creature`, repositorio, servicio y controlador inicial.
  - `feature/zone-crud`: Extensión para `Zone`, relación `@ManyToOne`/`@OneToMany` y validación de capacidad.
  - `feature/n-plus-1-optimization`: Optimización de consultas JPQL con `JOIN FETCH` y proyecciones.
  - `feature/global-exception-handling`: Implementación del `@RestControllerAdvice` y DTO `ErrorResponse`.

### 3.2. Proceso de Integración y Entrega
1. Cada desarrollador trabajó exclusivamente en su rama `feature/*` correspondiente.
2. Al culminar la funcionalidad, se abrieron **Pull Requests (PR)** hacia la rama `develop` para revisión de código por pares.
3. Tras superar las pruebas unitarias y de integración, se realizó el PR final de `develop` hacia la rama `main`, asignando al docente **`soldash`** como revisor oficial en GitHub.

---

## 4. Análisis Detallado del Problema de las N+1 Consultas

### 4.1. Definición del Problema
El problema de las $N+1$ consultas se suscita cuando un framework ORM ejecuta **1 consulta SQL inicial** para obtener un conjunto de $N$ entidades padre, y posteriormente ejecuta **$N$ consultas SQL adicionales** (una por cada registro) para cargar la entidad relacionada cuando dicha relación se encuentra configurada con carga diferida (`FetchType.LAZY`).

### 4.2. Manifestación en el Zoológico (Antes de la Optimización)
En la relación `@ManyToOne` entre `Creature` y `Zone`:
Al ejecutar `GET /api/creatures`, el repositorio ejecutaba un `findAll()` estándar:
```sql
-- 1. Consulta inicial para obtener las N criaturas:
SELECT c.id, c.name, c.species, c.size, c.danger_level, c.health_status, c.zone_id FROM creatures c;
```
Posteriormente, al iterar la lista para construir el DTO `CreatureResponse` y extraer el nombre de la zona (`creature.getZone().getName()`), Hibernate detectaba que la entidad `Zone` no estaba cargada en el contexto de persistencia y disparaba **$N$ consultas adicionales**:
```sql
-- Consultas 2 a N+1 (1 consulta por cada criatura listada):
Hibernate: SELECT z.id, z.name, z.description, z.capacity FROM zones z WHERE z.id = 1;
Hibernate: SELECT z.id, z.name, z.description, z.capacity FROM zones z WHERE z.id = 2;
Hibernate: SELECT z.id, z.name, z.description, z.capacity FROM zones z WHERE z.id = 1;
...
Hibernate: SELECT z.id, z.name, z.description, z.capacity FROM zones z WHERE z.id = N;
```
*Impacto:* Para 100 criaturas, se producían **101 viajes de ida y vuelta (round-trips) a la base de datos**, generando alta latencia y sobrecarga en el pool de conexiones.

### 4.3. Solución Implementada (Después de la Optimización)
Para erradicar definitivamente este problema se aplicaron dos técnicas senior:

#### A. En Criaturas: `JOIN FETCH`
En `CreatureRepository`, se sobreescribió la consulta de listado mediante JPQL explícito:
```java
@Query("SELECT c FROM Creature c LEFT JOIN FETCH c.zone")
List<Creature> findAllWithZone();
```
**Traza SQL resultante en Hibernate (`spring.jpa.show-sql=true`):**
```sql
-- Consulta ÚNICA optimizada:
Hibernate: 
    select
        c1_0.id,
        c1_0.danger_level,
        c1_0.health_status,
        c1_0.name,
        c1_0.size,
        c1_0.species,
        z1_0.id,
        z1_0.capacity,
        z1_0.description,
        z1_0.name 
    from
        creatures c1_0 
    left join
        zones z1_0 
            on z1_0.id=c1_0.zone_id;
```
*Resultado:* **1 sola consulta SQL**, independientemente de si existen 10 o 10,000 criaturas.

#### B. En Zonas: Proyección Directa con `COUNT()` Agrupado
Para obtener el campo `creatureCount` sin disparar consultas adicionales al recorrer la colección `List<Creature> creatures`, se diseñó una proyección directa al record `ZoneResponse`:
```java
@Query("""
    SELECT new com.javeriana.zoofantastico.dto.ZoneResponse(
        z.id, z.name, z.description, z.capacity, COUNT(c)
    )
    FROM Zone z
    LEFT JOIN z.creatures c
    GROUP BY z.id, z.name, z.description, z.capacity
""")
List<ZoneResponse> findAllWithCreatureCount();
```
*Resultado:* Toda la agregación se computa eficientemente en el motor de base de datos MySQL en una única operación.

---

## 5. Pruebas y Validación de Criterios de Aceptación (Evidencia HTTP)

### Caso 1: Crear Zona Exitosamente (HTTP 201 Created)
- **Endpoint:** `POST /api/zones`
- **Request Body:**
  ```json
  {
    "name": "Laguna de Sirenas",
    "description": "Hábitat acuático de alta humedad",
    "capacity": 2
  }
  ```
- **Response (HTTP 201 Created):**
  ```json
  {
    "id": 1,
    "name": "Laguna de Sirenas",
    "description": "Hábitat acuático de alta humedad",
    "capacity": 2,
    "creatureCount": 0
  }
  ```

### Caso 2: Validación Bean Validation en Criaturas (HTTP 400 Bad Request)
- **Regla:** `size >= 0` y `dangerLevel` entre 1 y 10.
- **Endpoint:** `POST /api/creatures`
- **Request Body:**
  ```json
  {
    "name": "Dragón Colacuerno",
    "species": "Dragón",
    "size": -3.5,
    "dangerLevel": 12,
    "healthStatus": "healthy",
    "zoneId": 1
  }
  ```
- **Response (HTTP 400 Bad Request):**
  ```json
  {
    "timestamp": "2026-09-21T10:15:30.123",
    "status": 400,
    "error": "Bad Request",
    "message": "Error de validación en los campos enviados",
    "validationErrors": {
      "size": "El tamaño debe ser mayor o igual a 0",
      "dangerLevel": "El nivel de peligrosidad máximo permitido es 10"
    }
  }
  ```

### Caso 3: Violación de Regla de Capacidad de Zona (HTTP 409 Conflict)
- **Regla:** No se puede asociar una criatura a una zona que ya alcanzó su capacidad máxima.
- **Contexto:** La zona 1 tiene capacidad para 2 criaturas y ya tiene 2 asignadas.
- **Endpoint:** `POST /api/creatures`
- **Response (HTTP 409 Conflict):**
  ```json
  {
    "timestamp": "2026-09-21T10:18:45.678",
    "status": 409,
    "error": "Conflict",
    "message": "La zona 'Laguna de Sirenas' (ID: 1) ha alcanzado su capacidad máxima de 2 criatura(s). No es posible asignar nuevas criaturas."
  }
  ```

### Caso 4: Eliminación de Criatura en Estado Crítico (HTTP 409 Conflict)
- **Regla:** No se permite eliminar una criatura si su `healthStatus` es `"critical"`.
- **Endpoint:** `DELETE /api/creatures/2`
- **Response (HTTP 409 Conflict):**
  ```json
  {
    "timestamp": "2026-09-21T10:20:12.450",
    "status": 409,
    "error": "Conflict",
    "message": "Operación rechazada: No se permite eliminar la criatura 'Fénix Dorado' porque su estado de salud es crítico ('critical'). Debe recibir atención veterinaria previa."
  }
  ```

### Caso 5: Eliminación de Zona con Criaturas Asignadas (HTTP 409 Conflict)
- **Regla:** Solo se permite eliminar una zona si no contiene criaturas asignadas.
- **Endpoint:** `DELETE /api/zones/1`
- **Response (HTTP 409 Conflict):**
  ```json
  {
    "timestamp": "2026-09-21T10:22:05.110",
    "status": 409,
    "error": "Conflict",
    "message": "No se permite eliminar la zona 'Laguna de Sirenas' (ID: 1) porque tiene 2 criatura(s) mágica(s) asignada(s). Reubique o elimine las criaturas antes de eliminar la zona."
  }
  ```

### Caso 6: Consulta de Criaturas de una Zona Específica (HTTP 200 OK) - Criterio Evaluado en Sustentación
- **Requerimiento:** El docente solicitó en la sustentación oral listar en Postman todas las criaturas que pertenecen a una zona determinada.
- **Resolución Implementada:**
  1. **Enfoque Principal (`GET /api/zones/1`):** Retorna la zona completa con su capacidad, `creatureCount` y el array embebido `creatures` mapeado a `CreatureResponse`.
  2. **Sub-recurso Directo (`GET /api/zones/1/creatures`):** Retorna el arreglo plano de criaturas.
  3. **Filtro (`GET /api/creatures?zoneId=1`):** Filtra criaturas por zona.
- **Optimización SQL:** Ejecuta `findByZoneIdWithZone(1L)` con `LEFT JOIN FETCH c.zone` en `CreatureRepository`, garantizando **1 sola consulta SQL** sin ciclos.
- **Response de Zona con Criaturas en Postman (`GET /api/zones/1`):**
  ```json
  {
    "id": 1,
    "name": "Montañas Nubladas",
    "description": "Hábitat rocoso de gran altura",
    "capacity": 5,
    "creatureCount": 2,
    "creatures": [
      {
        "id": 1,
        "name": "Dragón Colacuerno Húngaro",
        "species": "Dragón",
        "size": 15.5,
        "dangerLevel": 9,
        "healthStatus": "healthy",
        "zoneId": 1,
        "zoneName": "Montañas Nubladas"
      },
      {
        "id": 4,
        "name": "Fénix Carmesí",
        "species": "Fénix",
        "size": 1.2,
        "dangerLevel": 4,
        "healthStatus": "healthy",
        "zoneId": 1,
        "zoneName": "Montañas Nubladas"
      }
    ]
  }
  ```

---

## 6. Desafíos Enfrentados y Soluciones Aplicadas

1. **Ciclos Infinitos en toString y Serialización JSON:**
   - *Problema:* El uso inicial de `@Data` en las entidades provocaba llamadas circulares y excepciones de desbordamiento de pila.
   - *Solución:* Reemplazo de `@Data` por anotaciones granulares (`@Getter`, `@Setter`, `@NoArgsConstructor`) y desacoplamiento absoluto mediante DTOs inmutables con **Java Records**.
2. **Resolución Definitiva: Visualización de Criaturas de una Zona en Postman:**
   - *Problema:* Durante la sustentación inicial, al consultar `GET /api/zones/{id}` no se mostraba el arreglo de criaturas para evitar ciclos infinitos y N+1.
   - *Solución:* Se actualizó `ZoneResponse` para incluir `List<CreatureResponse> creatures` mediante desacoplamiento DTO inmutable y se implementó `getZoneById` en `ZoneServiceImpl` con `findByZoneIdWithZone` (`JOIN FETCH`). Adicionalmente se crearon los endpoints `GET /api/zones/{id}/creatures` y `GET /api/creatures?zoneId={id}`. De esta manera, el requerimiento en Postman quedó resuelto al 100% de forma segura y sin impacto de rendimiento.
3. **Evitar Errores 500 Inesperados ante Reglas de Negocio:**
   - *Problema:* Lanzar `IllegalStateException` genérico por defecto producía respuestas HTTP 500 (Internal Server Error), lo cual es semánticamente incorrecto para violaciones lógicas provocadas por el usuario.
   - *Solución:* Creación de `BusinessRuleException` interceptada en `@RestControllerAdvice` para mapear consistentemente a HTTP 409 (Conflict).
4. **Mantenimiento de Transaccionalidad:**
   - *Solución:* Se aplicó `@Transactional(readOnly = true)` a nivel de clase de servicio y `@Transactional` explícito en los métodos de escritura (create, update, delete) para optimizar el rendimiento del gestor de transacciones y garantizar aislamiento ACID.

---

## 7. Reflexión y Aprendizajes

La implementación de este proyecto demostró que la **arquitectura en 3 capas** no es un mero requisito formal, sino un estándar indispensable en proyectos empresariales reales:
- Permite que la capa web cambie sin impactar las reglas de negocio.
- Facilita la realización de pruebas unitarias aisladas con mocks (Mockito) sin necesidad de levantar servidores ni bases de datos.
- Proporciona predictibilidad y trazabilidad al depurar cuellos de botella de rendimiento como el problema $N+1$.

---

## 8. Declaración sobre el Uso de Inteligencia Artificial

En cumplimiento con la directriz de honestidad académica de la guía de laboratorio:

- **Herramientas de IA consultadas:** Antigravity / Gemini.
- **Prompts y consultas realizadas:**
  - *"Desarrollar el código completo y la estructura para el sistema CRUD 'Zoo de Mascotas Fantásticas' en Spring Boot y Java 25 con arquitectura en 3 capas, Lombok sin @Data, DTOs con records y mitigación del problema N+1".*
- **Aportes de la IA:**
  - Generación del esqueleto inicial (*scaffolding*) de clases, DTOs y configuración de `pom.xml`.
- **Validaciones, Decisiones y Correcciones tomadas por el equipo:**
  - El equipo validó la semántica de los códigos de estado HTTP (garantizando 409 en salud crítica y límites de capacidad en lugar de 500).
  - Se verificó que las consultas JPQL contaran con `LEFT JOIN FETCH` y que el mapeador no invocara getters perezosos fuera de la transacción.
  - Se añadieron pruebas unitarias automatizadas con Mockito para asegurar la cobertura de todas las ramas lógicas condicionales.
- **Declaración:** Todos los integrantes del equipo comprenden la totalidad de las clases, anotaciones y consultas entregadas y están capacitados para sustentarlas técnicamente.

---

## 9. Guía Rápida de Defensa Técnica para la Sustentación Individual

| Pregunta Típica del Docente | Respuesta Técnica Rigurosa |
| :--- | :--- |
| **¿Cómo inyectan dependencias y por qué no usaron `@Autowired` en los atributos?** | *"Profesor, usamos Inyección por Constructor con `@RequiredArgsConstructor` de Lombok y atributos `private final`. No usamos inyección por campo (`@Autowired`) porque es un antipatrón: rompe la inmutabilidad, acopla el código al contenedor e impide pruebas unitarias aisladas sin reflexión. Por constructor garantizamos inmutabilidad, fail-fast en arranque y testabilidad pura con Mockito."* |
| **¿Por qué `GET /api/zones` no devuelve todas las criaturas anidadas en el JSON?** | *"Para prevenir recursión infinita (`StackOverflowError`) por el ciclo bidireccional entre Zone y Creature, evitar `LazyInitializationException` y eliminar sobrecostos N+1. La zona devuelve el conteo `creatureCount` calculado en SQL, y el detalle de animales se consulta en el sub-recurso RESTful `GET /api/zones/{id}/creatures` optimizado con `JOIN FETCH`."* |
| **¿Cómo solucionaron el problema de las N+1 consultas?** | *"En `CreatureRepository` sobreescribimos las consultas con JPQL usando `LEFT JOIN FETCH c.zone`. Esto hace que Hibernate ejecute un único `SELECT` relacional con `LEFT JOIN`, trayendo las criaturas y su zona en un único viaje a la base de datos (round-trip)."* |
| **¿Cómo manejan los errores sin retornar error 500?** | *"Con un interceptor global `@RestControllerAdvice` (`GlobalExceptionHandler`). Excepciones de negocio como `BusinessRuleException` o `ResourceNotFoundException` se capturan y retornan respuestas semánticas con códigos HTTP 409 (Conflict) y 404 (Not Found) junto con un payload uniforme `ErrorResponse`."* |
