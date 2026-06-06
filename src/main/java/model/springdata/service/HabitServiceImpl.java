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

@Service
public class HabitServiceImpl implements HabitService {
    private final HabitRepository habitRepository;
    private final UserSpringRepository userRepository;
    private final CompletionRepository completionRepository;

    public HabitServiceImpl(HabitRepository habitRepository,
                            UserSpringRepository userRepository,
                            CompletionRepository completionRepository) {
        this.habitRepository = habitRepository;
        this.userRepository = userRepository;
        this.completionRepository = completionRepository;
    }

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

    @Override
    @Transactional(readOnly = true)
    public List<Habit> getHabitsByUserAndFrequency(User user, String frequency) {
        return habitRepository.findByUserAndFrequency(user, frequency);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Habit> getCompletedHabits() {
        return habitRepository.findByCompletedTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Habit> getTopHabitsByStreak(User user) {
        return habitRepository.findTopHabitsByStreak(user);
    }

    private int calculateXpAward(Habit habit, int quality) {
        int base = switch (habit.getDifficulty()) {
            case "hard" -> 20;
            case "easy" -> 8;
            default -> 12;
        };
        return Math.max(0, base + (quality * 5) + (habit.getStreak() * 2));
    }

    private int calculateGoldAward(Habit habit, int quality) {
        int base = switch (habit.getDifficulty()) {
            case "hard" -> 5;
            case "easy" -> 2;
            default -> 3;
        };
        return Math.max(0, base + quality);
    }
}
