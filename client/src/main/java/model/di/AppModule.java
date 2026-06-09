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
import View.api.SessionStore;

/**
 * Konfiguration des Abhängigkeits-Containers (Guice).
 * <p>
 * Legt fest, welche konkrete Umsetzung jeweils für eine Schnittstelle
 * verwendet wird und welche Komponenten als Singleton (nur einmal pro
 * Anwendung) erzeugt werden.
 */
public class AppModule extends AbstractModule {

    /**
     * Verknüpft die Schnittstellen mit ihren konkreten Umsetzungen und legt
     * deren Lebensdauer (Singleton) fest.
     */
    @Override
    protected void configure() {
        bind(HabitRepository.class).to(SQLiteHabitRepository.class).in(Singleton.class);
        bind(UserService.class).to(DefaultUserService.class).in(Singleton.class);
        bind(ReminderService.class).in(Singleton.class);
        bind(DataPortabilityService.class).in(Singleton.class);
        bind(BackupSchedulerService.class).in(Singleton.class);
        bind(SessionStore.class).in(Singleton.class);
    }

    /**
     * Stellt den aktuellen Benutzer als Singleton bereit.
     *
     * @return der für die Anwendung verwendete Benutzer
     */
    @Provides
    @Singleton
    User provideUser() {
        return new User("Simon");
    }
}
