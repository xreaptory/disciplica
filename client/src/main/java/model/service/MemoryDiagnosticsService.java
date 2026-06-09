package model.service;

import com.google.inject.Singleton;

import javax.management.MBeanServer;
import java.lang.management.ManagementFactory;
import java.nio.file.Path;

/**
 * Diagnose-Dienst zum Erstellen von Heap-Dumps der laufenden Anwendung – als
 * Hilfsmittel zur Analyse des Speicherverbrauchs.
 */
@Singleton
public class MemoryDiagnosticsService {

    /**
     * Erstellt einen Heap-Dump der Java Virtual Machine.
     *
     * @param targetFile die Zieldatei des Dumps
     * @param liveOnly   {@code true}, um nur noch erreichbare Objekte
     *                   aufzunehmen
     * @return der Pfad der erstellten Dump-Datei
     * @throws IllegalStateException wenn der Heap-Dump nicht erstellt werden
     *                               kann
     */
    public Path writeHeapDump(Path targetFile, boolean liveOnly) {
        try {
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            String beanName = "com.sun.management:type=HotSpotDiagnostic";
            Object bean = ManagementFactory.newPlatformMXBeanProxy(
                    server,
                    beanName,
                    Class.forName("com.sun.management.HotSpotDiagnosticMXBean")
            );
            bean.getClass().getMethod("dumpHeap", String.class, boolean.class)
                    .invoke(bean, targetFile.toAbsolutePath().toString(), liveOnly);
            return targetFile;
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to create heap dump", exception);
        }
    }
}
