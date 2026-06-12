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
    private Stage primaryStage;
    private ReminderService reminderService;
    private BackupSchedulerService backupSchedulerService;
    private boolean backgroundServicesStarted;

    /**
     * Baut die Anwendung auf und zeigt das Anmeldefenster an. Nach der
     * Anmeldung werden das Hauptfenster geöffnet und die Hintergrunddienste
     * initialisiert.
     *
     * @param primaryStage das Hauptfenster, das JavaFX bereitstellt
     */
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        DatabaseMigration.migrateOnStartup();
        injector = Guice.createInjector(new AppModule());
        reminderService = injector.getInstance(ReminderService.class);
        backupSchedulerService = injector.getInstance(BackupSchedulerService.class);
        showLogin();
    }

    /**
     * Zeigt das Anmeldefenster an. Wird sowohl beim Start als auch nach einer
     * Abmeldung verwendet.
     */
    private void showLogin() {
        SessionStore sessionStore = injector.getInstance(SessionStore.class);
        new LoginView(primaryStage, sessionStore, this::onAuthenticated);
    }

    /**
     * Wird nach erfolgreicher Anmeldung aufgerufen: zeigt das Hauptfenster und
     * startet einmalig die Hintergrunddienste. Der Onboarding-Assistent (Helden
     * erstellen) erscheint nur bei einer frischen Registrierung – bereits
     * registrierte Benutzer gelangen direkt zum Dashboard. Bei der Abmeldung
     * kehrt die Anwendung über {@link #showLogin()} wieder zum Anmeldefenster
     * zurück.
     *
     * @param newAccount {@code true}, wenn gerade ein neues Konto registriert
     *                   wurde
     */
    private void onAuthenticated(boolean newAccount) {
        if (newAccount) {
            SessionStore sessionStore = injector.getInstance(SessionStore.class);
            new OnboardingDialog(primaryStage, sessionStore).show();
        }
        new View(injector, this::showLogin);
        lazyInitializeBackgroundServices();
    }

    /**
     * Startet die Hintergrunddienste (Erinnerungen und automatische
     * Sicherungen) zeitversetzt in einem Hintergrund-Thread, damit das
     * Hauptfenster sofort bedienbar bleibt.
     */
    private void lazyInitializeBackgroundServices() {
        if (backgroundServicesStarted) {
            return;
        }
        backgroundServicesStarted = true;
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
