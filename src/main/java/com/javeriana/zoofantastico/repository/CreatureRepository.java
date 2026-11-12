package com.javeriana.zoofantastico.repository;

import com.javeriana.zoofantastico.entity.Creature;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad Creature.
 * 
 * =========================================================================================
 * ESTUDIO Y MITIGACIÓN DEL PROBLEMA DE LAS N+1 CONSULTAS (ManyToOne Creature -> Zone):
 * =========================================================================================
 * 1. ¿Cómo se manifiesta el problema N+1?
 *    Si utilizáramos `creatureRepository.findAll()`, Spring Data JPA ejecuta:
 *      Query 1: SELECT c.* FROM creatures c; (Retorna N registros)
 *    Posteriormente, al mapear cada criatura a su DTO `CreatureResponse` e invocar `creature.getZone().getName()`,
 *    el proxy perezoso (Lazy) de Hibernate se ve obligado a disparar una consulta individual por cada zona:
 *      Query 2: SELECT z.* FROM zones z WHERE z.id = ?; (para la criatura 1)
 *      Query 3: SELECT z.* FROM zones z WHERE z.id = ?; (para la criatura 2)
 *      ...
 *      Query N+1: SELECT z.* FROM zones z WHERE z.id = ?; (para la criatura N)
 *    En un sistema con 500 criaturas, esto representaría 501 consultas a la base de datos,
 *    degradando exponencialmente el rendimiento y la latencia de la red.
 * 
 * 2. Solución 1: JOIN FETCH (Utilizada aquí)
 *    Mediante `SELECT c FROM Creature c LEFT JOIN FETCH c.zone`, le indicamos a Hibernate
 *    que genere un único `LEFT OUTER JOIN` en SQL, trayendo todos los datos de Creature y Zone
 *    en una ÚNICA llamada a MySQL.
 * 
 * 3. Solución 2: @EntityGraph
 *    También se puede declarar `@EntityGraph(attributePaths = {"zone"})` sobre el método,
 *    lo que instruye a JPA a anular dinámicamente el FetchType.LAZY convirtiéndolo en EAGER
 *    exclusivamente para esa consulta específica mediante un join.
 * =========================================================================================
 */
@Repository
public interface CreatureRepository extends JpaRepository<Creature, Long> {

    /**
     * Consulta optimizada con JOIN FETCH que resuelve el problema N+1 al listar criaturas.
     */
    @Query("SELECT c FROM Creature c LEFT JOIN FETCH c.zone")
    List<Creature> findAllWithZone();

    /**
     * Alternativa con @EntityGraph para obtener todas las criaturas con su zona en 1 sola consulta:
     */
    @EntityGraph(attributePaths = {"zone"})
    @Query("SELECT c FROM Creature c WHERE c.species = :species")
    List<Creature> findBySpeciesWithZone(@Param("species") String species);

    /**
     * Consulta con JOIN FETCH para obtener una criatura por ID con su zona asociada en 1 sola consulta.
     */
    @Query("SELECT c FROM Creature c LEFT JOIN FETCH c.zone WHERE c.id = :id")
    Optional<Creature> findByIdWithZone(@Param("id") Long id);

    /**
     * Consulta optimizada con JOIN FETCH para listar todas las criaturas de una zona específica en 1 sola consulta SQL.
     */
    @Query("SELECT c FROM Creature c LEFT JOIN FETCH c.zone WHERE c.zone.id = :zoneId")
    List<Creature> findByZoneIdWithZone(@Param("zoneId") Long zoneId);

    /**
     * Cuenta la cantidad de criaturas actualmente asignadas a una zona específica.
     * Utilizado para validar la regla de negocio de capacidad máxima de la zona.
     */
    long countByZoneId(Long zoneId);

    /**
     * Verifica si existen criaturas asignadas a una zona.
     * Utilizado para validar la regla de negocio antes de eliminar una zona.
     */
    boolean existsByZoneId(Long zoneId);
}
