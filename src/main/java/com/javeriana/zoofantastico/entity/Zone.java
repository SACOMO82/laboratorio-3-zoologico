package com.javeriana.zoofantastico.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Entidad JPA que representa una Zona del Zoológico.
 * 
 * NOTA DE ARQUITECTURA SENIOR:
 * Se utilizan deliberadamente @Getter, @Setter y @NoArgsConstructor en lugar de @Data.
 * La anotación @Data genera de forma predeterminada implementaciones de toString(), equals()
 * y hashCode() que inspeccionan todos los campos. Al existir una relación bidireccional
 * (Zone -> creatures -> Creature -> zone -> ...), esto causaría una recursión infinita
 * culminando en un error crítico de desbordamiento de pila (StackOverflowError).
 */
@Entity
@Table(name = "zone")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Zone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private int capacity;

    /**
     * Relación One-to-Many con Creature.
     * La carga es diferida (LAZY) para no traer las criaturas innecesariamente a memoria.
     * No se utiliza CascadeType.REMOVE directo porque una de las reglas de negocio prohíbe
     * eliminar una zona si aún tiene criaturas asignadas.
     */
    @OneToMany(mappedBy = "zone", fetch = FetchType.EAGER)
    @Builder.Default
    private List<Creature> creatures = new ArrayList<>();
}
