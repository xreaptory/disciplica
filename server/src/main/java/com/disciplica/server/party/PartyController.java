package com.disciplica.server.party;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.disciplica.server.security.CurrentUser;
import com.disciplica.shared.party.ChatMessageDto;
import com.disciplica.shared.party.CreatePartyRequest;
import com.disciplica.shared.party.InvitePartyRequest;
import com.disciplica.shared.party.LeaderboardEntryDto;
import com.disciplica.shared.party.PartyDto;
import com.disciplica.shared.party.PartyInviteDto;
import com.disciplica.shared.party.SendChatMessageRequest;

/**
 * REST-Controller für Gruppen (Parties): Erstellen, Einladungen, Mitgliedschaft
 * und Gruppen-Chat.
 * <p>
 * Alle Endpunkte beziehen sich auf den jeweils angemeldeten Benutzer.
 */
@RestController
@RequestMapping
public class PartyController {
    private final PartyService partyService;
    private final CurrentUser currentUser;

    /**
     * Erzeugt den Controller mit seinen Abhängigkeiten.
     *
     * @param partyService der Dienst mit der Gruppenlogik
     * @param currentUser  Hilfsmittel zum Ermitteln des angemeldeten Benutzers
     */
    public PartyController(PartyService partyService, CurrentUser currentUser) {
        this.partyService = partyService;
        this.currentUser = currentUser;
    }

    /**
     * Erstellt eine neue Gruppe; der Ersteller wird deren Leiter.
     *
     * @param authentication der Anmeldekontext der Anfrage
     * @param request        der Name der neuen Gruppe
     * @return die erstellte Gruppe
     */
    @PostMapping("/parties")
    public PartyDto create(Authentication authentication, @Valid @RequestBody CreatePartyRequest request) {
        return partyService.create(currentUser.requireUserId(authentication), request);
    }

    /**
     * Gibt die Gruppe zurück, in der der angemeldete Benutzer aktuell Mitglied
     * ist.
     *
     * @param authentication der Anmeldekontext der Anfrage
     * @return die aktuelle Gruppe des Benutzers
     */
    @GetMapping("/parties/current")
    public PartyDto current(Authentication authentication) {
        return partyService.current(currentUser.requireUserId(authentication));
    }

    /**
     * Lädt einen Benutzer in die aktuelle Gruppe ein (nur für den Leiter).
     *
     * @param authentication der Anmeldekontext der Anfrage
     * @param request        Benutzername oder E-Mail der einzuladenden Person
     * @return die erstellte Einladung
     */
    @PostMapping("/parties/current/invites")
    public PartyInviteDto invite(Authentication authentication, @Valid @RequestBody InvitePartyRequest request) {
        return partyService.invite(currentUser.requireUserId(authentication), request);
    }

    /**
     * Gibt die offenen Einladungen zurück, die an den angemeldeten Benutzer
     * gerichtet sind.
     *
     * @param authentication der Anmeldekontext der Anfrage
     * @return die Liste der offenen Einladungen
     */
    @GetMapping("/party-invites")
    public List<PartyInviteDto> pendingInvites(Authentication authentication) {
        return partyService.pendingInvites(currentUser.requireUserId(authentication));
    }

    /**
     * Nimmt eine Gruppeneinladung an.
     *
     * @param authentication der Anmeldekontext der Anfrage
     * @param id             die Kennung der Einladung
     * @return die aktualisierte Einladung
     */
    @PostMapping("/party-invites/{id}/accept")
    public PartyInviteDto accept(Authentication authentication, @PathVariable UUID id) {
        return partyService.accept(currentUser.requireUserId(authentication), id);
    }

    /**
     * Lehnt eine Gruppeneinladung ab.
     *
     * @param authentication der Anmeldekontext der Anfrage
     * @param id             die Kennung der Einladung
     * @return die aktualisierte Einladung
     */
    @PostMapping("/party-invites/{id}/decline")
    public PartyInviteDto decline(Authentication authentication, @PathVariable UUID id) {
        return partyService.decline(currentUser.requireUserId(authentication), id);
    }

    /**
     * Gibt die Bestenliste der aktuellen Gruppe zurück (nach Level sortiert).
     *
     * @param authentication der Anmeldekontext der Anfrage
     * @return die Bestenliste in Platzierungsreihenfolge
     */
    @GetMapping("/parties/current/leaderboard")
    public List<LeaderboardEntryDto> leaderboard(Authentication authentication) {
        return partyService.leaderboard(currentUser.requireUserId(authentication));
    }

    /**
     * Gibt die letzten Chat-Nachrichten der aktuellen Gruppe zurück.
     *
     * @param authentication der Anmeldekontext der Anfrage
     * @return die Liste der Chat-Nachrichten
     */
    @GetMapping("/parties/current/messages")
    public List<ChatMessageDto> messages(Authentication authentication) {
        return partyService.messages(currentUser.requireUserId(authentication));
    }

    /**
     * Sendet eine Chat-Nachricht in die aktuelle Gruppe.
     *
     * @param authentication der Anmeldekontext der Anfrage
     * @param request        der Nachrichtentext
     * @return die gespeicherte Chat-Nachricht
     */
    @PostMapping("/parties/current/messages")
    public ChatMessageDto chat(Authentication authentication, @Valid @RequestBody SendChatMessageRequest request) {
        return partyService.chat(currentUser.requireUserId(authentication), request);
    }
}
