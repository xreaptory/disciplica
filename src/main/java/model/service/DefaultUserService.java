package model.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import model.domain.model.User;

import java.io.IOException;

@Singleton
public class DefaultUserService implements UserService {
    private final User user;

    @Inject
    public DefaultUserService(User user) {
        this.user = user;
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public void readTaskData() throws IOException {
        user.readTaskTxt();
    }

    @Override
    public void writeTaskData() throws IOException {
        user.writeTaskTxt();
    }

    @Override
    public void readUserData() throws IOException {
        user.readUserTxt();
    }

    @Override
    public void writeUserData() throws IOException {
        user.writeUserTxt();
    }
}
