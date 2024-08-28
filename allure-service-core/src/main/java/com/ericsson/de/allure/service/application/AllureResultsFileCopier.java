package com.ericsson.de.allure.service.application;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

import static java.nio.file.Files.copy;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * This {@link java.nio.file.FileVisitor} impl copies all the results files from the source to the destination.
 * Folder structure is preserved and zip files are ignored.
 */
@Component
public class AllureResultsFileCopier extends SimpleFileVisitor<Path> {
    private static final Logger LOGGER = getLogger(AllureResultsFileCopier.class);
    private Path dstPath;

    private Path srcPath;
    private Map<String, Integer> fileIds = new HashMap<>();

    @Value("${copy.ignore.file}")
    private String ignoreFile;

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        Path target = dstPath.resolve(srcPath.relativize(dir));
        try {
            copy(dir, target);
        } catch (FileAlreadyExistsException e) {
            if (!target.toFile().isDirectory()) {
                LOGGER.warn("Found that {} already existed and was not a directory so re-creating it", target);
                Files.delete(target);
                copy(dir, target);
            }
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(final Path file, BasicFileAttributes attrs) throws IOException {
        final Path dstFile = dstPath.resolve(srcPath.relativize(file));
        if (!file.getFileName().endsWith(ignoreFile)) {
            LOGGER.debug("Copying {} to {}", file, dstFile);
            try {
                copy(file, dstFile);
                LOGGER.debug("Copied {} to {}", file, dstFile);
            } catch (FileAlreadyExistsException e) {
                LOGGER.debug("{} already exists", dstFile);
                Path newDstPath = createUniquePath(dstFile);
                copy(file, newDstPath);
                LOGGER.debug("Copied {} to {}", file, newDstPath);
            }
        }
        return FileVisitResult.CONTINUE;
    }

    private Path createUniquePath(final Path dstFile) {
        Path parent = dstFile.getParent();
        String logFilename = dstFile.getFileName().toString();
        int fileId = fileIds.getOrDefault(logFilename, 1);
        Path newDstPath = parent.resolve(logFilename + "." + fileId++);
        fileIds.put(logFilename, fileId);
        return newDstPath;
    }

    public void setDestinationPath(final Path dstPath) {
        this.dstPath = dstPath;
    }

    public void setSourcePath(final Path srcPath) {
        this.srcPath = srcPath;
    }

    public void resetFileIds() {
        LOGGER.debug("Clearing file ids: {}", fileIds);
        fileIds.clear();
    }
}
