package com.disciplica.server.task;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.disciplica.server.security.CurrentUser;
import com.disciplica.shared.task.CreateTaskRequest;
import com.disciplica.shared.task.TaskDto;
import com.disciplica.shared.task.UpdateTaskRequest;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    private final TaskService taskService;
    private final CurrentUser currentUser;

    public TaskController(TaskService taskService, CurrentUser currentUser) {
        this.taskService = taskService;
        this.currentUser = currentUser;
    }

    @GetMapping
    public List<TaskDto> list(Authentication authentication) {
        return taskService.list(currentUser.requireUserId(authentication));
    }

    @PostMapping
    public TaskDto create(Authentication authentication, @Valid @RequestBody CreateTaskRequest request) {
        return taskService.create(currentUser.requireUserId(authentication), request);
    }

    @PatchMapping("/{id}")
    public TaskDto update(Authentication authentication,
                          @PathVariable UUID id,
                          @Valid @RequestBody UpdateTaskRequest request) {
        return taskService.update(currentUser.requireUserId(authentication), id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(Authentication authentication, @PathVariable UUID id) {
        taskService.delete(currentUser.requireUserId(authentication), id);
    }

    @PostMapping("/{id}/complete")
    public TaskDto complete(Authentication authentication, @PathVariable UUID id) {
        return taskService.complete(currentUser.requireUserId(authentication), id);
    }
}
