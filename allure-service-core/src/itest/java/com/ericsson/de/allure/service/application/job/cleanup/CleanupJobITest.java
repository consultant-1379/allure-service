package com.ericsson.de.allure.service.application.job.cleanup;

import com.ericsson.de.allure.service.application.ReportGenerationService;
import com.ericsson.de.allure.service.application.job.lock.LockingService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.file.Path;

import static com.ericsson.de.allure.service.application.storage.StorageService.create;
import static com.ericsson.de.allure.service.infrastructure.Profiles.INTEGRATION_TEST;
import static java.nio.file.Files.deleteIfExists;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@ActiveProfiles(INTEGRATION_TEST)
@SpringBootTest(properties = {
        "jobs.cleanup.report.lifetime.completed.hours=0",
        "jobs.cleanup.report.lifetime.uncompleted.hours=100"
})
public class CleanupJobITest {

    @Autowired
    private CleanupJob job;

    @MockBean
    @SuppressWarnings("unused")
    private LockingService lockingService;

    @Autowired
    private ReportGenerationService reportService;

    private String reportId;

    private Path reportDataDirectory;

    @Before
    public void setUp() throws Exception {
        reportId = getClass().getSimpleName();
        reportDataDirectory = create(reportService.getReportDataDirectory(reportId));
    }

    @After
    public void tearDown() throws Exception {
        deleteIfExists(reportDataDirectory);
    }

    @Test
    public void cleanup_completedReport() throws Exception {
        reportService.completeUpload(reportId);

        job.cleanup();

        assertThat(reportDataDirectory).doesNotExist();
    }

    @Test
    public void cleanup_uncompletedReport() throws Exception {
        job.cleanup();

        assertThat(reportDataDirectory).exists();
    }
}
