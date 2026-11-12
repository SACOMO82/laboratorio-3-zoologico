package com.javeriana.zoofantastico.service.impl;

import com.javeriana.zoofantastico.dto.CreatureResponse;
import com.javeriana.zoofantastico.dto.ZoneRequest;
import com.javeriana.zoofantastico.dto.ZoneResponse;
import com.javeriana.zoofantastico.entity.Zone;
import com.javeriana.zoofantastico.exception.BusinessRuleException;
import com.javeriana.zoofantastico.exception.ResourceNotFoundException;
import com.javeriana.zoofantastico.repository.CreatureRepository;
import com.javeriana.zoofantastico.repository.ZoneRepository;
import com.javeriana.zoofantastico.service.ZoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementación de la lógica de negocio para la gestión de Zonas.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ZoneServiceImpl implements ZoneService {

    private final ZoneRepository zoneRepository;
    private final CreatureRepository creatureRepository;

    @Override
    @Transactional
    public ZoneResponse createZone(ZoneRequest request) {
        Zone zone = Zone.builder()
            .name(request.name().trim())
            .description(request.description() != null ? request.description().trim() : null)
            .capacity(request.capacity())
            .build();

        Zone savedZone = zoneRepository.save(zone);
        return ZoneResponse.fromEntity(savedZone, 0L);
    }

    @Override
    public List<ZoneResponse> getAllZones() {
        // Ejecuta una única consulta SQL optimizada con GROUP BY y COUNT para evitar N+1
        return zoneRepository.findAllWithCreatureCount();
    }

    @Override
    public ZoneResponse getZoneById(Long id) {
        Zone zone = getZoneEntityById(id);
        List<CreatureResponse> creatures = creatureRepository.findByZoneIdWithZone(id)
            .stream()
            .map(CreatureResponse::fromEntity)
            .toList();
        return ZoneResponse.fromEntity(zone, creatures.size(), creatures);
    }

    @Override
    @Transactional
    public ZoneResponse updateZone(Long id, ZoneRequest request) {
        Zone zone = getZoneEntityById(id);
        long currentCreatureCount = creatureRepository.countByZoneId(id);

        // Regla de Negocio: No se puede reducir la capacidad por debajo del número de criaturas actuales
        if (request.capacity() < currentCreatureCount) {
            throw new BusinessRuleException(
                "La nueva capacidad (" + request.capacity() + ") no puede ser menor a la cantidad " +
                "de criaturas actualmente asignadas a la zona (" + currentCreatureCount + ")."
            );
        }

        zone.setName(request.name().trim());
        zone.setDescription(request.description() != null ? request.description().trim() : null);
        zone.setCapacity(request.capacity());

        Zone updatedZone = zoneRepository.save(zone);
        return ZoneResponse.fromEntity(updatedZone, currentCreatureCount);
    }

    @Override
    @Transactional
    public void deleteZone(Long id) {
        Zone zone = getZoneEntityById(id);

        // Regla de Negocio: No se permite eliminar si la zona tiene criaturas asignadas
        long assignedCreatures = creatureRepository.countByZoneId(id);
        if (assignedCreatures > 0) {
            throw new BusinessRuleException(
                "No se permite eliminar la zona '" + zone.getName() + "' (ID: " + id + ") " +
                "porque tiene " + assignedCreatures + " criatura(s) mágica(s) asignada(s). " +
                "Reubique o elimine las criaturas antes de eliminar la zona."
            );
        }

        zoneRepository.delete(zone);
    }

    @Override
    public Zone getZoneEntityById(Long id) {
        return zoneRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Zona no encontrada con ID: " + id));
    }
}
