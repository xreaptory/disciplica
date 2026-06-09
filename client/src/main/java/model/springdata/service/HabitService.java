package model.springdata.service;

import model.domain.model.Habit;
import model.domain.model.User;

import java.util.List;

/**
 * Schnittstelle für die Geschäftslogik rund um Gewohnheiten auf Basis von
 * Spring Data JPA.
 */
public interface HabitService {

    /**
     * Schließt eine Gewohnheit ab, vermerkt die Erfüllung und schreibt dem
     * Benutzer die Belohnung gut.
     *
     * @param habitId die Kennung der Gewohnheit
     * @param quality die Qualität der Erfüllung
     * @return die abgeschlossene Gewohnheit
     */
    Habit completeHabit(Long habitId, int quality);

    /**
     * Liefert die Gewohnheiten eines Benutzers mit einer bestimmten Häufigkeit.
     *
     * @param user      der Benutzer
     * @param frequency die gesuchte Häufigkeit
     * @return die passenden Gewohnheiten
     */
    List<Habit> getHabitsByUserAndFrequency(User user, String frequency);

    /**
     * {@return alle als erfüllt markierten Gewohnheiten}
     */
    List<Habit> getCompletedHabits();

    /**
     * Liefert die Gewohnheiten eines Benutzers mit den längsten Serien.
     *
     * @param user der Benutzer
     * @return die nach Serie sortierten Gewohnheiten
     */
    List<Habit> getTopHabitsByStreak(User user);
}
