package com.ericsson.de.allure.service.application.storage;

import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.Files.createDirectory;

@Service
public class StorageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StorageService.class);

    @Value("${upload.path}")
    private String uploadPath;

    @Value("${upload.dir.name}")
    private String uploadDirName;

    private Path directory;

    @PostConstruct
    public void init() {
        try {
            this.directory = getOrCreateDirectory(Paths.get(uploadPath), uploadDirName);
        } catch (IOException e) {
            Throwables.propagate(e);
        }
        LOGGER.info("created upload directory at {}", directory.toString());
    }

    public Path getRootDirectory() {
        return directory;
    }

    public Path getDirectory(String dirName) {
        return getDirectory(directory, dirName);
    }

    public Path getDirectory(Path parentPath, String dirName) {
        return parentPath.resolve(dirName);
    }

    public Path getOrCreateDirectory(String dirName) throws IOException {
        return create(getDirectory(directory, dirName));
    }

    private Path getOrCreateDirectory(Path parentPath, String dirName) throws IOException {
        return create(getDirectory(parentPath, dirName));
    }

    public static Path create(Path directory) throws IOException {
        if (notExists(directory)) {
            LOGGER.info("create new directory: {}", directory);
            createDirectory(directory);
        }
        return directory;
    }

    private static boolean notExists(final Path directory) {
        return !directory.toFile().exists();
    }

}
