package com.javeriana.zoofantastico.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Record DTO para la creación y actualización de Zonas.
 * Inmutable y auto-validable mediante Bean Validation en Java 25.
 */
public record ZoneRequest(
    @NotBlank(message = "El nombre de la zona es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    String name,

    @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
    String description,

    @NotNull(message = "La capacidad de la zona es obligatoria")
    @Min(value = 1, message = "La capacidad mínima de una zona debe ser de al menos 1 criatura")
    Integer capacity
) {}
