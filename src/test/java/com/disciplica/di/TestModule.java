package com.disciplica.di;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import model.domain.model.User;
import model.persistence.HabitRepository;
import model.service.UserService;

import java.io.IOException;

public class TestModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(HabitRepository.class).toInstance(new HabitRepository());
        bind(UserService.class).toInstance(new MockUserService());
    }

    @Provides
    @Singleton
    User provideUser() {
        return new User("TestUser");
    }

    private static final class MockUserService implements UserService {
        private final User user = new User("MockUser");

        @Override
        public User getUser() {
            return user;
        }

        @Override
        public void readTaskData() throws IOException {
            // no-op mock
        }

        @Override
        public void writeTaskData() throws IOException {
            // no-op mock
        }

        @Override
        public void readUserData() throws IOException {
            // no-op mock
        }

        @Override
        public void writeUserData() throws IOException {
            // no-op mock
        }
    }
}
