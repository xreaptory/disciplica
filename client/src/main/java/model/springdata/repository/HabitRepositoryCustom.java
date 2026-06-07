package model.springdata.repository;

import model.domain.model.Habit;
import model.domain.model.User;

import java.util.List;

public interface HabitRepositoryCustom {
    List<Habit> searchByDescriptionNative(String term, User user);
}
