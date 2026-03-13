package com.disciplica.infrastructure.persistence;

import com.disciplica.domain.exception.HabitNotFoundException;
import com.disciplica.domain.exception.InvalidHabitException;
import com.disciplica.domain.model.AbstractTask;
import com.disciplica.domain.repository.TaskRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import org.slf4j.*;

public class FileTaskRepository implements TaskRepository {
    private static final Logger logger = LoggerFactory.getLogger(FileTaskRepository.class);
    private static final String DEFAULT_FILE_PATH = "data/tasks.json";

    private final List<AbstractTask> tasks;
    private final ObjectMapper objectMapper;
    private final Path filePath;

    public FileTaskRepository() {
        this(DEFAULT_FILE_PATH);
    }

    public FileTaskRepository(String filePath) {
        this.tasks = new ArrayList<>();
        this.filePath = Paths.get(filePath);
        this.objectMapper = createObjectMapper();
        logger.info("Initializing FileTaskRepository with file: {}", filePath);
        loadExistingTasks();
    }

    @Override
    public void save(AbstractTask task) throws InvalidHabitException, IOException {
        validateTask(task);
        upsertTask(task);
        saveToFile();
        logger.info("Task '{}' saved successfully ({} total)", task.getName(), tasks.size());
    }

    @Override
    public void saveAll(List<AbstractTask> tasksToSave) throws IOException {
        if (tasksToSave == null) {
            logger.warn("saveAll called with null list");
            return;
        }
        tasks.clear();
        tasks.addAll(tasksToSave);
        saveToFile();
        logger.info("Saved {} task(s) successfully", tasks.size());
    }

    @Override
    public List<AbstractTask> load() throws IOException {
        File taskFile = filePath.toFile();
        if (!taskFile.exists()) {
            logger.debug("File does not exist: {} - First run", filePath);
            return new ArrayList<>();
        }
        return readTasks(taskFile);
    }

    @Override
    public Optional<AbstractTask> findById(String id) {
        if (id == null || id.isBlank()) {
            logger.debug("findById called with null/blank id");
            return Optional.empty();
        }
        return tasks.stream().filter(task -> task.getName().equals(id)).findFirst();
    }

    @Override
    public List<AbstractTask> findAll() {
        logger.debug("findAll called, returning {} task(s)", tasks.size());
        return new ArrayList<>(tasks);
    }

    @Override
    public void delete(String id) throws HabitNotFoundException, IOException {
        validateDeleteId(id);
        boolean removed = tasks.removeIf(task -> task.getName().equals(id));
        if (!removed) {
            logger.error("Task not found for deletion: {}", id);
            throw new HabitNotFoundException("Task not found: " + id);
        }
        saveToFile();
        logger.info("Task '{}' deleted successfully ({} remaining)", id, tasks.size());
    }

    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        return mapper;
    }

    private void loadExistingTasks() {
        try {
            tasks.addAll(load());
            logger.info("Loaded {} task(s) from file", tasks.size());
        } catch (IOException ioException) {
            logger.info("No existing tasks file found or error loading: {} - Starting fresh",
                    ioException.getMessage());
        }
    }

    private void validateTask(AbstractTask task) throws InvalidHabitException {
        if (task == null) {
            logger.error("Cannot save a null task");
            throw new InvalidHabitException("Cannot save a null task");
        }
    }

    private void upsertTask(AbstractTask task) {
        tasks.removeIf(existingTask -> existingTask.getName().equals(task.getName()));
        tasks.add(task);
    }

    private List<AbstractTask> readTasks(File taskFile) throws IOException {
        try {
            TaskList taskList = objectMapper.readValue(taskFile, TaskList.class);
            logger.info("Loaded {} task(s) from file", taskList.tasks.size());
            return taskList.tasks;
        } catch (IOException ioException) {
            logger.error("Error loading tasks from file: {}", ioException.getMessage(), ioException);
            throw ioException;
        }
    }

    private void validateDeleteId(String id) throws HabitNotFoundException {
        if (id == null || id.isBlank()) {
            logger.warn("delete called with null/blank id");
            throw new HabitNotFoundException("Cannot delete task with null/blank id");
        }
    }

    private void saveToFile() throws IOException {
        File taskFile = filePath.toFile();
        ensureParentDirectoryExists(taskFile);
        TaskList taskList = new TaskList();
        taskList.tasks = new ArrayList<>(tasks);
        objectMapper.writeValue(taskFile, taskList);
        logger.debug("Successfully wrote {} task(s) to {}", tasks.size(), filePath);
    }

    private void ensureParentDirectoryExists(File taskFile) throws IOException {
        File parentDirectory = taskFile.getParentFile();
        if (parentDirectory == null || parentDirectory.exists()) {
            return;
        }
        if (!parentDirectory.mkdirs()) {
            throw new IOException("Failed to create directory: " + parentDirectory);
        }
    }

    private static class TaskList {
        public List<AbstractTask> tasks = new ArrayList<>();
    }
}
