package com.javeriana.zoofantastico.repository;

import com.javeriana.zoofantastico.dto.ZoneResponse;
import com.javeriana.zoofantastico.entity.Zone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad Zone.
 * 
 * NOTA DE OPTIMIZACIÓN SENIOR (Resolución N+1 en Zonas):
 * Al listar las zonas junto con la cantidad de criaturas asignadas (creatureCount),
 * si iteráramos cada zona y llamáramos a `zone.getCreatures().size()`, Hibernate dispararía
 * 1 consulta para traer las zonas y N consultas para cargar la colección de criaturas de cada zona (Problema N+1).
 * 
 * Para resolverlo de forma óptima, se implementa una consulta JPQL con proyección directa al
 * Java Record DTO `ZoneResponse`, ejecutando un `LEFT JOIN` y `COUNT(c)` agrupado en una
 * ÚNICA consulta SQL en la base de datos.
 */
@Repository
public interface ZoneRepository extends JpaRepository<Zone, Long> {

    /**
     * Consulta optimizada que devuelve todas las zonas con el conteo de criaturas asignadas en 1 sola consulta SQL.
     */
    @Query("""
        SELECT new com.javeriana.zoofantastico.dto.ZoneResponse(
            z.id, z.name, z.description, z.capacity, COUNT(c)
        )
        FROM Zone z
        LEFT JOIN z.creatures c
        GROUP BY z.id, z.name, z.description, z.capacity
    """)
    List<ZoneResponse> findAllWithCreatureCount();

    /**
     * Consulta que obtiene el detalle de una zona específica por ID junto con su creatureCount.
     */
    @Query("""
        SELECT new com.javeriana.zoofantastico.dto.ZoneResponse(
            z.id, z.name, z.description, z.capacity, COUNT(c)
        )
        FROM Zone z
        LEFT JOIN z.creatures c
        WHERE z.id = :id
        GROUP BY z.id, z.name, z.description, z.capacity
    """)
    Optional<ZoneResponse> findZoneResponseById(@Param("id") Long id);

    boolean existsByName(String name);
}
