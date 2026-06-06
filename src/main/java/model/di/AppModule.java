package model.di;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import model.domain.model.User;
import model.persistence.HabitRepository;
import model.persistence.SQLiteHabitRepository;
import model.service.BackupSchedulerService;
import model.service.DataPortabilityService;
import model.service.DefaultUserService;
import model.service.ReminderService;
import model.service.UserService;

public class AppModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(HabitRepository.class).to(SQLiteHabitRepository.class).in(Singleton.class);
        bind(UserService.class).to(DefaultUserService.class).in(Singleton.class);
        bind(ReminderService.class).in(Singleton.class);
        bind(DataPortabilityService.class).in(Singleton.class);
        bind(BackupSchedulerService.class).in(Singleton.class);
    }

    @Provides
    @Singleton
    User provideUser() {
        return new User("Simon");
    }
}
