package com.javeriana.zoofantastico.service.impl;

import com.javeriana.zoofantastico.dto.CreatureRequest;
import com.javeriana.zoofantastico.dto.CreatureResponse;
import com.javeriana.zoofantastico.entity.Creature;
import com.javeriana.zoofantastico.entity.Zone;
import com.javeriana.zoofantastico.exception.BusinessRuleException;
import com.javeriana.zoofantastico.exception.ResourceNotFoundException;
import com.javeriana.zoofantastico.repository.CreatureRepository;
import com.javeriana.zoofantastico.service.CreatureService;
import com.javeriana.zoofantastico.service.ZoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementación de la lógica de negocio para Criaturas Mágicas.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CreatureServiceImpl implements CreatureService {

    private final CreatureRepository creatureRepository;
    private final ZoneService zoneService;

    @Override
    @Transactional
    public CreatureResponse createCreature(CreatureRequest request) {
        Zone zone = zoneService.getZoneEntityById(request.zoneId());

        // Regla de Negocio: Validar capacidad máxima de la zona
        long currentCount = creatureRepository.countByZoneId(zone.getId());
        if (currentCount >= zone.getCapacity()) {
            throw new BusinessRuleException(
                "La zona '" + zone.getName() + "' (ID: " + zone.getId() + ") ha alcanzado su capacidad máxima " +
                "de " + zone.getCapacity() + " criatura(s). No es posible asignar nuevas criaturas."
            );
        }

        Creature creature = Creature.builder()
            .name(request.name().trim())
            .species(request.species().trim())
            .size(request.size())
            .dangerLevel(request.dangerLevel())
            .healthStatus(request.healthStatus().trim())
            .zone(zone)
            .build();

        Creature saved = creatureRepository.save(creature);
        return CreatureResponse.fromEntity(saved);
    }

    @Override
    public List<CreatureResponse> getAllCreatures() {
        // Uso de findAllWithZone() con JOIN FETCH para erradicar el problema N+1
        return creatureRepository.findAllWithZone()
            .stream()
            .map(CreatureResponse::fromEntity)
            .toList();
    }

    @Override
    public CreatureResponse getCreatureById(Long id) {
        Creature creature = creatureRepository.findByIdWithZone(id)
            .orElseThrow(() -> new ResourceNotFoundException("Criatura mágica no encontrada con ID: " + id));

        return CreatureResponse.fromEntity(creature);
    }

    @Override
    @Transactional
    public CreatureResponse updateCreature(Long id, CreatureRequest request) {
        Creature creature = creatureRepository.findByIdWithZone(id)
            .orElseThrow(() -> new ResourceNotFoundException("Criatura mágica no encontrada con ID: " + id));

        // Regla de Negocio: Si cambia de zona, verificar capacidad de la nueva zona de destino
        if (!creature.getZone().getId().equals(request.zoneId())) {
            Zone newZone = zoneService.getZoneEntityById(request.zoneId());
            long countInNewZone = creatureRepository.countByZoneId(newZone.getId());

            if (countInNewZone >= newZone.getCapacity()) {
                throw new BusinessRuleException(
                    "No se puede reubicar la criatura a la zona '" + newZone.getName() + "' porque ha alcanzado su capacidad máxima (" +
                    newZone.getCapacity() + " criatura(s))."
                );
            }
            creature.setZone(newZone);
        }

        creature.setName(request.name().trim());
        creature.setSpecies(request.species().trim());
        creature.setSize(request.size());
        creature.setDangerLevel(request.dangerLevel());
        creature.setHealthStatus(request.healthStatus().trim());

        Creature updated = creatureRepository.save(creature);
        return CreatureResponse.fromEntity(updated);
    }

    @Override
    @Transactional
    public void deleteCreature(Long id) {
        Creature creature = creatureRepository.findByIdWithZone(id)
            .orElseThrow(() -> new ResourceNotFoundException("Criatura mágica no encontrada con ID: " + id));

        // Regla de Negocio: No se puede eliminar una criatura en estado de salud crítico
        if ("critical".equalsIgnoreCase(creature.getHealthStatus())) {
            throw new BusinessRuleException(
                "Operación rechazada: No se permite eliminar la criatura '" + creature.getName() +
                "' porque su estado de salud es crítico ('critical'). Debe recibir atención veterinaria previa."
            );
        }

        creatureRepository.delete(creature);
    }

    @Override
    public List<CreatureResponse> getCreaturesByZone(Long zoneId) {
        // Valida que la zona exista (arroja 404 si no existe)
        zoneService.getZoneEntityById(zoneId);

        // Consulta optimizada con JOIN FETCH: 1 sola llamada SQL a base de datos
        return creatureRepository.findByZoneIdWithZone(zoneId)
            .stream()
            .map(CreatureResponse::fromEntity)
            .toList();
    }
}
