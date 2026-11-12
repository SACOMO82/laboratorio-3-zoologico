package com.javeriana.zoofantastico.dto;

import com.javeriana.zoofantastico.entity.Zone;
import java.util.List;

/**
 * Record DTO de respuesta para Zone.
 * Proporciona el campo calculado creatureCount y la lista de criaturas asociadas.
 * Evita ciclos de serialización JSON al utilizar CreatureResponse inmutable.
 */
public record ZoneResponse(
    Long id,
    String name,
    String description,
    int capacity,
    long creatureCount,
    List<CreatureResponse> creatures
) {
    /**
     * Constructor de compatibilidad para proyecciones JPQL que calculan COUNT(c).
     */
    public ZoneResponse(Long id, String name, String description, int capacity, long creatureCount) {
        this(id, name, description, capacity, creatureCount, List.of());
    }

    public static ZoneResponse fromEntity(Zone zone, long creatureCount) {
        return new ZoneResponse(
            zone.getId(),
            zone.getName(),
            zone.getDescription(),
            zone.getCapacity(),
            creatureCount,
            List.of()
        );
    }

    public static ZoneResponse fromEntity(Zone zone, long creatureCount, List<CreatureResponse> creatures) {
        return new ZoneResponse(
            zone.getId(),
            zone.getName(),
            zone.getDescription(),
            zone.getCapacity(),
            creatureCount,
            creatures != null ? creatures : List.of()
        );
    }
}
