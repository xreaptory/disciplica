package model.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import model.domain.model.User;

import java.io.IOException;
import java.nio.file.Path;

@Singleton
public class DefaultUserService implements UserService {
    private final User user;
    private final DataPortabilityService dataPortabilityService;

    @Inject
    public DefaultUserService(User user, DataPortabilityService dataPortabilityService) {
        this.user = user;
        this.dataPortabilityService = dataPortabilityService;
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public void readTaskData() throws IOException {
        user.readTaskTxt();
    }

    @Override
    public void writeTaskData() throws IOException {
        user.writeTaskTxt();
    }

    @Override
    public void readUserData() throws IOException {
        user.readUserTxt();
    }

    @Override
    public void writeUserData() throws IOException {
        user.writeUserTxt();
    }

    @Override
    public Path exportEncryptedJsonBackup(Path outputFile) {
        return dataPortabilityService.exportEncryptedJson(outputFile);
    }

    @Override
    public void importEncryptedJsonBackup(Path inputFile) {
        dataPortabilityService.importEncryptedJson(inputFile);
    }

    @Override
    public Path exportHabitsCsv(Path outputFile) {
        return dataPortabilityService.exportCsv(outputFile);
    }

    @Override
    public Path prepareCloudSyncFolder() {
        return dataPortabilityService.ensureCloudSyncPreparationPath();
    }
}
