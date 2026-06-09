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

/**
 * Die zentrale JavaFX-Anwendungsklasse des Clients.
 * <p>
 * Beim Start wird die Datenbank migriert, der Abhängigkeits-Container (Guice)
 * aufgebaut und das Anmeldefenster angezeigt. Nach erfolgreicher Anmeldung
 * werden das Hauptfenster geöffnet und – leicht verzögert – die
 * Hintergrunddienste (Erinnerungen, automatische Sicherungen) gestartet.
 */
public class HabitTrackerApp extends Application {
    private Injector injector;
    private ReminderService reminderService;
    private BackupSchedulerService backupSchedulerService;

    /**
     * Baut die Anwendung auf und zeigt das Anmeldefenster an. Nach der
     * Anmeldung werden das Hauptfenster geöffnet und die Hintergrunddienste
     * initialisiert.
     *
     * @param primaryStage das Hauptfenster, das JavaFX bereitstellt
     */
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

    /**
     * Startet die Hintergrunddienste (Erinnerungen und automatische
     * Sicherungen) zeitversetzt in einem Hintergrund-Thread, damit das
     * Hauptfenster sofort bedienbar bleibt.
     */
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

    /**
     * Wird beim Beenden der Anwendung aufgerufen und stoppt die laufenden
     * Hintergrunddienste geordnet.
     */
    @Override
    public void stop() {
        if (reminderService != null) {
            reminderService.stop();
        }
        if (backupSchedulerService != null) {
            backupSchedulerService.stop();
        }
    }

    /**
     * Direkter Einstiegspunkt zum Starten der JavaFX-Anwendung.
     *
     * @param args die Kommandozeilenargumente
     */
    public static void main(String[] args) {
        launch(args);
    }
}
