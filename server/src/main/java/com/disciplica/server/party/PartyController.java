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
import com.disciplica.shared.party.PartyDto;
import com.disciplica.shared.party.PartyInviteDto;
import com.disciplica.shared.party.SendChatMessageRequest;

@RestController
@RequestMapping
public class PartyController {
    private final PartyService partyService;
    private final CurrentUser currentUser;

    public PartyController(PartyService partyService, CurrentUser currentUser) {
        this.partyService = partyService;
        this.currentUser = currentUser;
    }

    @PostMapping("/parties")
    public PartyDto create(Authentication authentication, @Valid @RequestBody CreatePartyRequest request) {
        return partyService.create(currentUser.requireUserId(authentication), request);
    }

    @GetMapping("/parties/current")
    public PartyDto current(Authentication authentication) {
        return partyService.current(currentUser.requireUserId(authentication));
    }

    @PostMapping("/parties/current/invites")
    public PartyInviteDto invite(Authentication authentication, @Valid @RequestBody InvitePartyRequest request) {
        return partyService.invite(currentUser.requireUserId(authentication), request);
    }

    @PostMapping("/party-invites/{id}/accept")
    public PartyInviteDto accept(Authentication authentication, @PathVariable UUID id) {
        return partyService.accept(currentUser.requireUserId(authentication), id);
    }

    @PostMapping("/party-invites/{id}/decline")
    public PartyInviteDto decline(Authentication authentication, @PathVariable UUID id) {
        return partyService.decline(currentUser.requireUserId(authentication), id);
    }

    @GetMapping("/parties/current/messages")
    public List<ChatMessageDto> messages(Authentication authentication) {
        return partyService.messages(currentUser.requireUserId(authentication));
    }

    @PostMapping("/parties/current/messages")
    public ChatMessageDto chat(Authentication authentication, @Valid @RequestBody SendChatMessageRequest request) {
        return partyService.chat(currentUser.requireUserId(authentication), request);
    }
}
