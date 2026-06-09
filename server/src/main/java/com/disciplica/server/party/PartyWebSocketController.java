package com.disciplica.server.party;

import java.security.Principal;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import com.disciplica.shared.party.ChatMessageDto;
import com.disciplica.shared.party.SendChatMessageRequest;

/**
 * WebSocket-Controller für den Gruppen-Chat in Echtzeit (STOMP).
 * <p>
 * Nimmt über WebSocket gesendete Nachrichten entgegen und verteilt sie an alle
 * Mitglieder der jeweiligen Gruppe.
 */
@Controller
public class PartyWebSocketController {
    private final PartyService partyService;

    /**
     * Erzeugt den Controller mit der Gruppenlogik.
     *
     * @param partyService der Dienst mit der Gruppenlogik
     */
    public PartyWebSocketController(PartyService partyService) {
        this.partyService = partyService;
    }

    /**
     * Verarbeitet eine über WebSocket gesendete Chat-Nachricht und verteilt
     * sie an die Gruppe.
     *
     * @param principal der angemeldete Absender (aus der WebSocket-Sitzung)
     * @param partyId   die Kennung der Gruppe
     * @param request   der Nachrichtentext
     * @return die gespeicherte Chat-Nachricht
     * @throws IllegalStateException wenn kein angemeldeter Absender vorhanden
     *                               ist
     */
    @MessageMapping("/parties/{partyId}/chat")
    public ChatMessageDto chat(Principal principal,
                               @DestinationVariable UUID partyId,
                               @Valid @Payload SendChatMessageRequest request) {
        if (principal == null) {
            throw new IllegalStateException("Authenticated WebSocket principal is required");
        }
        return partyService.chat(UUID.fromString(principal.getName()), partyId, request);
    }
}
