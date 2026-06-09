package model.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
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

import java.time.LocalTime;
import java.util.Properties;
import java.util.prefs.Preferences;

/**
 * Dienst, der automatische Sicherungen der Benutzerdaten plant.
 * <p>
 * Über einen Quartz-Scheduler wird – täglich oder wöchentlich zur
 * eingestellten Uhrzeit – eine verschlüsselte Sicherung im
 * Synchronisierungsordner erstellt.
 */
@Singleton
public class BackupSchedulerService {
    private static final Logger logger = LoggerFactory.getLogger(BackupSchedulerService.class);
    private static final String PREF_FREQUENCY = "backup_frequency";
    private static final String PREF_TIME = "backup_time";

    private final DataPortabilityService portabilityService;
    private final Preferences preferences = Preferences.userNodeForPackage(BackupSchedulerService.class);
    private Scheduler scheduler;

    /**
     * Erzeugt den Dienst mit dem für die Sicherung zuständigen Dienst.
     *
     * @param portabilityService der Dienst zum Erstellen der Sicherungen
     */
    @Inject
    public BackupSchedulerService(DataPortabilityService portabilityService) {
        this.portabilityService = portabilityService;
    }

    /**
     * Startet den Scheduler und plant die Sicherung gemäß den gespeicherten
     * Einstellungen.
     */
    public synchronized void start() {
        try {
            scheduler = new StdSchedulerFactory(quartzProps("DisciplicaBackupScheduler")).getScheduler();
            scheduler.start();
            schedule(getFrequency(), getTime());
        } catch (Exception exception) {
            logger.error("Failed to start backup scheduler", exception);
        }
    }

    /**
     * Stoppt den Scheduler.
     */
    public synchronized void stop() {
        try {
            if (scheduler != null && !scheduler.isShutdown()) {
                scheduler.shutdown(true);
            }
        } catch (SchedulerException exception) {
            logger.warn("Failed to stop backup scheduler", exception);
        }
    }

    /**
     * Stellt Häufigkeit und Uhrzeit der Sicherung ein und plant sie bei
     * laufendem Dienst neu.
     *
     * @param frequency die Häufigkeit (täglich oder wöchentlich)
     * @param time      die Uhrzeit der Sicherung
     */
    public synchronized void configure(BackupFrequency frequency, LocalTime time) {
        preferences.put(PREF_FREQUENCY, frequency.name());
        preferences.put(PREF_TIME, time.toString());
        try {
            if (scheduler != null && scheduler.isStarted()) {
                schedule(frequency, time);
            }
        } catch (SchedulerException exception) {
            logger.warn("Failed to reconfigure backup scheduler", exception);
        }
    }

    /**
     * {@return die eingestellte Sicherungshäufigkeit}
     */
    public BackupFrequency getFrequency() {
        String value = preferences.get(PREF_FREQUENCY, BackupFrequency.DAILY.name());
        return BackupFrequency.valueOf(value);
    }

    /**
     * {@return die eingestellte Uhrzeit der Sicherung}
     */
    public LocalTime getTime() {
        return LocalTime.parse(preferences.get(PREF_TIME, "02:00"));
    }

    /**
     * Plant den Sicherungs-Job gemäß Häufigkeit und Uhrzeit (ersetzt eine
     * bestehende Planung).
     *
     * @param frequency die Häufigkeit
     * @param time      die Uhrzeit
     * @throws SchedulerException bei einem Fehler des Schedulers
     */
    private void schedule(BackupFrequency frequency, LocalTime time) throws SchedulerException {
        if (scheduler == null) {
            return;
        }
        scheduler.clear();

        JobDataMap data = new JobDataMap();
        data.put("backupSchedulerService", this);

        JobDetail job = JobBuilder.newJob(BackupJob.class)
                .withIdentity("backup-job")
                .usingJobData(data)
                .build();

        String cron = frequency == BackupFrequency.DAILY
                ? String.format("0 %d %d * * ?", time.getMinute(), time.getHour())
                : String.format("0 %d %d ? * MON", time.getMinute(), time.getHour());

        CronTrigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("backup-trigger")
                .withSchedule(CronScheduleBuilder.cronSchedule(cron))
                .build();
        scheduler.scheduleJob(job, trigger);
        logger.info("Configured {} backup at {}", frequency, time);
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
     * Erstellt sofort eine verschlüsselte Sicherung im Synchronisierungsordner.
     */
    void runBackupNow() {
        portabilityService.createTimestampedEncryptedBackupInSyncFolder();
    }

    /**
     * Mögliche Häufigkeiten für automatische Sicherungen.
     */
    public enum BackupFrequency {
        /** Tägliche Sicherung. */
        DAILY,
        /** Wöchentliche Sicherung (montags). */
        WEEKLY
    }

    /**
     * Quartz-Job, der zur geplanten Zeit eine Sicherung auslöst.
     */
    public static class BackupJob implements Job {
        /**
         * Löst die Sicherung des hinterlegten Dienstes aus.
         *
         * @param context der Ausführungskontext mit dem Sicherungsdienst
         * @throws JobExecutionException wenn der Dienst im Kontext fehlt
         */
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            BackupSchedulerService service =
                    (BackupSchedulerService) context.getMergedJobDataMap().get("backupSchedulerService");
            if (service == null) {
                throw new JobExecutionException("Missing backup scheduler service");
            }
            service.runBackupNow();
        }
    }
}
