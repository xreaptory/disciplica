package model.bootstrap;
import com.google.inject.Guice;
import com.google.inject.Injector;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.animation.PauseTransition;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.di.AppModule;
import model.persistence.DatabaseMigration;
import model.service.BackupSchedulerService;
import model.service.ReminderService;
import View.LoginView;
import View.OnboardingDialog;
import View.View;
import View.api.SessionStore;

public class HabitTrackerApp extends Application {
    private Injector injector;
    private ReminderService reminderService;
    private BackupSchedulerService backupSchedulerService;

    @Override
    public void start(Stage primaryStage) {
        DatabaseMigration.migrateOnStartup();
        injector = Guice.createInjector(new AppModule());
        reminderService = injector.getInstance(ReminderService.class);
        backupSchedulerService = injector.getInstance(BackupSchedulerService.class);
        SessionStore sessionStore = injector.getInstance(SessionStore.class);
        new LoginView(primaryStage, sessionStore, () -> {
            new OnboardingDialog(primaryStage, sessionStore).show();
            new View(injector);
            lazyInitializeBackgroundServices();
        });
    }

    private void lazyInitializeBackgroundServices() {
        PauseTransition delay = new PauseTransition(Duration.seconds(1.5));
        delay.setOnFinished(event -> {
            Thread initThread = new Thread(() -> {
                reminderService.start();
                backupSchedulerService.start();
            }, "disciplica-lazy-init");
            initThread.setDaemon(true);
            initThread.start();
        });
        Platform.runLater(delay::play);
    }

    @Override
    public void stop() {
        if (reminderService != null) {
            reminderService.stop();
        }
        if (backupSchedulerService != null) {
            backupSchedulerService.stop();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
