package com.javeriana.zoofantastico.controller;

import com.javeriana.zoofantastico.dto.CreatureRequest;
import com.javeriana.zoofantastico.dto.CreatureResponse;
import com.javeriana.zoofantastico.service.CreatureService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de Criaturas Mágicas (/api/creatures).
 */
@RestController
@RequestMapping("/api/creatures")
@RequiredArgsConstructor
public class CreatureController {

    private final CreatureService creatureService;

    @PostMapping
    public ResponseEntity<CreatureResponse> createCreature(@Valid @RequestBody CreatureRequest request) {
        CreatureResponse response = creatureService.createCreature(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<CreatureResponse>> getAllCreatures(
        @RequestParam(required = false) Long zoneId
    ) {
        if (zoneId != null) {
            return ResponseEntity.ok(creatureService.getCreaturesByZone(zoneId));
        }
        return ResponseEntity.ok(creatureService.getAllCreatures());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CreatureResponse> getCreatureById(@PathVariable Long id) {
        return ResponseEntity.ok(creatureService.getCreatureById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CreatureResponse> updateCreature(
        @PathVariable Long id,
        @Valid @RequestBody CreatureRequest request
    ) {
        return ResponseEntity.ok(creatureService.updateCreature(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCreature(@PathVariable Long id) {
        creatureService.deleteCreature(id);
        return ResponseEntity.noContent().build();
    }
}
