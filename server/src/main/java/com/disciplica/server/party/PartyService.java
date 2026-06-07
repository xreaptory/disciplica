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

@Service
public class PartyService {
    private final PartyRepository partyRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public PartyService(PartyRepository partyRepository,
                        UserRepository userRepository,
                        SimpMessagingTemplate messagingTemplate) {
        this.partyRepository = partyRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public PartyDto create(UUID userId, CreatePartyRequest request) {
        return partyRepository.create(userId, request.name());
    }

    public PartyDto current(UUID userId) {
        return partyRepository.findCurrentForUser(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "You are not in a party"));
    }

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

    @Transactional
    public PartyInviteDto decline(UUID userId, UUID inviteId) {
        PartyInviteDto invite = partyRepository.findInvite(inviteId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Invite not found"));
        if (!"PENDING".equals(invite.status())) {
            throw new ApiException(HttpStatus.CONFLICT, "Invite is not pending");
        }
        return partyRepository.setInviteStatus(inviteId, "DECLINED");
    }

    public List<ChatMessageDto> messages(UUID userId) {
        PartyDto party = current(userId);
        return partyRepository.messages(party.id());
    }

    @Transactional
    public ChatMessageDto chat(UUID userId, SendChatMessageRequest request) {
        PartyDto party = current(userId);
        return chat(userId, party.id(), request);
    }

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
