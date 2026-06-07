package com.disciplica;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.image.WritableImage;
import javafx.stage.Window;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class UiFailureScreenshotExtension implements TestWatcher {
    private static final Logger logger = LoggerFactory.getLogger(UiFailureScreenshotExtension.class);
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        try {
            Path screenshotDir = Paths.get("target", "test-screenshots");
            Files.createDirectories(screenshotDir);

            String testName = context.getRequiredTestMethod().getName();
            String filename = testName + "-" + LocalDateTime.now().format(TS) + ".png";
            Path output = screenshotDir.resolve(filename);

            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                try {
                    Optional<Window> activeWindow = Window.getWindows().stream()
                            .filter(Window::isShowing)
                            .reduce((first, second) -> second);
                    if (activeWindow.isEmpty() || activeWindow.get().getScene() == null) {
                        latch.countDown();
                        return;
                    }
                    Scene scene = activeWindow.get().getScene();
                    WritableImage image = scene.snapshot(null);
                    ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", output.toFile());
                } catch (Exception exception) {
                    logger.warn("Failed to capture UI screenshot from JavaFX scene snapshot", exception);
                } finally {
                    latch.countDown();
                }
            });
            latch.await(3, TimeUnit.SECONDS);
        } catch (Exception exception) {
            logger.warn("Screenshot extension failed while handling test failure", exception);
        }
    }
}
