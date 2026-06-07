package com.disciplica.shared.task;

import jakarta.validation.constraints.Size;

public record UpdateTaskRequest(
        @Size(max = 120) String title,
        @Size(max = 1000) String description,
        Integer points,
        Boolean completed,
        String category
) {
}
