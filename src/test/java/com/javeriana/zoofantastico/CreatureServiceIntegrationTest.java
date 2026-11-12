package com.javeriana.zoofantastico;

import com.javeriana.zoofantastico.dto.CreatureRequest;
import com.javeriana.zoofantastico.dto.CreatureResponse;
import com.javeriana.zoofantastico.entity.Creature;
import com.javeriana.zoofantastico.entity.Zone;
import com.javeriana.zoofantastico.exception.BusinessRuleException;
import com.javeriana.zoofantastico.repository.CreatureRepository;
import com.javeriana.zoofantastico.repository.ZoneRepository;
import com.javeriana.zoofantastico.service.CreatureService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest //Aca no se utiliza mock, si se conecta a todas las entidades
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) //Unicamente le permite abrir la base de datos que se coloco en h2 y no sobreescibir o adivinar datos
@Transactional // Todo lo que se haga en test se haga rollback para que no afecte las demas (atomica y aislada)
public class CreatureServiceIntegrationTest {

    @Autowired //Se inyectan componentes reales
    private CreatureService creatureService;

    @Autowired
    private CreatureRepository creatureRepository;

    @Autowired
    private ZoneRepository zoneRepository;

    private Zone persistentZone;

    @BeforeEach
    void setUp() {
        // Limpia tablas e inserta una zona real en la base de datos H2 para cada test
        creatureRepository.deleteAll();
        zoneRepository.deleteAll();

        Zone zone = Zone.builder()
                .name("Valle Olvidado")
                .description("Hábitat de pruebas de integración")
                .capacity(5)
                .build();
        persistentZone = zoneRepository.save(zone); //Se inserta un valor en h2 por cada test
    }

    // 1. Crear criatura y verificar que se haya guardado en la BD
    @Test
    @DisplayName("Integración: Crear criatura y verificar su persistencia real en H2")
    void testCreateCreature_ShouldPersistInDatabase() {
        CreatureRequest request = new CreatureRequest(
                "Unicornio", "Místico", 2.0, 5, "healthy", persistentZone.getId()
        );

        CreatureResponse response = creatureService.createCreature(request);

        assertNotNull(response.id());
        Optional<Creature> foundCreature = creatureRepository.findById(response.id());
        assertTrue(foundCreature.isPresent());
        assertEquals("Unicornio", foundCreature.get().getName());
        assertEquals(5, foundCreature.get().getDangerLevel());
    }

    // 2. Actualizar criatura y verificar los cambios en la BD
    @Test
    @DisplayName("Integración: Actualizar criatura y comprobar cambios en H2")
    void testUpdateCreature_ShouldPersistChangesInDatabase() {
        Creature creature = Creature.builder()
                .name("Grifo")
                .species("Bestia Alada")
                .size(3.0)
                .dangerLevel(6)
                .healthStatus("healthy")
                .zone(persistentZone)
                .build();
        Creature saved = creatureRepository.save(creature);

        CreatureRequest updateRequest = new CreatureRequest(
                "Grifo Imperial", "Bestia Legendaria", 3.8, 8, "healthy", persistentZone.getId()
        );
        creatureService.updateCreature(saved.getId(), updateRequest);

        Optional<Creature> updated = creatureRepository.findById(saved.getId());
        assertTrue(updated.isPresent());
        assertEquals("Grifo Imperial", updated.get().getName());
        assertEquals(8, updated.get().getDangerLevel());
    }

    // 3. Eliminar criatura y confirmar que ya no existe en la BD
    @Test
    @DisplayName("Integración: Eliminar criatura sana y confirmar eliminación en H2")
    void testDeleteCreature_ShouldRemoveFromDatabase() {
        Creature creature = Creature.builder()
                .name("Fénix Dorado")
                .species("Ave Fénix")
                .size(1.5)
                .dangerLevel(4)
                .healthStatus("healthy")
                .zone(persistentZone)
                .build();
        Creature saved = creatureRepository.save(creature);

        creatureService.deleteCreature(saved.getId()); //Se elimina

        Optional<Creature> foundCreature = creatureRepository.findById(saved.getId());
        assertTrue(foundCreature.isEmpty()); //Confirma que ya no esta dentro de la base de datos
    }

    // 4. Regla de negocio de eliminación: rechazar si está en estado crítico
    @Test
    @DisplayName("Integración: Impedir eliminar criatura si su estado es crítico ('critical')")
    void testDeleteCreature_ShouldNotDeleteIfCritical() {
        Creature criticalCreature = Creature.builder()
                .name("Dragón Malherido")
                .species("Dragón")
                .size(12.0)
                .dangerLevel(10)
                .healthStatus("critical")
                .zone(persistentZone)
                .build();
        Creature saved = creatureRepository.save(criticalCreature);

        assertThrows(BusinessRuleException.class, () ->
                creatureService.deleteCreature(saved.getId())
        );

        Optional<Creature> stillExists = creatureRepository.findById(saved.getId());
        assertTrue(stillExists.isPresent());
    }
}