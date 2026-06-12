package com.disciplica.server.task;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.disciplica.server.support.ApiException;
import com.disciplica.server.user.LevelCalculator;
import com.disciplica.server.user.UserRepository;
import com.disciplica.shared.task.CreateTaskRequest;
import com.disciplica.shared.task.DailyActivityDto;
import com.disciplica.shared.task.TaskDto;
import com.disciplica.shared.task.TaskType;
import com.disciplica.shared.task.UpdateTaskRequest;

/**
 * Dienst mit der Geschäftslogik rund um Aufgaben. Reicht die Aufrufe an das
 * {@link TaskRepository} weiter und wirft bei fehlenden Aufgaben einen
 * passenden Fehler.
 */
@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final LevelCalculator levelCalculator;
    private final CronService cronService;

    /**
     * Erzeugt den Dienst mit seinen Abhängigkeiten.
     *
     * @param taskRepository  der Datenbankzugriff auf Aufgaben
     * @param userRepository  der Datenbankzugriff auf Benutzer (für Level/Gold)
     * @param levelCalculator berechnet das Level aus den Erfahrungspunkten
     * @param cronService     führt den täglichen Tageswechsel aus
     */
    public TaskService(TaskRepository taskRepository,
                       UserRepository userRepository,
                       LevelCalculator levelCalculator,
                       CronService cronService) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.levelCalculator = levelCalculator;
        this.cronService = cronService;
    }

    /**
     * Listet alle Aufgaben eines Benutzers auf. Vorher wird – falls ein neuer
     * Tag begonnen hat – der Tageswechsel ausgeführt (Dailies zurücksetzen,
     * Schaden für verpasste Dailies).
     *
     * @param userId die Kennung des Benutzers
     * @return die Liste der Aufgaben
     */
    public List<TaskDto> list(UUID userId) {
        cronService.runIfNeeded(userId);
        return taskRepository.findByUser(userId);
    }

    /**
     * Liefert die tagesweise Aktivität des Benutzers für das Dashboard.
     *
     * @param userId die Kennung des Benutzers
     * @param days   die Anzahl der betrachteten Tage (auf 1..365 begrenzt)
     * @return die Liste der Tagesaktivitäten
     */
    public List<DailyActivityDto> dailyActivity(UUID userId, int days) {
        int bounded = Math.max(1, Math.min(days, 365));
        return taskRepository.dailyActivity(userId, bounded);
    }

    /**
     * Bewertet eine Gewohnheit negativ („−“): Serie sinkt, Lebenspunkte werden
     * abgezogen.
     *
     * @param userId die Kennung des Benutzers
     * @param taskId die Kennung der Aufgabe
     * @return die aktualisierte Aufgabe
     * @throws ApiException wenn die Aufgabe nicht gefunden wird
     */
    @Transactional
    public TaskDto scoreDown(UUID userId, UUID taskId) {
        return taskRepository.scoreDown(userId, taskId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Task not found"));
    }

    /**
     * Kauft eine Belohnung: zieht das Gold (= Punktewert) ab, sofern genügend
     * vorhanden ist.
     *
     * @param userId die Kennung des Benutzers
     * @param taskId die Kennung der Belohnung
     * @return die gekaufte Belohnung
     * @throws ApiException wenn die Aufgabe fehlt, keine Belohnung ist oder das
     *                      Gold nicht ausreicht
     */
    @Transactional
    public TaskDto buyReward(UUID userId, UUID taskId) {
        TaskDto task = taskRepository.findByUserAndId(userId, taskId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Task not found"));
        if (task.type() != TaskType.REWARD) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Task is not a reward");
        }
        if (!userRepository.spendGold(userId, task.points())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Not enough gold");
        }
        return task;
    }

    /**
     * Legt eine neue Aufgabe an.
     *
     * @param userId  die Kennung des Benutzers
     * @param request die Daten der neuen Aufgabe
     * @return die angelegte Aufgabe
     */
    @Transactional
    public TaskDto create(UUID userId, CreateTaskRequest request) {
        return taskRepository.create(userId, request);
    }

    /**
     * Ändert eine bestehende Aufgabe.
     *
     * @param userId  die Kennung des Benutzers
     * @param taskId  die Kennung der Aufgabe
     * @param request die zu ändernden Felder
     * @return die aktualisierte Aufgabe
     * @throws ApiException wenn die Aufgabe nicht gefunden wird
     */
    @Transactional
    public TaskDto update(UUID userId, UUID taskId, UpdateTaskRequest request) {
        return taskRepository.update(userId, taskId, request)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Task not found"));
    }

    /**
     * Löscht eine Aufgabe.
     *
     * @param userId die Kennung des Benutzers
     * @param taskId die Kennung der Aufgabe
     * @throws ApiException wenn die Aufgabe nicht gefunden wird
     */
    @Transactional
    public void delete(UUID userId, UUID taskId) {
        if (!taskRepository.delete(userId, taskId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Task not found");
        }
    }

    /**
     * Schließt eine Aufgabe ab und vergibt die Belohnung.
     *
     * @param userId die Kennung des Benutzers
     * @param taskId die Kennung der Aufgabe
     * @return die abgeschlossene Aufgabe
     * @throws ApiException wenn die Aufgabe nicht gefunden wird
     */
    @Transactional
    public TaskDto complete(UUID userId, UUID taskId) {
        TaskDto task = taskRepository.complete(userId, taskId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Task not found"));
        recomputeLevel(userId);
        return task;
    }

    /**
     * Leitet das Level des Benutzers aus seinen aktuellen Erfahrungspunkten ab
     * und speichert es, falls es sich geändert hat (z.&nbsp;B. ein Levelaufstieg
     * nach dem Abschließen einer Aufgabe).
     *
     * @param userId die Kennung des Benutzers
     */
    private void recomputeLevel(UUID userId) {
        userRepository.findById(userId).ifPresent(user -> {
            int level = levelCalculator.calculateLevel(user.xp());
            if (level == user.level()) {
                return;
            }
            userRepository.updateLevel(userId, level);
            if (level > user.level()) {
                // Levelaufstieg heilt vollständig (wie bei Habitica) und verhindert
                // damit eine Sackgasse bei 0 Lebenspunkten.
                userRepository.restoreHealth(userId);
            }
        });
    }
}
