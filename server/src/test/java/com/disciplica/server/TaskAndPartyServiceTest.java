package com.disciplica.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.disciplica.server.auth.AuthService;
import com.disciplica.server.party.PartyService;
import com.disciplica.server.task.TaskService;
import com.disciplica.shared.auth.RegisterRequest;
import com.disciplica.shared.party.CreatePartyRequest;
import com.disciplica.shared.party.SendChatMessageRequest;
import com.disciplica.shared.task.CreateTaskRequest;
import com.disciplica.shared.task.TaskDto;
import com.disciplica.shared.task.TaskType;

@SpringBootTest
@ActiveProfiles("test")
class TaskAndPartyServiceTest {
    @Autowired
    private AuthService authService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private PartyService partyService;

    @Test
    void createsCompletesTaskAndStoresPartyChat() {
        var user = authService.register(new RegisterRequest(
                "partyhero",
                "partyhero@example.com",
                "very-secure-password"
        )).user();

        TaskDto task = taskService.create(user.id(), new CreateTaskRequest(
                TaskType.DAILY,
                "Stretch",
                "Move for five minutes",
                12,
                "Exercise"
        ));
        TaskDto completed = taskService.complete(user.id(), task.id());

        partyService.create(user.id(), new CreatePartyRequest("Morning Party"));
        partyService.chat(user.id(), new SendChatMessageRequest("Ready for quests."));

        assertEquals("Stretch", completed.title());
        assertEquals(1, completed.streak());
        assertFalse(partyService.messages(user.id()).isEmpty());
        assertNotNull(partyService.current(user.id()).id());
    }
}
