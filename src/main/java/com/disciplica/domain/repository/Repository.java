package com.disciplica.domain.repository;

import com.disciplica.domain.contract.Trackable;
import com.disciplica.domain.exception.HabitNotFoundException;
import com.disciplica.domain.exception.InvalidHabitException;
import java.util.List;
import java.util.Optional;

public interface Repository<T extends Trackable> {
    void save(T entity) throws InvalidHabitException;
    Optional<T> findByName(String name);
    List<T> findAll();
    void delete(String name) throws HabitNotFoundException;
}



