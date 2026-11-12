package com.javeriana.zoofantastico.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad JPA que representa una Criatura Mágica en el Zoológico.
 * 
 * NOTA DE ARQUITECTURA SENIOR:
 * Se evita el uso de @Data para proteger contra ciclos de serialización/toString/equals/hashCode
 * con la entidad Zone relacionada.
 */
@Entity
@Table(name = "creature")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Creature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String species;

    @Column(nullable = false)
    private double size;

    @Column(name = "danger_level", nullable = false)
    private int dangerLevel;

    @Column(name = "health_status", nullable = false, length = 50)
    private String healthStatus;

    /**
     * Asociación Many-to-One con la Zona.
     * Se configura con FetchType.LAZY como buena práctica para evitar que JPA
     * ejecute eager joins indiscriminados en todas las consultas por defecto.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id", nullable = false)
    private Zone zone;
}
