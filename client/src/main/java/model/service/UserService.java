package model.service;

import model.domain.model.User;

import java.io.IOException;
import java.nio.file.Path;

public interface UserService {
    User getUser();

    void readTaskData() throws IOException;

    void writeTaskData() throws IOException;

    void readUserData() throws IOException;

    void writeUserData() throws IOException;

    Path exportEncryptedJsonBackup(Path outputFile);

    void importEncryptedJsonBackup(Path inputFile);

    Path exportHabitsCsv(Path outputFile);

    Path prepareCloudSyncFolder();
}
