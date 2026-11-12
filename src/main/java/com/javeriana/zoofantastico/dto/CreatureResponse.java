package com.javeriana.zoofantastico.dto;

import com.javeriana.zoofantastico.entity.Creature;

/**
 * Record DTO de respuesta para Creature.
 * Desacopla la entidad JPA de la capa web evitando ciclos de serialización JSON.
 */
public record CreatureResponse(
    Long id,
    String name,
    String species,
    double size,
    int dangerLevel,
    String healthStatus,
    Long zoneId,
    String zoneName
) {
    public static CreatureResponse fromEntity(Creature creature) {
        Long zoneId = (creature.getZone() != null) ? creature.getZone().getId() : null;
        String zoneName = (creature.getZone() != null) ? creature.getZone().getName() : null;

        return new CreatureResponse(
            creature.getId(),
            creature.getName(),
            creature.getSpecies(),
            creature.getSize(),
            creature.getDangerLevel(),
            creature.getHealthStatus(),
            zoneId,
            zoneName
        );
    }
}
