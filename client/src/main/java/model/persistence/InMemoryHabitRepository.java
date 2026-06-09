package model.persistence;

import model.domain.model.Habit;
import model.domain.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Im Speicher gehaltene Umsetzung von {@link HabitRepository}.
 * <p>
 * Hält die Gewohnheiten lediglich in einer Liste und eignet sich vor allem
 * für Tests oder einen Betrieb ohne Datenbank. Die Kennung entspricht der
 * Position in der Liste.
 */
public class InMemoryHabitRepository implements HabitRepository {
    private final List<Habit> habits = new ArrayList<>();

    /**
     * Fügt eine Gewohnheit der Liste hinzu.
     *
     * @param habit die zu speichernde Gewohnheit
     */
    @Override
    public void save(Habit habit) {
        habits.add(habit);
    }

    /**
     * Sucht eine Gewohnheit anhand ihrer Listenposition.
     *
     * @param id die Position in der Liste
     * @return die Gewohnheit oder ein leeres {@link Optional}, falls die
     *         Position ungültig ist
     */
    @Override
    public Optional<Habit> findById(Long id) {
        if (id == null || id < 0 || id >= habits.size()) {
            return Optional.empty();
        }
        return Optional.of(habits.get(id.intValue()));
    }

    /**
     * Liefert alle Gewohnheiten (der Benutzer wird hier nicht berücksichtigt).
     *
     * @param user der Benutzer (unbeachtet)
     * @return eine Kopie der Gewohnheitenliste
     */
    @Override
    public List<Habit> findByUser(User user) {
        return new ArrayList<>(habits);
    }

    /**
     * Ersetzt eine vorhandene Gewohnheit durch dieselbe Instanz an gleicher
     * Position.
     *
     * @param habit die zu aktualisierende Gewohnheit
     */
    @Override
    public void update(Habit habit) {
        int index = habits.indexOf(habit);
        if (index >= 0) {
            habits.set(index, habit);
        }
    }

    /**
     * Entfernt die Gewohnheit an der angegebenen Position.
     *
     * @param id die Position der zu löschenden Gewohnheit
     */
    @Override
    public void delete(Long id) {
        findById(id).ifPresent(habits::remove);
    }

    /**
     * {@return eine Kopie aller gespeicherten Gewohnheiten}
     */
    @Override
    public List<Habit> findAll() {
        return new ArrayList<>(habits);
    }

    /**
     * Schließt die angegebene Gewohnheit ab. Belohnungswerte werden in dieser
     * Umsetzung nicht ausgewertet.
     *
     * @param habitId  die Position der Gewohnheit
     * @param quality  die Qualität der Erfüllung (unbeachtet)
     * @param xpGain   die Erfahrungspunkte (unbeachtet)
     * @param goldGain das Gold (unbeachtet)
     * @throws DatabaseException wenn keine Gewohnheit an der Position existiert
     */
    @Override
    public void completeHabit(Long habitId, int quality, int xpGain, int goldGain) {
        Habit habit = findById(habitId).orElseThrow(() -> new DatabaseException("Habit not found"));
        habit.complete();
    }
}
