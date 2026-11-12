package com.javeriana.zoofantastico.dto;

import jakarta.validation.constraints.*;

/**
 * Record DTO para la creación y actualización de Criaturas Mágicas.
 * Cumple estrictamente con las reglas de validación:
 * - size >= 0
 * - dangerLevel entre 1 y 10
 */
public record CreatureRequest(
    @NotBlank(message = "El nombre de la criatura es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres")
    String name,

    @NotBlank(message = "La especie de la criatura es obligatoria")
    @Size(max = 100, message = "La especie no puede exceder los 100 caracteres")
    String species,

    @NotNull(message = "El tamaño es obligatorio")
    @PositiveOrZero(message = "El tamaño debe ser mayor o igual a 0")
    Double size,

    @NotNull(message = "El nivel de peligrosidad es obligatorio")
    @Min(value = 1, message = "El nivel de peligrosidad mínimo permitido es 1")
    @Max(value = 10, message = "El nivel de peligrosidad máximo permitido es 10")
    Integer dangerLevel,

    @NotBlank(message = "El estado de salud es obligatorio")
    @Size(max = 50, message = "El estado de salud no puede exceder los 50 caracteres")
    String healthStatus,

    @NotNull(message = "El ID de la zona a la que pertenece la criatura es obligatorio")
    Long zoneId
) {}
