package com.javeriana.zoofantastico.service;

import com.javeriana.zoofantastico.dto.ZoneRequest;
import com.javeriana.zoofantastico.dto.ZoneResponse;
import com.javeriana.zoofantastico.entity.Zone;

import java.util.List;

/**
 * Contrato de servicio para la gestión de Zonas del Zoológico.
 */
public interface ZoneService {

    ZoneResponse createZone(ZoneRequest request);

    List<ZoneResponse> getAllZones();

    ZoneResponse getZoneById(Long id);

    ZoneResponse updateZone(Long id, ZoneRequest request);

    void deleteZone(Long id);

    Zone getZoneEntityById(Long id);
}
