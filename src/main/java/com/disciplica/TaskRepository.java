package com.disciplica;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for persisting and retrieving {@link AbstractTask} objects.
 * Provides methods for CRUD operations and file persistence.
 */
public interface TaskRepository {

    /**
     * Saves a task to the repository.
     *
     * @param task the task to save
     * @throws InvalidHabitException if the task is invalid or null
     * @throws IOException if file operations fail
     */
    void save(AbstractTask task) throws InvalidHabitException, IOException;

    /**
     * Saves all tasks to the repository (bulk save).
     *
     * @param tasks the list of tasks to save
     * @throws IOException if file operations fail
     */
    void saveAll(List<AbstractTask> tasks) throws IOException;

    /**
     * Loads all tasks from persistent storage.
     *
     * @return list of all persisted tasks
     * @throws IOException if file operations fail
     */
    List<AbstractTask> load() throws IOException;

    /**
     * Finds a task by its unique ID (name).
     *
     * @param id the task name/ID
     * @return an Optional containing the task, or empty if not found
     */
    Optional<AbstractTask> findById(String id);

    /**
     * Finds all tasks in the repository.
     *
     * @return list of all tasks
     */
    List<AbstractTask> findAll();

    /**
     * Deletes a task by its unique ID (name).
     *
     * @param id the task name/ID to delete
     * @throws HabitNotFoundException if no task with the given ID exists
     * @throws IOException if file operations fail
     */
    void delete(String id) throws HabitNotFoundException, IOException;
}
