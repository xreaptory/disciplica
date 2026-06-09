package model.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import model.domain.model.AbstractTask;
import model.domain.model.User;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;
import java.time.LocalTime;
import java.util.Properties;
import java.util.prefs.Preferences;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

/**
 * Dienst für tägliche Erinnerungen an noch offene Gewohnheiten.
 * <p>
 * Plant über einen Quartz-Scheduler eine tägliche Prüfung, zeigt
 * Benachrichtigungen über ein Symbol im System-Tray an und spielt einen
 * Hinweiston ab. Erinnerungen können über das Tray-Menü kurz aufgeschoben
 * (Snooze) oder abgeschaltet werden.
 */
@Singleton
public class ReminderService {
    private static final Logger logger = LoggerFactory.getLogger(ReminderService.class);
    private static final String PREF_TIME = "daily_reminder_time";
    private static final String DEFAULT_TIME = "20:00";

    private final UserService userService;
    private final Preferences preferences = Preferences.userNodeForPackage(ReminderService.class);

    private Scheduler scheduler;
    private TrayIcon trayIcon;
    private volatile boolean started;
    private volatile long snoozedUntilMillis;

    /**
     * Erzeugt den Dienst mit Zugriff auf die Benutzerdaten.
     *
     * @param userService der Benutzerdienst (liefert die offenen Aufgaben)
     */
    @Inject
    public ReminderService(UserService userService) {
        this.userService = userService;
    }

    /**
     * Startet den Dienst: richtet das Tray-Symbol ein und plant die tägliche
     * Erinnerung. Mehrfache Aufrufe haben keine Wirkung.
     */
    public synchronized void start() {
        if (started) {
            return;
        }
        try {
            initTrayIcon();
            scheduler = new StdSchedulerFactory(quartzProps("DisciplicaReminderScheduler")).getScheduler();
            scheduler.start();
            scheduleDailyReminder(getReminderTime());
            started = true;
            logger.info("Reminder service started at daily time {}", getReminderTime());
        } catch (Exception exception) {
            logger.error("Failed to start reminder service", exception);
        }
    }

    /**
     * Stoppt den Dienst, fährt den Scheduler herunter und entfernt das
     * Tray-Symbol.
     */
    public synchronized void stop() {
        try {
            if (scheduler != null && !scheduler.isShutdown()) {
                scheduler.shutdown(true);
            }
        } catch (SchedulerException exception) {
            logger.warn("Failed to cleanly shutdown reminder scheduler", exception);
        }
        if (trayIcon != null && SystemTray.isSupported()) {
            SystemTray.getSystemTray().remove(trayIcon);
        }
        started = false;
    }

    /**
     * Legt die Uhrzeit der täglichen Erinnerung fest und plant sie bei
     * laufendem Dienst neu.
     *
     * @param time die gewünschte Uhrzeit
     * @throws IllegalArgumentException wenn die Uhrzeit {@code null} ist
     */
    public synchronized void setReminderTime(LocalTime time) {
        if (time == null) {
            throw new IllegalArgumentException("Reminder time cannot be null");
        }
        preferences.put(PREF_TIME, time.toString());
        try {
            if (scheduler != null && scheduler.isStarted()) {
                scheduleDailyReminder(time);
            }
        } catch (SchedulerException exception) {
            logger.warn("Failed to reschedule reminder time {}", time, exception);
        }
    }

    /**
     * {@return die aktuell eingestellte Uhrzeit der Erinnerung}
     */
    public LocalTime getReminderTime() {
        String stored = preferences.get(PREF_TIME, DEFAULT_TIME);
        return LocalTime.parse(stored);
    }

    /**
     * Schiebt die Erinnerungen um die angegebene Anzahl Minuten auf.
     *
     * @param minutes die Dauer in Minuten (mindestens 1)
     */
    public void snoozeMinutes(int minutes) {
        int safeMinutes = Math.max(1, minutes);
        snoozedUntilMillis = System.currentTimeMillis() + (safeMinutes * 60_000L);
        displayNotification("Disciplica Reminder", "Snoozed for " + safeMinutes + " minutes.");
    }

