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

@Controller
public class PartyWebSocketController {
    private final PartyService partyService;

    public PartyWebSocketController(PartyService partyService) {
        this.partyService = partyService;
    }

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
