package com.ericsson.de.allure.service.application;

import com.ericsson.de.allure.plugin.JenkinsLogsPlugin;
import com.ericsson.de.allure.service.application.storage.StorageService;
import com.ericsson.de.allure.service.presentation.controllers.exceptions.NotFoundException;
import com.ericsson.de.allure.service.presentation.controllers.exceptions.ServiceException;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zeroturnaround.zip.ZipUtil;
import ru.yandex.qatools.allure.CustomAllureMain;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ericsson.de.allure.service.application.storage.StorageService.create;
import static com.ericsson.de.allure.service.application.util.UnsafeIOConsumer.safely;

import static java.lang.String.format;
import static java.nio.file.Files.copy;
import static java.nio.file.Files.walk;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Service
public class ReportGenerationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportGenerationService.class);

    public static final String UPLOAD_COMPLETED = "COMPLETED";
    private static final String REPORT_DIR_NAME = "report";
    private static final String REPORT_ARCHIVE_NAME = "report.zip";
    private static final String SERVICE_EXCEPTION_MESSAGE_PATTERN = "Unexpected error in %s report generation";
    private static boolean clearFolder = true;

    @Autowired
    private StorageService uploadStorage;
    @Autowired
    private AllureResultsFileCopier fileCopier;

    public File generate(String reportId) {
        try {
            Path reportDataDir = create(getReportDataDirectory(reportId));
            if (isEmpty(reportDataDir)) {
                throw new NotFoundException(format("Report with id '%s' not found", reportId));
            }
            return generate(reportDataDir, reportId);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new ServiceException(format(SERVICE_EXCEPTION_MESSAGE_PATTERN, reportId));
        }
    }

    public File generate(final String combinedReportId, final List<String> executionIds) {
        checkExecutionIds(executionIds);
        final List<Path> reportPaths = getResultsWhichExist(executionIds);
        checkForNonExistentResults(executionIds, reportPaths);
        try {
            return generate(reportPaths, combinedReportId);
        } catch (final IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new ServiceException(format(SERVICE_EXCEPTION_MESSAGE_PATTERN, combinedReportId));
        }
    }

    private static void checkExecutionIds(final List<String> executionIds) {
        if (executionIds.isEmpty()) {
            throw new ServiceException("No execution Ids given in the request");
        }
        executionIds.stream().forEach(checkIfExecutionIdIsBlank());
    }

    private static Consumer<String> checkIfExecutionIdIsBlank() {
        return executionId -> {
            if (Strings.isNullOrEmpty(executionId)) {
                throw new ServiceException("One of the executionIds was blank");
            }
        };
    }

    private List<Path> getResultsWhichExist(final List<String> executionIds) {
        return executionIds.stream().filter(executionIdExists()).map(this::getReportDataDirectory)
                           .collect(Collectors.toList());
    }

    private Predicate<String> executionIdExists() {
        return executionId -> {
            try {
                final Path path = create(getReportDataDirectory(executionId));
                return !isEmpty(path);
            } catch (final IOException e) {
                // Ignore
            }
            return false;
        };
    }

    private static void checkForNonExistentResults(final List<String> executionIds, final List<Path> reportPaths) {
        if (anyExecutionIdsNotExist(executionIds, reportPaths)) {
            final List<String> reportNames = reportPaths.stream().map(Path::getFileName)
                                                        .map(Path::toString).collect(Collectors.toList());
            final List<String> nonexistentReports = new ArrayList<>(executionIds);
            nonexistentReports.removeAll(reportNames);
            throw new NotFoundException(format("Execution id(s) not found: %s", nonexistentReports));
        }
    }

    private static boolean anyExecutionIdsNotExist(final List<String> executionIds, final List<Path> reportPaths) {
        return reportPaths.size() < executionIds.size();
    }

    public Path getReportDataDirectory(String reportId) {
        return uploadStorage.getDirectory(reportId);
    }

    public Path getReportDirectory(String reportId) {
        Path reportDataDirectory = getReportDataDirectory(reportId);
        return uploadStorage.getDirectory(reportDataDirectory, REPORT_DIR_NAME);
    }

    private File generate(Path sourceDir, String reportId) throws IOException {
        Path destinationDir = create(getReportDirectory(reportId));

        // double check in case of clean-up one of file/directory
        if (isEmpty(destinationDir) && hasNoGeneratedReport(sourceDir)) {
            copyConsoleLogs(destinationDir);

            String[] params = Stream.of(sourceDir.toString(), destinationDir.toString()).toArray(String[]::new);
            CustomAllureMain.run(params);

            completeUpload(reportId);

            return packing(destinationDir);
        } else {
            LOGGER.info("Found existing report at {}", sourceDir);
            try (Stream<Path> list = Files.list(sourceDir)) {
                return list.filter(path -> path.toString().endsWith(REPORT_ARCHIVE_NAME)).findFirst().get().toFile();
            }
        }
    }

    private synchronized File generate(final List<Path> reportDataDirectories, final String combinedReportId) throws IOException {
        final Path reportDataDirectory = getReportDataDirectory(combinedReportId);
        clearFolderContents(reportDataDirectory);
        final Path destinationDataDir = create(reportDataDirectory);

        final Path destinationDir = create(getReportDirectory(combinedReportId));

        copyResults(reportDataDirectories, destinationDataDir);

        final String[] params = Stream
                .of(destinationDataDir.toString(), destinationDir.toString())
                .toArray(String[]::new);
        CustomAllureMain.run(params);

        return packing(destinationDir);

    }

    private void copyResults(final List<Path> reportDataDirectories, final Path destinationDataDir) throws IOException {
        fileCopier.setDestinationPath(destinationDataDir);
        for (final Path path : reportDataDirectories) {
            fileCopier.setSourcePath(path);
            Files.walkFileTree(path, fileCopier);
        }
        fileCopier.resetFileIds();
    }

    /**
     * This method is only for unit testing purposes.
     * It greatly simplified unit testing the creation of a combined report
     */
    @VisibleForTesting
    protected static void disableFolderClearing() {
        clearFolder = false;
    }

    private static void clearFolderContents(final Path reportDataDirectory) throws IOException {
        final File[] contentsOfReportDataDirectory = reportDataDirectory.toFile().listFiles();
        LOGGER.debug("Files to clear:");
        if (clearFolder && contentsOfReportDataDirectory != null && contentsOfReportDataDirectory.length > 0) {
            try (Stream<Path> walkPath = walk(reportDataDirectory)) {
                walkPath.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(safely(File::delete));
            }
        }
    }

    public void completeUpload(String reportId) throws IOException {
        Path reportDataDir = create(getReportDataDirectory(reportId));
        Files.createFile(reportDataDir.resolve(UPLOAD_COMPLETED));
    }

    public boolean isUploadCompleted(String reportId) throws IOException {
        Path reportDataDir = create(getReportDataDirectory(reportId));
        return reportDataDir.resolve(UPLOAD_COMPLETED).toFile().exists();
    }

    private static void copyConsoleLogs(Path reportDir) throws IOException {
        Path logsPath = Paths.get(reportDir.getParent().toString(), JenkinsLogsPlugin.JENKINS_LOGS_SOURCE_PATH_DEFAULT);
        Path dstPath = Paths.get(reportDir.toString(), JenkinsLogsPlugin.JENKINS_LOGS_SOURCE_PATH_DEFAULT);

        if (logsPath.toFile().exists()) {
            LOGGER.debug("move {} to {}", logsPath.toUri(), dstPath.toUri());
            copy(logsPath, dstPath, REPLACE_EXISTING);
        } else {
            LOGGER.warn("console log files not found at {}", logsPath);
        }
    }

    @VisibleForTesting
    static boolean isEmpty(Path dir) throws IOException {
        try (final Stream<Path> directoryListing = Files.list(dir)) {
            return !directoryListing.findAny().isPresent();
        }
    }

    @VisibleForTesting
    static boolean hasNoGeneratedReport(final Path dirPath) throws IOException {
        try (final Stream<Path> directoryListing = Files.list(dirPath)) {
            return directoryListing.noneMatch(path -> path.toString().endsWith(REPORT_ARCHIVE_NAME));
        }
    }

    private File packing(Path sourcePath) {
        File archiveFile = Paths.get(sourcePath.getParent().toString(), REPORT_ARCHIVE_NAME).toFile();
        ZipUtil.pack(sourcePath.toFile(), archiveFile);

        LOGGER.info("entries in archive at {}:", archiveFile);
        ZipUtil.iterate(archiveFile, zipEntry -> LOGGER.info("- {}", zipEntry.getName()));
        return archiveFile;
    }
}
