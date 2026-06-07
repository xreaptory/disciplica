package com.disciplica.integration;

import com.disciplica.testtags.IntegrationTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@IntegrationTest
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = IntegrationTestConfig.class)
@Transactional
@Rollback
public abstract class IntegrationTestBase {

    @BeforeAll
    static void setupDatabaseFile() throws Exception {
        Path dbPath = Paths.get("target", "integration-tests.db").toAbsolutePath();
        Files.createDirectories(dbPath.getParent());
        if (!Files.exists(dbPath)) {
            Files.createFile(dbPath);
        }
    }

}
