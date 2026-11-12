package com.javeriana.zoofantastico;

import com.javeriana.zoofantastico.dto.CreatureRequest;
import com.javeriana.zoofantastico.dto.CreatureResponse;
import com.javeriana.zoofantastico.entity.Creature;
import com.javeriana.zoofantastico.entity.Zone;
import com.javeriana.zoofantastico.exception.BusinessRuleException;
import com.javeriana.zoofantastico.exception.ResourceNotFoundException;
import com.javeriana.zoofantastico.repository.CreatureRepository;
import com.javeriana.zoofantastico.service.ZoneService;
import com.javeriana.zoofantastico.service.impl.CreatureServiceImpl;
import org.junit.jupiter.api.BeforeEach; //Librerias para que se prueba correctamente las pruebas unitarias
import org.junit.jupiter.api.DisplayName; //Junit 5 Framework para trabajar
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks; //  Mockito: Herramienta que nos ayudara a crear pruebas para no toca directamente la base de datos de sql
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) //Le da la orden para que se ejecuten todos los mocks antes de que se hagan pruebas
class CreatureServiceTest {

    @Mock // Crea objetos simulados donde nos se conectan directamente a la base sql.
    private CreatureRepository creatureRepository;

    @Mock
    private ZoneService zoneService;

    @InjectMocks //Instancia a la clase CreatureServiceImpl para que se inyecten los dos mocks que creamos
    private CreatureServiceImpl creatureService;

    private Zone mockZone;
    private Creature mockCreature;

    @BeforeEach //Se ejecuta antes de cada test y deja lista una zona y una criatura vinculada a esa zona
    void setUp() {
        mockZone = Zone.builder()
                .id(1L)
                .name("Bosque Prohibido")
                .description("Zona de criaturas místicas")
                .capacity(2)
                .build();

        mockCreature = Creature.builder()
                .id(100L)
                .name("Buckbeak")
                .species("Hipogrifo")
                .size(3.5)
                .dangerLevel(7)
                .healthStatus("healthy")
                .zone(mockZone)
                .build();
    }

    // ==========================================
    // 1. CREATE CREATURE
    // ==========================================

    @Test
    @DisplayName("Debe crear una criatura exitosamente si la zona tiene capacidad disponible")
    void shouldCreateCreatureSuccessfully() {
        CreatureRequest request = new CreatureRequest(
                "Buckbeak", "Hipogrifo", 3.5, 7, "healthy", 1L
        );

        when(zoneService.getZoneEntityById(1L)).thenReturn(mockZone); //Aca pregunta por la zona de la criatura y devuelve la zona
        when(creatureRepository.countByZoneId(1L)).thenReturn(1L); //Cuenta cuantas criaturas hay en la zona
        when(creatureRepository.save(any(Creature.class))).thenReturn(mockCreature); //Aca devuelve a la criatura guardada

        CreatureResponse response = creatureService.createCreature(request);

        assertNotNull(response); //Las respuestas no sean nulas
        assertEquals("Buckbeak", response.name()); //Y que aca se espera que los datpos councidan con esto
        assertEquals(1L, response.zoneId());
        verify(creatureRepository, times(1)).save(any(Creature.class)); //que service guarde una vez a la criatura
    }

    @Test
    @DisplayName("Debe lanzar BusinessRuleException si la zona alcanzó su capacidad máxima")
    void shouldThrowExceptionWhenZoneCapacityReached() {
        CreatureRequest request = new CreatureRequest(
                "Smaug", "Dragón", 15.0, 10, "healthy", 1L
        );

        when(zoneService.getZoneEntityById(1L)).thenReturn(mockZone);
        when(creatureRepository.countByZoneId(1L)).thenReturn(2L); //Aca asiganamos a que la capacidad de la zona ya este llena

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () ->
                creatureService.createCreature(request) //Revisamos que directamente se lanze la ecepcion
        );

