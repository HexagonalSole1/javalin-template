package org.devquality.web.validators.groups;

/**
 * Grupos de validación para diferentes escenarios de uso
 */
public class ValidationGroups {

    /**
     * Validaciones básicas - campos obligatorios
     */
    public interface Basic {}

    /**
     * Validaciones para creación de entidades
     */
    public interface Create extends Basic {}

    /**
     * Validaciones para actualización de entidades
     */
    public interface Update extends Basic {}

    /**
     * Validaciones completas - incluye todo
     */
    public interface Complete extends Create, Update {}

    /**
     * Validaciones para admin - reglas más estrictas
     */
    public interface Admin extends Complete {}
}