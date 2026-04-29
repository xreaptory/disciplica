package model.domain.repository;

import model.domain.contract.Trackable;
import model.domain.exception.HabitNotFoundException;
import model.domain.exception.InvalidHabitException;
import java.util.List;
import java.util.Optional;

public interface Repository<T extends Trackable> {
    void save(T entity) throws InvalidHabitException;
    Optional<T> findByName(String name);
    List<T> findAll();
    void delete(String name) throws HabitNotFoundException;
}