        assertTrue(ex.getMessage().contains("capacidad máxima"));
        verify(creatureRepository, never()).save(any(Creature.class)); //Aca al estar lleno aseguramos que no se llame al metodo de guardar criatura
    }

    // ==========================================
    // 2. GET CREATURE BY ID
    // ==========================================

    @Test
    @DisplayName("Debe obtener una criatura por ID exitosamente")
    void shouldGetCreatureByIdSuccessfully() {
        when(creatureRepository.findByIdWithZone(100L)).thenReturn(Optional.of(mockCreature)); //Se simula que el repositorio encontro la criatura en la base de datos

        CreatureResponse response = creatureService.getCreatureById(100L);

        assertNotNull(response);
        assertEquals(100L, response.id());
        assertEquals("Buckbeak", response.name());
        assertEquals("Hipogrifo", response.species());
        verify(creatureRepository, times(1)).findByIdWithZone(100L); //que los datos coincidan
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException si la criatura no existe al buscar por ID")
    void shouldThrowExceptionWhenCreatureNotFoundById() {
        when(creatureRepository.findByIdWithZone(999L)).thenReturn(Optional.empty()); //Simula que se busco la criatura y que no se encontro

        assertThrows(ResourceNotFoundException.class, () ->
                creatureService.getCreatureById(999L) //Aseguramos que se lance el error adecuado al no encontrar una criatura
        );

        verify(creatureRepository, times(1)).findByIdWithZone(999L);
    }

    // ==========================================
    // 3. UPDATE CREATURE
    // ==========================================

    @Test
    @DisplayName("Debe actualizar una criatura exitosamente manteniendo la misma zona")
    void shouldUpdateCreatureSuccessfully() {
        CreatureRequest updateRequest = new CreatureRequest(
                "Buckbeak Renovado", "Hipogrifo Real", 4.0, 8, "healthy", 1L //Se crea esta criatura
        );

        when(creatureRepository.findByIdWithZone(100L)).thenReturn(Optional.of(mockCreature)); //Aca assumimos que la criatura se le quieren cambiar al id 100
        when(creatureRepository.save(any(Creature.class))).thenReturn(mockCreature);

        CreatureResponse response = creatureService.updateCreature(100L, updateRequest); //Aca directamente actualiza el id a 100

        assertNotNull(response);
        verify(creatureRepository, times(1)).save(mockCreature); //Aca se guardan las modificaciones
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException al actualizar si la criatura no existe")
    void shouldThrowExceptionWhenUpdatingNonExistentCreature() {
        CreatureRequest updateRequest = new CreatureRequest(
                "Fantasma", "Espectro", 1.0, 1, "healthy", 1L
        );

        when(creatureRepository.findByIdWithZone(999L)).thenReturn(Optional.empty()); //simulamos que la ciratura no existe en el repository

        assertThrows(ResourceNotFoundException.class, () ->
                creatureService.updateCreature(999L, updateRequest)
        );

        verify(creatureRepository, never()).save(any(Creature.class)); //Asegura que no se guarde ninguna criatura que no este
    }

    // ==========================================
    // 4. DELETE CREATURE
    // ==========================================

    @Test
    @DisplayName("Debe rechazar la eliminación si la criatura está en estado crítico ('critical')")
    void shouldThrowExceptionWhenDeletingCreatureInCriticalState() {
        mockCreature.setHealthStatus("critical"); //Se simula que la criatura este en estado critico

        when(creatureRepository.findByIdWithZone(100L)).thenReturn(Optional.of(mockCreature));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () ->
                creatureService.deleteCreature(100L) //Aca verificamos que al momento de intertar eliminarla se lance la exepcion correcta
        );

        assertTrue(ex.getMessage().contains("crítico ('critical')"));
        verify(creatureRepository, never()).delete(any(Creature.class)); //Aca aseguramos que no se elimine esa criatura
    }

    @Test
    @DisplayName("Debe eliminar la criatura exitosamente si su salud no es crítica")
    void shouldDeleteCreatureWhenHealthIsNotCritical() {
        mockCreature.setHealthStatus("healthy"); //Aca asumimos que la criatura NO esta en estado critico

        when(creatureRepository.findByIdWithZone(100L)).thenReturn(Optional.of(mockCreature));

        assertDoesNotThrow(() -> creatureService.deleteCreature(100L)); //Aca borramos y verificamosd que se haya eliminado correctamente
        verify(creatureRepository, times(1)).delete(mockCreature);
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException al intentar eliminar una criatura inexistente")
    void shouldThrowExceptionWhenDeletingNonExistentCreature() {
        when(creatureRepository.findByIdWithZone(999L)).thenReturn(Optional.empty()); //Aca simulamos una criatura que no esta en el repository

        assertThrows(ResourceNotFoundException.class, () ->
                creatureService.deleteCreature(999L) //Aca lanzamos la ecpecion correcta
        );

        verify(creatureRepository, never()).delete(any(Creature.class)); //Y que no se pueda borrar
    }

    // ==========================================
    // 5. GET CREATURES BY ZONE
    // ==========================================

    @Test
    @DisplayName("Debe consultar y retornar todas las criaturas pertenecientes a una zona específica")
    void shouldReturnCreaturesByZone() {
        when(zoneService.getZoneEntityById(1L)).thenReturn(mockZone);
        when(creatureRepository.findByZoneIdWithZone(1L)).thenReturn(List.of(mockCreature)); //Aca retorna una lista de todas las criaturas que pertenezca a esa zona

        List<CreatureResponse> results = creatureService.getCreaturesByZone(1L);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Buckbeak", results.get(0).name());
        assertEquals("Bosque Prohibido", results.get(0).zoneName());
        verify(zoneService, times(1)).getZoneEntityById(1L);
        verify(creatureRepository, times(1)).findByZoneIdWithZone(1L); //Aca retorna todos los datos de las criaturas que esten en la zona
    }
}
