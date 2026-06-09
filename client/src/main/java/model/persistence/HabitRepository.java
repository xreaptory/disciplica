package model.persistence;

import model.domain.model.Habit;
import model.domain.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Schnittstelle für den Datenzugriff auf Gewohnheiten.
 * <p>
 * Legt fest, welche Operationen zum Speichern, Suchen, Ändern und Löschen von
 * Gewohnheiten angeboten werden, unabhängig von der konkreten Umsetzung
 * (z.&nbsp;B. SQLite oder im Speicher).
 */
public interface HabitRepository {

    /**
     * Speichert eine neue Gewohnheit.
     *
     * @param habit die zu speichernde Gewohnheit
     */
    void save(Habit habit);

    /**
     * Sucht eine Gewohnheit anhand ihrer Kennung.
     *
     * @param id die Kennung der Gewohnheit
     * @return die Gewohnheit oder ein leeres {@link Optional}, falls nicht
     *         vorhanden
     */
    Optional<Habit> findById(Long id);

    /**
     * Liefert alle Gewohnheiten eines Benutzers.
     *
     * @param user der Benutzer
     * @return die Liste seiner Gewohnheiten
     */
    List<Habit> findByUser(User user);

    /**
     * Aktualisiert eine bestehende Gewohnheit.
     *
     * @param habit die zu aktualisierende Gewohnheit
     */
    void update(Habit habit);

    /**
     * Löscht eine Gewohnheit anhand ihrer Kennung.
     *
     * @param id die Kennung der zu löschenden Gewohnheit
     */
    void delete(Long id);

    /**
     * Liefert alle gespeicherten Gewohnheiten.
     *
     * @return die Liste aller Gewohnheiten
     */
    List<Habit> findAll();

    /**
     * Schließt eine Gewohnheit ab und vermerkt die zugehörige Erfüllung samt
     * Belohnung.
     *
     * @param habitId  die Kennung der Gewohnheit
     * @param quality  die Qualität der Erfüllung
     * @param xpGain   die verdienten Erfahrungspunkte
     * @param goldGain das verdiente Gold
     */
    void completeHabit(Long habitId, int quality, int xpGain, int goldGain);
}
