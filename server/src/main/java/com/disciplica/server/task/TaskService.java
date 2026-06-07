package com.disciplica.server.task;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.disciplica.server.support.ApiException;
import com.disciplica.shared.task.CreateTaskRequest;
import com.disciplica.shared.task.TaskDto;
import com.disciplica.shared.task.UpdateTaskRequest;

@Service
public class TaskService {
    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<TaskDto> list(UUID userId) {
        return taskRepository.findByUser(userId);
    }

    @Transactional
    public TaskDto create(UUID userId, CreateTaskRequest request) {
        return taskRepository.create(userId, request);
    }

    @Transactional
    public TaskDto update(UUID userId, UUID taskId, UpdateTaskRequest request) {
        return taskRepository.update(userId, taskId, request)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Task not found"));
    }

    @Transactional
    public void delete(UUID userId, UUID taskId) {
        if (!taskRepository.delete(userId, taskId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Task not found");
        }
    }

    @Transactional
    public TaskDto complete(UUID userId, UUID taskId) {
        return taskRepository.complete(userId, taskId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Task not found"));
    }
}