    /**
     * Plant den täglichen Erinnerungs-Job zur angegebenen Uhrzeit (ersetzt
     * eine bestehende Planung).
     *
     * @param time die Uhrzeit der Erinnerung
     * @throws SchedulerException bei einem Fehler des Schedulers
     */
    private void scheduleDailyReminder(LocalTime time) throws SchedulerException {
        if (scheduler == null) {
            return;
        }
        scheduler.clear();

        JobDataMap dataMap = new JobDataMap();
        dataMap.put("reminderService", this);

        JobDetail job = JobBuilder.newJob(DailyReminderJob.class)
                .withIdentity("dailyHabitReminderJob")
                .usingJobData(dataMap)
                .build();

        String cron = String.format("0 %d %d * * ?", time.getMinute(), time.getHour());
        CronTrigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("dailyHabitReminderTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule(cron))
                .build();

        scheduler.scheduleJob(job, trigger);
    }

    /**
     * Erstellt die Konfiguration für den Quartz-Scheduler (im Speicher,
     * ein Thread).
     *
     * @param instanceName der Name der Scheduler-Instanz
     * @return die Konfigurationseigenschaften
     */
    private Properties quartzProps(String instanceName) {
        Properties properties = new Properties();
        properties.setProperty("org.quartz.scheduler.instanceName", instanceName);
        properties.setProperty("org.quartz.threadPool.threadCount", "1");
        properties.setProperty("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
        properties.setProperty("org.quartz.jobStore.class", "org.quartz.simpl.RAMJobStore");
        return properties;
    }

    /**
     * Prüft, ob noch offene Aufgaben bestehen, und zeigt in diesem Fall eine
     * Erinnerung an. Während einer aktiven Snooze-Phase geschieht nichts.
     */
    public void runReminderCheck() {
        if (System.currentTimeMillis() < snoozedUntilMillis) {
            return;
        }

        User user = userService.getUser();
        long incompleteCount = user.getTasks().stream().filter(task -> !task.isCompleted()).count();
        if (incompleteCount == 0) {
            return;
        }

        AbstractTask firstIncomplete = user.getTasks().stream().filter(task -> !task.isCompleted()).findFirst().orElse(null);
        String detail = firstIncomplete == null
                ? incompleteCount + " habits are still incomplete."
                : incompleteCount + " incomplete habits. Next: " + firstIncomplete.getName();

        playNotificationSound();
        displayNotification("Disciplica Reminder", detail);
    }

    /**
     * Richtet das Symbol im System-Tray samt Kontextmenü ein (sofern das
     * Betriebssystem den Tray unterstützt).
     */
    private void initTrayIcon() {
        if (!SystemTray.isSupported()) {
            logger.info("SystemTray not supported. Notifications disabled.");
            return;
        }
        if (trayIcon != null) {
            return;
        }

        PopupMenu popupMenu = new PopupMenu();
        MenuItem snooze10 = new MenuItem("Snooze 10 minutes");
        snooze10.addActionListener(event -> snoozeMinutes(10));
        MenuItem snooze30 = new MenuItem("Snooze 30 minutes");
        snooze30.addActionListener(event -> snoozeMinutes(30));
        MenuItem checkNow = new MenuItem("Check now");
        checkNow.addActionListener(event -> runReminderCheck());
        MenuItem exitReminder = new MenuItem("Disable reminders");
        exitReminder.addActionListener(event -> stop());
        popupMenu.add(checkNow);
        popupMenu.add(snooze10);
        popupMenu.add(snooze30);
        popupMenu.add(exitReminder);

        trayIcon = new TrayIcon(createTrayImage(), "Disciplica Reminder", popupMenu);
        trayIcon.setImageAutoSize(true);
        try {
            SystemTray.getSystemTray().add(trayIcon);
        } catch (AWTException exception) {
            logger.warn("Failed to initialize system tray icon", exception);
            trayIcon = null;
        }
    }

    /**
     * Erzeugt ein kleines Symbolbild für den System-Tray.
     *
     * @return das erzeugte Tray-Bild
     */
    private Image createTrayImage() {
        BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(new Color(124, 94, 230));
        graphics.fillOval(0, 0, 16, 16);
        graphics.setColor(Color.WHITE);
        graphics.setFont(new Font("Arial", Font.BOLD, 12));
        graphics.drawString("D", 4, 12);
        graphics.dispose();
        return image;
    }

    /**
     * Zeigt eine Benachrichtigung über das Tray-Symbol an.
     *
     * @param title   der Titel der Benachrichtigung
     * @param message der Text der Benachrichtigung
     */
    private void displayNotification(String title, String message) {
        if (trayIcon != null) {
            trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
        }
    }

    /**
     * Spielt einen kurzen Hinweiston ab (mit Rückfall auf einen einfachen
     * Signalton).
     */
    private void playNotificationSound() {
        Toolkit.getDefaultToolkit().beep();
        try {
            playTone(880, 120);
            playTone(988, 140);
        } catch (Exception exception) {
            logger.debug("Falling back to default beep only", exception);
        }
    }

    /**
     * Spielt einen einzelnen Ton der angegebenen Frequenz und Dauer ab.
     *
     * @param hz die Frequenz in Hertz
     * @param ms die Dauer in Millisekunden
     * @throws Exception bei einem Fehler der Audioausgabe
     */
    private void playTone(int hz, int ms) throws Exception {
        float sampleRate = 8000f;
        byte[] buffer = new byte[1];
        AudioFormat format = new AudioFormat(sampleRate, 8, 1, true, false);
        try (SourceDataLine line = AudioSystem.getSourceDataLine(format)) {
            line.open(format);
            line.start();
            for (int i = 0; i < ms * 8; i++) {
                double angle = i / (sampleRate / hz) * 2.0 * Math.PI;
                buffer[0] = (byte) (Math.sin(angle) * 90);
                line.write(buffer, 0, 1);
            }
            line.drain();
        }
    }

    /**
     * Quartz-Job, der zur geplanten Zeit die Erinnerungsprüfung auslöst.
     */
    public static class DailyReminderJob implements Job {
        /**
         * Führt die Erinnerungsprüfung des hinterlegten Dienstes aus.
         *
         * @param context der Ausführungskontext mit dem ReminderService
         * @throws JobExecutionException wenn der Dienst im Kontext fehlt
         */
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            ReminderService reminderService = (ReminderService) context.getMergedJobDataMap().get("reminderService");
            if (reminderService == null) {
                throw new JobExecutionException("Missing ReminderService in job context");
            }
            reminderService.runReminderCheck();
        }
    }
}
