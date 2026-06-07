package com.disciplica.shared.task;

import java.time.Instant;
import java.util.UUID;

public record TaskDto(
        UUID id,
        TaskType type,
        String title,
        String description,
        int points,
        int streak,
        boolean completed,
        String category,
        Instant createdAt,
        Instant updatedAt
) {
}
