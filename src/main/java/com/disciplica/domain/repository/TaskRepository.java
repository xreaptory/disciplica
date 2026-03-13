package com.disciplica.domain.repository;

import com.disciplica.domain.exception.HabitNotFoundException;
import com.disciplica.domain.exception.InvalidHabitException;
import com.disciplica.domain.model.AbstractTask;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface TaskRepository {
    void save(AbstractTask task) throws InvalidHabitException, IOException;
    void saveAll(List<AbstractTask> tasks) throws IOException;
    List<AbstractTask> load() throws IOException;
    Optional<AbstractTask> findById(String id);
    List<AbstractTask> findAll();
    void delete(String id) throws HabitNotFoundException, IOException;
}


