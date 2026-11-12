package com.javeriana.zoofantastico.service;

import com.javeriana.zoofantastico.dto.CreatureRequest;
import com.javeriana.zoofantastico.dto.CreatureResponse;

import java.util.List;

/**
 * Contrato de servicio para la gestión de Criaturas Mágicas del Zoológico.
 */
public interface CreatureService {

    CreatureResponse createCreature(CreatureRequest request);

    List<CreatureResponse> getAllCreatures();

    CreatureResponse getCreatureById(Long id);

    CreatureResponse updateCreature(Long id, CreatureRequest request);

    void deleteCreature(Long id);

    List<CreatureResponse> getCreaturesByZone(Long zoneId);
}
