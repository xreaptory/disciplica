package com.disciplica.server.task;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.disciplica.server.security.CurrentUser;
import com.disciplica.shared.task.CreateTaskRequest;
import com.disciplica.shared.task.TaskDto;
import com.disciplica.shared.task.UpdateTaskRequest;

/**
 * REST-Controller zum Verwalten der Aufgaben eines Benutzers (Auflisten,
 * Anlegen, Ändern, Löschen und Abschließen).
 * <p>
 * Alle Endpunkte beziehen sich auf den jeweils angemeldeten Benutzer.
 */
@RestController
@RequestMapping("/tasks")
public class TaskController {
    private final TaskService taskService;
    private final CurrentUser currentUser;

    /**
     * Erzeugt den Controller mit seinen Abhängigkeiten.
     *
     * @param taskService der Dienst mit der Aufgabenlogik
     * @param currentUser Hilfsmittel zum Ermitteln des angemeldeten Benutzers
     */
    public TaskController(TaskService taskService, CurrentUser currentUser) {
        this.taskService = taskService;
        this.currentUser = currentUser;
    }

    /**
     * Listet alle Aufgaben des angemeldeten Benutzers auf.
     *
     * @param authentication der Anmeldekontext der Anfrage
     * @return die Liste der Aufgaben
     */
    @GetMapping
    public List<TaskDto> list(Authentication authentication) {
        return taskService.list(currentUser.requireUserId(authentication));
    }

    /**
     * Legt eine neue Aufgabe für den angemeldeten Benutzer an.
     *
     * @param authentication der Anmeldekontext der Anfrage
     * @param request        die Daten der neuen Aufgabe
     * @return die angelegte Aufgabe
     */
    @PostMapping
    public TaskDto create(Authentication authentication, @Valid @RequestBody CreateTaskRequest request) {
        return taskService.create(currentUser.requireUserId(authentication), request);
    }

    /**
     * Ändert eine bestehende Aufgabe des angemeldeten Benutzers.
     *
     * @param authentication der Anmeldekontext der Anfrage
     * @param id             die Kennung der zu ändernden Aufgabe
     * @param request        die zu ändernden Felder
     * @return die aktualisierte Aufgabe
     */
    @PatchMapping("/{id}")
    public TaskDto update(Authentication authentication,
                          @PathVariable UUID id,
                          @Valid @RequestBody UpdateTaskRequest request) {
        return taskService.update(currentUser.requireUserId(authentication), id, request);
    }

    /**
     * Löscht eine Aufgabe des angemeldeten Benutzers.
     *
     * @param authentication der Anmeldekontext der Anfrage
     * @param id             die Kennung der zu löschenden Aufgabe
     */
    @DeleteMapping("/{id}")
    public void delete(Authentication authentication, @PathVariable UUID id) {
        taskService.delete(currentUser.requireUserId(authentication), id);
    }

    /**
     * Schließt eine Aufgabe ab und schreibt dem Benutzer die Belohnung gut.
     *
     * @param authentication der Anmeldekontext der Anfrage
     * @param id             die Kennung der abzuschließenden Aufgabe
     * @return die abgeschlossene Aufgabe
     */
    @PostMapping("/{id}/complete")
    public TaskDto complete(Authentication authentication, @PathVariable UUID id) {
        return taskService.complete(currentUser.requireUserId(authentication), id);
    }

    /**
     * Bewertet eine Gewohnheit negativ („−“): zieht dem Benutzer Lebenspunkte
     * ab und verringert die Serie.
     *
     * @param authentication der Anmeldekontext der Anfrage
     * @param id             die Kennung der Gewohnheit
     * @return die aktualisierte Aufgabe
     */
    @PostMapping("/{id}/down")
    public TaskDto scoreDown(Authentication authentication, @PathVariable UUID id) {
        return taskService.scoreDown(currentUser.requireUserId(authentication), id);
    }

    /**
     * Kauft eine Belohnung und zieht dem Benutzer das entsprechende Gold ab.
     *
     * @param authentication der Anmeldekontext der Anfrage
     * @param id             die Kennung der Belohnung
     * @return die gekaufte Belohnung
     */
    @PostMapping("/{id}/buy")
    public TaskDto buyReward(Authentication authentication, @PathVariable UUID id) {
        return taskService.buyReward(currentUser.requireUserId(authentication), id);
    }
}
