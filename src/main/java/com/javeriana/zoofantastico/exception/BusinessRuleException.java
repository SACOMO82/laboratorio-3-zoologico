package com.javeriana.zoofantastico.exception;

/**
 * Excepción lanzada cuando una operación viola una regla de negocio del sistema, tales como:
 * - Intentar eliminar una criatura con healthStatus = "critical".
 * - Intentar asignar una criatura a una zona que ya alcanzó su capacidad máxima.
 * - Intentar eliminar una zona que aún contiene criaturas asignadas.
 * 
 * Mapeada a HTTP 409 (Conflict).
 */
public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }
}
