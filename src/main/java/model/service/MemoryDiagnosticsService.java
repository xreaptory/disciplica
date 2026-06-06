package model.service;

import com.google.inject.Singleton;

import javax.management.MBeanServer;
import java.lang.management.ManagementFactory;
import java.nio.file.Path;

@Singleton
public class MemoryDiagnosticsService {

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
