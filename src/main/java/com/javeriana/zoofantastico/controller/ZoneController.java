package com.javeriana.zoofantastico.controller;

import com.javeriana.zoofantastico.dto.CreatureResponse;
import com.javeriana.zoofantastico.dto.ZoneRequest;
import com.javeriana.zoofantastico.dto.ZoneResponse;
import com.javeriana.zoofantastico.service.CreatureService;
import com.javeriana.zoofantastico.service.ZoneService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de Zonas del Zoológico (/api/zones).
 * Utiliza Inyección de Dependencias por Constructor mediante @RequiredArgsConstructor.
 */
@RestController
@RequestMapping("/api/zones")
@RequiredArgsConstructor
public class ZoneController {

    private final ZoneService zoneService;
    private final CreatureService creatureService;

    @PostMapping
    public ResponseEntity<ZoneResponse> createZone(@Valid @RequestBody ZoneRequest request) {
        ZoneResponse response = zoneService.createZone(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ZoneResponse>> getAllZones() {
        return ResponseEntity.ok(zoneService.getAllZones());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ZoneResponse> getZoneById(@PathVariable Long id) {
        return ResponseEntity.ok(zoneService.getZoneById(id));
    }

    /**
     * Endpoint REST sub-recurso para consultar todas las criaturas de una zona en específico:
     * GET /api/zones/{id}/creatures
     */
    @GetMapping("/{id}/creatures")
    public ResponseEntity<List<CreatureResponse>> getCreaturesByZone(@PathVariable Long id) {
        return ResponseEntity.ok(creatureService.getCreaturesByZone(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ZoneResponse> updateZone(
        @PathVariable Long id,
        @Valid @RequestBody ZoneRequest request
    ) {
        return ResponseEntity.ok(zoneService.updateZone(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteZone(@PathVariable Long id) {
        zoneService.deleteZone(id);
        return ResponseEntity.noContent().build();
    }
}
