package com.disciplica;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * File-based implementation of TaskRepository using JSON for persistence.
 * Automatically loads tasks on construction and saves on explicit save operations.
 */
public class FileTaskRepository implements TaskRepository {
    private static final Logger logger = LoggerFactory.getLogger(FileTaskRepository.class);
    private static final String DEFAULT_FILE_PATH = "data/tasks.json";
    
    private final List<AbstractTask> tasks;
    private final ObjectMapper objectMapper;
    private final Path filePath;

    /**
     * Creates a new FileTaskRepository using the default file path.
     * Automatically loads existing tasks if the file exists.
     */
    public FileTaskRepository() {
        this(DEFAULT_FILE_PATH);
    }

    /**
     * Creates a new FileTaskRepository with a custom file path.
     * Automatically loads existing tasks if the file exists.
     *
     * @param filePath the path to the JSON file for persistence
     */
    public FileTaskRepository(String filePath) {
        this.tasks = new ArrayList<>();
        this.filePath = Paths.get(filePath);
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        
        logger.info("Initializing FileTaskRepository with file: {}", filePath);
        
        try {
            List<AbstractTask> loadedTasks = load();
            tasks.addAll(loadedTasks);
            logger.info("Loaded {} task(s) from file", tasks.size());
        } catch (IOException e) {
            logger.info("No existing tasks file found or error loading: {} - Starting fresh", e.getMessage());
        }
    }

    @Override
    public void save(AbstractTask task) throws InvalidHabitException, IOException {
        if (task == null) {
            logger.error("Cannot save a null task");
            throw new InvalidHabitException("Cannot save a null task");
        }
        
        logger.debug("Saving task: {}", task.getName());
        
        // Remove existing task with same name if present
        tasks.removeIf(t -> t.getName().equals(task.getName()));
        tasks.add(task);
        
        // Persist to file
        saveToFile();
        logger.info("Task '{}' saved successfully ({} total)", task.getName(), tasks.size());
    }

    @Override
    public void saveAll(List<AbstractTask> tasksToSave) throws IOException {
        if (tasksToSave == null) {
            logger.warn("saveAll called with null list");
            return;
        }
        
        logger.debug("Saving {} task(s)", tasksToSave.size());
        tasks.clear();
        tasks.addAll(tasksToSave);
        
        // Persist to file
        saveToFile();
        logger.info("Saved {} task(s) successfully", tasks.size());
    }

    @Override
    public List<AbstractTask> load() throws IOException {
        File file = filePath.toFile();
        
        if (!file.exists()) {
            logger.debug("File does not exist: {} - First run", filePath);
            return new ArrayList<>();
        }
        
        logger.debug("Loading tasks from file: {}", filePath);
        
        try {
            TaskList taskList = objectMapper.readValue(file, TaskList.class);
            logger.info("Loaded {} task(s) from file", taskList.tasks.size());
            return taskList.tasks;
        } catch (IOException e) {
            logger.error("Error loading tasks from file: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public Optional<AbstractTask> findById(String id) {
        if (id == null || id.isBlank()) {
            logger.debug("findById called with null/blank id");
            return Optional.empty();
        }
        
        logger.debug("Finding task by id: {}", id);
        return tasks.stream()
                .filter(task -> task.getName().equals(id))
                .findFirst();
    }

    @Override
    public List<AbstractTask> findAll() {
        logger.debug("findAll called, returning {} task(s)", tasks.size());
        return new ArrayList<>(tasks);
    }

    @Override
    public void delete(String id) throws HabitNotFoundException, IOException {
        if (id == null || id.isBlank()) {
            logger.warn("delete called with null/blank id");
            throw new HabitNotFoundException("Cannot delete task with null/blank id");
        }
        
        logger.debug("Deleting task: {}", id);
        
        boolean removed = tasks.removeIf(task -> task.getName().equals(id));
        
        if (!removed) {
            logger.error("Task not found for deletion: {}", id);
            throw new HabitNotFoundException("Task not found: " + id);
        }
        
        // Persist to file
        saveToFile();
        logger.info("Task '{}' deleted successfully ({} remaining)", id, tasks.size());
    }

    /**
     * Internal method to persist the current tasks list to file.
     */
    private void saveToFile() throws IOException {
        // Ensure directory exists
        File file = filePath.toFile();
        File parentDir = file.getParentFile();
        
        if (parentDir != null && !parentDir.exists()) {
            logger.debug("Creating directory: {}", parentDir);
            if (!parentDir.mkdirs()) {
                logger.error("Failed to create directory: {}", parentDir);
                throw new IOException("Failed to create directory: " + parentDir);
            }
        }
        
        logger.debug("Writing {} task(s) to file: {}", tasks.size(), filePath);
        
        TaskList taskList = new TaskList();
        taskList.tasks = new ArrayList<>(tasks);
        
        objectMapper.writeValue(file, taskList);
        logger.debug("Successfully wrote to file");
    }

    /**
     * Wrapper class for JSON serialization/deserialization of task lists.
     * This helps Jackson handle the polymorphic task types properly.
     */
    private static class TaskList {
        public List<AbstractTask> tasks = new ArrayList<>();
    }
}
