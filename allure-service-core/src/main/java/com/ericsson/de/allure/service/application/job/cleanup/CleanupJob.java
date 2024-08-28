package com.ericsson.de.allure.service.application.job.cleanup;

import com.ericsson.de.allure.service.application.ReportGenerationService;
import com.ericsson.de.allure.service.application.job.CronRunnable;
import com.ericsson.de.allure.service.application.job.lock.LockingService;
import com.ericsson.de.allure.service.application.storage.StorageService;
import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.stream.Stream;

import static com.ericsson.de.allure.service.application.ReportGenerationService.UPLOAD_COMPLETED;
import static com.ericsson.de.allure.service.application.util.UnsafeIOConsumer.safely;
import static java.nio.file.Files.getLastModifiedTime;
import static java.nio.file.Files.newDirectoryStream;
import static java.nio.file.Files.walk;
import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.util.Comparator.reverseOrder;

@Component
@RefreshScope
public class CleanupJob implements CronRunnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanupJob.class);

    @Value("${jobs.cleanup.report.lifetime.completed.hours}")
    private int completedReportLifetime;

    @Value("${jobs.cleanup.report.lifetime.uncompleted.hours}")
    private int uncompletedReportLifetime;

    @Value("${jobs.cleanup.cron}")
    private String cron;

    @Autowired
    private LockingService lockingService;

    @Autowired
    private StorageService storageService;

    @Autowired
    private ReportGenerationService reportService;

    @Override
    public void run() {
        lockingService.lock("cleanupJob", this::cleanup);
    }

    @Override
    public String getExpression() {
        return cron;
    }

    @VisibleForTesting
    void cleanup() {
        LOGGER.info("cleanup job has started");
        try (DirectoryStream<Path> paths = newDirectoryStream(storageService.getRootDirectory())) {
            paths.forEach(safely(this::cleanupReport));
        } catch (IOException | UncheckedIOException e) {
            LOGGER.error("Could not perform cleanup job", e);
        }
    }

    private void cleanupReport(Path reportFolder) throws IOException {
        if (isStale(reportFolder)) {
            LOGGER.info("Deleting stale reportId '{}'", reportFolder);
            try (Stream<Path> walk = walk(reportFolder)) {
                walk.sorted(reverseOrder()).forEach(safely(Files::delete));
            }
        }
    }

    @VisibleForTesting
    boolean isStale(Path report) throws IOException {
        Instant deletionTime = getDeletionTime(report);
        LOGGER.debug("Report '{}' will be deleted after: {}",
                report.getFileName(), deletionTime);
        return now().isAfter(deletionTime);
    }

    @VisibleForTesting
    Instant getDeletionTime(Path reportFolder) throws IOException {
        String reportId = reportFolder.toFile().getName();
        if (reportService.isUploadCompleted(reportId)) {
            return calculateDeletionTime(reportFolder.resolve(UPLOAD_COMPLETED), completedReportLifetime);
        } else {
            return calculateDeletionTime(reportFolder, uncompletedReportLifetime);
        }
    }

    private static Instant calculateDeletionTime(Path path, int lifetime) throws IOException {
        FileTime lastModifiedTime = getLastModifiedTime(path);
        return lastModifiedTime.toInstant().plus(lifetime, HOURS);
    }
}
