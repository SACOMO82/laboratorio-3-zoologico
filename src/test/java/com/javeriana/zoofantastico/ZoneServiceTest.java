package com.javeriana.zoofantastico;

import com.javeriana.zoofantastico.dto.ZoneRequest;
import com.javeriana.zoofantastico.dto.ZoneResponse;
import com.javeriana.zoofantastico.entity.Creature;
import com.javeriana.zoofantastico.entity.Zone;
import com.javeriana.zoofantastico.exception.BusinessRuleException;
import com.javeriana.zoofantastico.exception.ResourceNotFoundException;
import com.javeriana.zoofantastico.repository.CreatureRepository;
import com.javeriana.zoofantastico.repository.ZoneRepository;
import com.javeriana.zoofantastico.service.impl.ZoneServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ZoneServiceTest {

    @Mock
    private ZoneRepository zoneRepository;

    @Mock
    private CreatureRepository creatureRepository;

    @InjectMocks
    private ZoneServiceImpl zoneService;

    private Zone mockZone;

    @BeforeEach
    void setUp() {
        mockZone = Zone.builder()
                .id(1L)
                .name("Laguna de Sirenas")
                .description("Hábitat acuático")
                .capacity(5)
                .build();
    }

    // ==========================================
    // 1. CREATE ZONE
    // ==========================================

    @Test
    @DisplayName("Debe crear una zona exitosamente")
    void shouldCreateZoneSuccessfully() {
        ZoneRequest request = new ZoneRequest("Laguna de Sirenas", "Hábitat acuático", 5);

        when(zoneRepository.save(any(Zone.class))).thenReturn(mockZone);

        ZoneResponse response = zoneService.createZone(request);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Laguna de Sirenas", response.name());
        verify(zoneRepository, times(1)).save(any(Zone.class));
    }

    // ==========================================
    // 2. GET ZONE BY ID
    // ==========================================

    @Test
    @DisplayName("Debe consultar la zona por ID y retornar la lista de criaturas asociadas")
    void shouldReturnZoneWithCreaturesWhenGettingById() {
        Creature mockCreature = Creature.builder()
                .id(10L)
                .name("Sirena Encantada")
                .species("Sirena")
                .size(1.6)
                .dangerLevel(3)
                .healthStatus("healthy")
                .zone(mockZone)
                .build();

        when(zoneRepository.findById(1L)).thenReturn(Optional.of(mockZone));
        when(creatureRepository.findByZoneIdWithZone(1L)).thenReturn(List.of(mockCreature));

        ZoneResponse response = zoneService.getZoneById(1L);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Laguna de Sirenas", response.name());
        assertEquals(1, response.creatureCount());
        assertNotNull(response.creatures());
        assertEquals(1, response.creatures().size());
        assertEquals("Sirena Encantada", response.creatures().get(0).name());
        verify(zoneRepository, times(1)).findById(1L);
        verify(creatureRepository, times(1)).findByZoneIdWithZone(1L);
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException si la zona no existe al buscar por ID")
    void shouldThrowExceptionWhenZoneNotFoundById() {
        when(zoneRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                zoneService.getZoneById(99L)
        );

        verify(zoneRepository, times(1)).findById(99L);
        verify(creatureRepository, never()).findByZoneIdWithZone(anyLong());
    }

    // ==========================================
    // 3. UPDATE ZONE
    // ==========================================

    @Test
    @DisplayName("Debe actualizar una zona exitosamente si la nueva capacidad es válida")
    void shouldUpdateZoneSuccessfully() {
        ZoneRequest updateRequest = new ZoneRequest("Laguna Profunda", "Hábitat acuático renovado", 10);

        when(zoneRepository.findById(1L)).thenReturn(Optional.of(mockZone));
        when(creatureRepository.countByZoneId(1L)).thenReturn(2L); // 2 criaturas actuales < 10 capacidad
        when(zoneRepository.save(any(Zone.class))).thenReturn(mockZone);


        ZoneResponse response = zoneService.updateZone(1L, updateRequest);

        assertNotNull(response);
        verify(zoneRepository, times(1)).save(any(Zone.class));
    }

    @Test
    @DisplayName("Debe rechazar la actualización si la nueva capacidad es menor al número de criaturas actuales")
    void shouldThrowExceptionWhenNewCapacityIsLowerThanCreaturesCount() {
        ZoneRequest updateRequest = new ZoneRequest("Laguna de Sirenas", "Hábitat acuático", 2);

        when(zoneRepository.findById(1L)).thenReturn(Optional.of(mockZone));
        when(creatureRepository.countByZoneId(1L)).thenReturn(4L); // 4 criaturas actuales > 2 capacidad

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () ->
                zoneService.updateZone(1L, updateRequest)
        );

        assertTrue(ex.getMessage().contains("no puede ser menor"));
        verify(zoneRepository, never()).save(any(Zone.class));
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException al intentar actualizar una zona inexistente")
    void shouldThrowExceptionWhenUpdatingNonExistentZone() {
        ZoneRequest updateRequest = new ZoneRequest("Zona Fantasma", "Descripción", 5);

        when(zoneRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                zoneService.updateZone(99L, updateRequest)
        );

        verify(zoneRepository, never()).save(any(Zone.class));
    }

    // ==========================================
    // 4. DELETE ZONE
    // ==========================================

    @Test
    @DisplayName("Debe rechazar la eliminación si la zona contiene criaturas asignadas")
    void shouldThrowExceptionWhenDeletingZoneWithCreatures() {
        when(zoneRepository.findById(1L)).thenReturn(Optional.of(mockZone));
        when(creatureRepository.countByZoneId(1L)).thenReturn(3L); // Tiene 3 criaturas asignadas

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () ->
                zoneService.deleteZone(1L)
        );

        assertTrue(ex.getMessage().contains("tiene 3 criatura(s)"));
        verify(zoneRepository, never()).delete(any(Zone.class));
    }

    @Test
    @DisplayName("Debe eliminar la zona exitosamente si no tiene criaturas asignadas")
    void shouldDeleteZoneWhenEmpty() {
        when(zoneRepository.findById(1L)).thenReturn(Optional.of(mockZone));
        when(creatureRepository.countByZoneId(1L)).thenReturn(0L); // 0 criaturas

        assertDoesNotThrow(() -> zoneService.deleteZone(1L));
        verify(zoneRepository, times(1)).delete(mockZone);
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException al intentar eliminar una zona que no existe")
    void shouldThrowExceptionWhenDeletingNonExistentZone() {
        when(zoneRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                zoneService.deleteZone(99L)
        );

        verify(zoneRepository, never()).delete(any(Zone.class));
    }
}