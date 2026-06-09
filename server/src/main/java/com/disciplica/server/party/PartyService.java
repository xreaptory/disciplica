package com.disciplica.server.party;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.disciplica.server.support.ApiException;
import com.disciplica.server.user.UserRepository;
import com.disciplica.shared.party.ChatMessageDto;
import com.disciplica.shared.party.CreatePartyRequest;
import com.disciplica.shared.party.InvitePartyRequest;
import com.disciplica.shared.party.PartyDto;
import com.disciplica.shared.party.PartyInviteDto;
import com.disciplica.shared.party.SendChatMessageRequest;

/**
 * Dienst mit der Geschäftslogik rund um Gruppen (Parties): Erstellen,
 * Einladungen verwalten und Chat-Nachrichten versenden.
 * <p>
 * Neu eingehende Chat-Nachrichten werden zusätzlich über den
 * Nachrichten-Broker an alle Mitglieder verteilt.
 */
@Service
public class PartyService {
    private final PartyRepository partyRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Erzeugt den Dienst mit seinen Abhängigkeiten.
     *
     * @param partyRepository   der Datenbankzugriff auf Gruppen
     * @param userRepository    der Datenbankzugriff auf Benutzer
     * @param messagingTemplate Komponente zum Verteilen von Nachrichten an die
     *                          Abonnenten
     */
    public PartyService(PartyRepository partyRepository,
                        UserRepository userRepository,
                        SimpMessagingTemplate messagingTemplate) {
        this.partyRepository = partyRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Erstellt eine neue Gruppe mit dem angegebenen Benutzer als Leiter.
     *
     * @param userId  die Kennung des erstellenden Benutzers
     * @param request der Name der neuen Gruppe
     * @return die erstellte Gruppe
     */
    @Transactional
    public PartyDto create(UUID userId, CreatePartyRequest request) {
        return partyRepository.create(userId, request.name());
    }

    /**
     * Ermittelt die Gruppe, in der der Benutzer aktuell Mitglied ist.
     *
     * @param userId die Kennung des Benutzers
     * @return die aktuelle Gruppe des Benutzers
     * @throws ApiException wenn der Benutzer in keiner Gruppe ist
     */
    public PartyDto current(UUID userId) {
        return partyRepository.findCurrentForUser(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "You are not in a party"));
    }

    /**
     * Lädt einen Benutzer in die aktuelle Gruppe ein. Nur der Leiter darf
     * einladen.
     *
     * @param userId  die Kennung des einladenden Benutzers
     * @param request Benutzername oder E-Mail der einzuladenden Person
     * @return die erstellte Einladung
     * @throws ApiException wenn der Benutzer nicht Leiter ist oder die
     *                      eingeladene Person nicht existiert
     */
    @Transactional
    public PartyInviteDto invite(UUID userId, InvitePartyRequest request) {
        PartyDto party = current(userId);
        if (!partyRepository.isLeader(party.id(), userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only the party leader can invite players");
        }
        UUID invitedUserId = userRepository.findByUsernameOrEmail(request.usernameOrEmail())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"))
                .id();
        return partyRepository.invite(party.id(), invitedUserId, userId);
    }

    /**
     * Nimmt eine offene Einladung an und fügt den Benutzer der Gruppe hinzu.
     *
     * @param userId   die Kennung des eingeladenen Benutzers
     * @param inviteId die Kennung der Einladung
     * @return die angenommene Einladung
     * @throws ApiException wenn die Einladung nicht gefunden wird oder nicht
     *                      mehr offen ist
     */
    @Transactional
    public PartyInviteDto accept(UUID userId, UUID inviteId) {
        PartyInviteDto invite = partyRepository.findInvite(inviteId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Invite not found"));
        if (!"PENDING".equals(invite.status())) {
            throw new ApiException(HttpStatus.CONFLICT, "Invite is not pending");
        }
        partyRepository.addMember(invite.partyId(), userId);
        return partyRepository.setInviteStatus(inviteId, "ACCEPTED");
    }

    /**
     * Lehnt eine offene Einladung ab.
     *
     * @param userId   die Kennung des eingeladenen Benutzers
     * @param inviteId die Kennung der Einladung
     * @return die abgelehnte Einladung
     * @throws ApiException wenn die Einladung nicht gefunden wird oder nicht
     *                      mehr offen ist
     */
    @Transactional
    public PartyInviteDto decline(UUID userId, UUID inviteId) {
        PartyInviteDto invite = partyRepository.findInvite(inviteId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Invite not found"));
        if (!"PENDING".equals(invite.status())) {
            throw new ApiException(HttpStatus.CONFLICT, "Invite is not pending");
        }
        return partyRepository.setInviteStatus(inviteId, "DECLINED");
    }

    /**
     * Gibt die Chat-Nachrichten der aktuellen Gruppe des Benutzers zurück.
     *
     * @param userId die Kennung des Benutzers
     * @return die Liste der Chat-Nachrichten
     */
    public List<ChatMessageDto> messages(UUID userId) {
        PartyDto party = current(userId);
        return partyRepository.messages(party.id());
    }

    /**
     * Sendet eine Chat-Nachricht in die aktuelle Gruppe des Benutzers.
     *
     * @param userId  die Kennung des Absenders
     * @param request der Nachrichtentext
     * @return die gespeicherte Chat-Nachricht
     */
    @Transactional
    public ChatMessageDto chat(UUID userId, SendChatMessageRequest request) {
        PartyDto party = current(userId);
        return chat(userId, party.id(), request);
    }

    /**
     * Sendet eine Chat-Nachricht in eine bestimmte Gruppe und verteilt sie
     * über den Nachrichten-Broker an alle Abonnenten.
     *
     * @param userId  die Kennung des Absenders
     * @param partyId die Kennung der Gruppe
     * @param request der Nachrichtentext
     * @return die gespeicherte Chat-Nachricht
     * @throws ApiException wenn der Absender kein Mitglied der Gruppe ist
     */
    @Transactional
    public ChatMessageDto chat(UUID userId, UUID partyId, SendChatMessageRequest request) {
        if (!partyRepository.isMember(partyId, userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You are not a member of this party");
        }
        ChatMessageDto message = partyRepository.addMessage(partyId, userId, request.message());
        messagingTemplate.convertAndSend("/topic/parties/" + partyId, message);
        return message;
    }
}
