package com.disciplica;

import java.util.List;
import java.util.Optional;

/**
 * Generic, type-safe repository contract for any {@link Trackable} entity.
 *
 * @param <T> the entity type, must extend {@link Trackable}
 */
public interface Repository<T extends Trackable> {

    /**
     * Persists the given entity (insert or update).
     *
     * @param entity the entity to save
     * @throws InvalidHabitException if the entity is invalid or null
     */
    void save(T entity) throws InvalidHabitException;

    /**
     * Finds an entity by its unique name.
     *
     * @param name the name to search for
     * @return an {@link Optional} containing the entity, or empty if not found
     */
    Optional<T> findByName(String name);

    /**
     * Returns all stored entities.
     *
     * @return a list of all entities
     */
    List<T> findAll();

    /**
     * Deletes an entity by its unique name.
     *
     * @param name the name of the entity to delete
     * @throws HabitNotFoundException if no entity with the given name exists
     */
    void delete(String name) throws HabitNotFoundException;
}

