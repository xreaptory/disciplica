package model.domain.repository;

import model.domain.exception.HabitNotFoundException;
import model.domain.exception.InvalidHabitException;
import model.domain.model.AbstractTask;
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


