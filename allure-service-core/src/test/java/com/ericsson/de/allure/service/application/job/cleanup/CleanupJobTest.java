package com.ericsson.de.allure.service.application.job.cleanup;

import com.ericsson.de.allure.service.application.ReportGenerationService;
import com.ericsson.de.allure.service.application.storage.StorageService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;

import static com.ericsson.de.allure.service.application.ReportGenerationService.UPLOAD_COMPLETED;
import static java.nio.file.Files.createFile;
import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@RunWith(MockitoJUnitRunner.class)
public class CleanupJobTest {

    private static final int COMPLETED_LIFETIME = 1;
    private static final int UNCOMPLETED_LIFETIME = 2;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Spy
    @InjectMocks
    private CleanupJob job;

    @Mock
    private StorageService storageService;

    @Mock
    private ReportGenerationService reportService;

    @Before
    public void setUp() throws Exception {
        Path root = folder.getRoot().toPath();
        //noinspection ResultOfMethodCallIgnored
        doReturn(root).when(storageService).getRootDirectory();
    }

    @Test
    public void cleanup_exception() throws Exception {
        File report = folder.newFolder();
        doThrow(new IOException()).when(job).isStale(any(Path.class));

        assertThatNothingThrownBy(() -> job.cleanup());

        assertThat(report).exists();
        //noinspection ResultOfMethodCallIgnored
        verify(storageService).getRootDirectory();
    }

    @Test
    public void cleanup_notStale() throws Exception {
        File report = folder.newFolder();
        doReturn(false).when(job).isStale(any(Path.class));

        job.cleanup();

        assertThat(report).exists();
        //noinspection ResultOfMethodCallIgnored
        verify(storageService).getRootDirectory();
    }

    @Test
    public void cleanup_stale() throws Exception {
        File report = folder.newFolder();
        doReturn(true).when(job).isStale(any(Path.class));

        job.cleanup();

        assertThat(report).doesNotExist();
        //noinspection ResultOfMethodCallIgnored
        verify(storageService).getRootDirectory();
    }

    @Test
    public void isStale_deletionTime_future() throws Exception {
        Path path = folder.newFile().toPath();
        Instant deletionTime = now().plus(1, HOURS);
        doReturn(deletionTime).when(job).getDeletionTime(path);

        boolean result = job.isStale(path);

        assertThat(result).isFalse();
    }

    @Test
    public void isStale_deletionTime_past() throws Exception {
        Path path = folder.newFile().toPath();
        Instant deletionTime = now().minus(1, HOURS);
        doReturn(deletionTime).when(job).getDeletionTime(path);

        boolean result = job.isStale(path);

        assertThat(result).isTrue();
    }

    @Test
    public void getDeletionTime_completed() throws Exception {
        Path report = folder.newFolder("report_completed").toPath();
        createFile(report.resolve(UPLOAD_COMPLETED));
        doReturn(true).when(reportService).isUploadCompleted(anyString());
        setField(job, "completedReportLifetime", COMPLETED_LIFETIME);

        Instant result = job.getDeletionTime(report);

        System.out.println(result);
        assertThat(result).isBetween(
                now().plus(COMPLETED_LIFETIME, HOURS).minus(3, SECONDS),
                now().plus(COMPLETED_LIFETIME, HOURS).plus(3, SECONDS)
        );
        verify(reportService).isUploadCompleted("report_completed");
    }

    @Test
    public void getDeletionTime_uncompleted() throws Exception {
        Path report = folder.newFolder("report_uncompleted").toPath();
        doReturn(false).when(reportService).isUploadCompleted(anyString());
        setField(job, "uncompletedReportLifetime", UNCOMPLETED_LIFETIME);

        Instant result = job.getDeletionTime(report);

        assertThat(result).isBetween(
                now().plus(UNCOMPLETED_LIFETIME, HOURS).minus(3, SECONDS),
                now().plus(UNCOMPLETED_LIFETIME, HOURS).plus(3, SECONDS)
        );
        verify(reportService).isUploadCompleted("report_uncompleted");
    }

    private void assertThatNothingThrownBy(Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            fail("No exceptions were supposed to be thrown", e);
        }
    }
}
