package model.springdata.repository;

import model.domain.model.Habit;
import model.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HabitRepository extends JpaRepository<Habit, Long>, HabitRepositoryCustom {
    List<Habit> findByUserAndFrequency(User user, String frequency);

    List<Habit> findByCompletedTrue();

    @Query("select h from Habit h where h.user = :user order by h.streak desc, h.name asc")
    List<Habit> findTopHabitsByStreak(@Param("user") User user);
}
