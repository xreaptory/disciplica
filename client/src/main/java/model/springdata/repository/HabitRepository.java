package model.springdata.repository;

import model.domain.model.Habit;
import model.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Spring-Data-Repository für {@link Habit Gewohnheiten}.
 * <p>
 * Erbt die üblichen CRUD-Operationen von {@link JpaRepository}, ergänzt
 * abgeleitete Abfragemethoden und bindet über {@link HabitRepositoryCustom}
 * eine eigene Volltextabfrage ein.
 */
public interface HabitRepository extends JpaRepository<Habit, Long>, HabitRepositoryCustom {

    /**
     * Sucht die Gewohnheiten eines Benutzers mit einer bestimmten Häufigkeit.
     *
     * @param user      der Benutzer
     * @param frequency die gesuchte Häufigkeit
     * @return die passenden Gewohnheiten
     */
    List<Habit> findByUserAndFrequency(User user, String frequency);

    /**
     * {@return alle als erfüllt markierten Gewohnheiten}
     */
    List<Habit> findByCompletedTrue();

    /**
     * Liefert die Gewohnheiten eines Benutzers, absteigend nach Serie und
     * anschließend nach Name sortiert.
     *
     * @param user der Benutzer
     * @return die sortierte Liste der Gewohnheiten
     */
    @Query("select h from Habit h where h.user = :user order by h.streak desc, h.name asc")
    List<Habit> findTopHabitsByStreak(@Param("user") User user);
}
