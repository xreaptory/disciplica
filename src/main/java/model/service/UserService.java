package model.service;

import model.domain.model.User;

import java.io.IOException;

public interface UserService {
    User getUser();

    void readTaskData() throws IOException;

    void writeTaskData() throws IOException;

    void readUserData() throws IOException;

    void writeUserData() throws IOException;
}
