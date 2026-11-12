package com.javeriana.zoofantastico.exception;

/**
 * Excepción lanzada cuando un recurso solicitado (Criatura o Zona) no existe en el sistema.
 * Mapeada a HTTP 404 (Not Found).
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
