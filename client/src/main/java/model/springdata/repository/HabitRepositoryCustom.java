package model.springdata.repository;

import model.domain.model.Habit;
import model.domain.model.User;

import java.util.List;

/**
 * Erweiterungsschnittstelle für eigene Abfragen, die sich nicht durch
 * abgeleitete Methodennamen ausdrücken lassen.
 * <p>
 * Die Umsetzung erfolgt in {@link HabitRepositoryImpl}.
 */
public interface HabitRepositoryCustom {

    /**
     * Durchsucht die Beschreibungen der Gewohnheiten eines Benutzers nach
     * einem Begriff (per nativer Datenbankabfrage).
     *
     * @param term der Suchbegriff
     * @param user der Benutzer
     * @return die passenden Gewohnheiten, absteigend nach Serie sortiert
     */
    List<Habit> searchByDescriptionNative(String term, User user);
}
