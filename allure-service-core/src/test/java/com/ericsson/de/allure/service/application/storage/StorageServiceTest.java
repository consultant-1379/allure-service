package com.ericsson.de.allure.service.application.storage;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.util.ReflectionTestUtils.setField;

public class StorageServiceTest {

    private static final String UPLOAD_DIR_NAME = "reports_";

    private StorageService storage;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private Path directory;

    @Before
    public void setUp() throws IOException {
        storage = new StorageService();
        directory = folder.getRoot().toPath();
        setField(storage, "directory", directory);
    }

    @Test
    public void get_new_directory() throws IOException {
        Path uploadDirPath = storage.getOrCreateDirectory(UPLOAD_DIR_NAME);
        assertThat(uploadDirPath).isNotNull();
        assertThat(uploadDirPath).startsWith(directory.resolve(UPLOAD_DIR_NAME));
    }

    @Test
    public void get_existing_directory() throws IOException {
        Path newUplDirPath = storage.getOrCreateDirectory(UPLOAD_DIR_NAME);
        String uplDirName = newUplDirPath.getFileName().toString();
        Path uplDirPath = storage.getOrCreateDirectory(uplDirName);

        assertThat(uplDirPath.toString()).isEqualTo(newUplDirPath.toString());
    }

}
