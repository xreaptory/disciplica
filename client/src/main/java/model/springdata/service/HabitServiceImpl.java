package model.springdata.service;

import model.domain.model.Completion;
import model.domain.model.Habit;
import model.domain.model.User;
import model.springdata.repository.CompletionRepository;
import model.springdata.repository.HabitRepository;
import model.springdata.repository.UserSpringRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Umsetzung von {@link HabitService} auf Basis von Spring Data JPA.
 * <p>
 * Das Abschließen einer Gewohnheit läuft als einzelne Transaktion: Erfüllung
 * speichern, Gewohnheit als erledigt markieren und dem Benutzer Erfahrung und
 * Gold gutschreiben.
 */
@Service
public class HabitServiceImpl implements HabitService {
    private final HabitRepository habitRepository;
    private final UserSpringRepository userRepository;
    private final CompletionRepository completionRepository;

    /**
     * Erzeugt den Dienst mit den benötigten Repositories.
     *
     * @param habitRepository      Zugriff auf Gewohnheiten
     * @param userRepository       Zugriff auf Benutzer
     * @param completionRepository Zugriff auf Erfüllungen
     */
    public HabitServiceImpl(HabitRepository habitRepository,
                            UserSpringRepository userRepository,
                            CompletionRepository completionRepository) {
        this.habitRepository = habitRepository;
        this.userRepository = userRepository;
        this.completionRepository = completionRepository;
    }

    /**
     * Schließt eine Gewohnheit ab: berechnet die Belohnung, vermerkt die
     * Erfüllung, markiert die Gewohnheit als erledigt und schreibt dem
     * Benutzer Erfahrung und Gold gut.
     *
     * @param habitId die Kennung der Gewohnheit
     * @param quality die Qualität der Erfüllung
     * @return die abgeschlossene Gewohnheit
     * @throws HabitServiceException wenn die Gewohnheit nicht existiert oder
     *                               keinem Benutzer gehört
     */
    @Override
    @Transactional(timeout = 10, rollbackFor = {Exception.class, HabitServiceException.class})
    public Habit completeHabit(Long habitId, int quality) {
        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new HabitServiceException("Habit not found: " + habitId));
        User user = habit.getUser();
        if (user == null) {
            throw new HabitServiceException("Habit has no owning user: " + habitId);
        }

        int xpAward = calculateXpAward(habit, quality);
        int goldAward = calculateGoldAward(habit, quality);

        Completion completion = new Completion(LocalDateTime.now(), quality, xpAward);
        habit.addCompletion(completion);
        completionRepository.save(completion);

        habit.complete();
        user.awardXpAndGold(xpAward, goldAward);

        userRepository.save(user);
        habitRepository.save(habit);
        return habit;
    }

    /**
     * Liefert die Gewohnheiten eines Benutzers mit einer bestimmten Häufigkeit.
     *
     * @param user      der Benutzer
     * @param frequency die gesuchte Häufigkeit
     * @return die passenden Gewohnheiten
     */
    @Override
    @Transactional(readOnly = true)
    public List<Habit> getHabitsByUserAndFrequency(User user, String frequency) {
        return habitRepository.findByUserAndFrequency(user, frequency);
    }

    /**
     * {@return alle als erfüllt markierten Gewohnheiten}
     */
    @Override
    @Transactional(readOnly = true)
    public List<Habit> getCompletedHabits() {
        return habitRepository.findByCompletedTrue();
    }

    /**
     * Liefert die Gewohnheiten eines Benutzers mit den längsten Serien.
     *
     * @param user der Benutzer
     * @return die nach Serie sortierten Gewohnheiten
     */
    @Override
    @Transactional(readOnly = true)
    public List<Habit> getTopHabitsByStreak(User user) {
        return habitRepository.findTopHabitsByStreak(user);
    }

    /**
     * Berechnet die Erfahrungspunkte für das Abschließen einer Gewohnheit
     * abhängig von Schwierigkeit, Qualität und Serie.
     *
     * @param habit   die Gewohnheit
     * @param quality die Qualität der Erfüllung
     * @return die verdienten Erfahrungspunkte (mindestens 0)
     */
    private int calculateXpAward(Habit habit, int quality) {
        int base = switch (habit.getDifficulty()) {
            case "hard" -> 20;
            case "easy" -> 8;
            default -> 12;
        };
        return Math.max(0, base + (quality * 5) + (habit.getStreak() * 2));
    }

    /**
     * Berechnet das Gold für das Abschließen einer Gewohnheit abhängig von
     * Schwierigkeit und Qualität.
     *
     * @param habit   die Gewohnheit
     * @param quality die Qualität der Erfüllung
     * @return das verdiente Gold (mindestens 0)
     */
    private int calculateGoldAward(Habit habit, int quality) {
        int base = switch (habit.getDifficulty()) {
            case "hard" -> 5;
            case "easy" -> 2;
            default -> 3;
        };
        return Math.max(0, base + quality);
    }
}
