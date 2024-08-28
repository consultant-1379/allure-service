package com.ericsson.de.allure.service.application;

import com.ericsson.de.allure.service.application.storage.StorageService;
import com.ericsson.de.allure.service.presentation.controllers.exceptions.NotFoundException;
import com.ericsson.de.allure.service.presentation.controllers.exceptions.ServiceException;
import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.zeroturnaround.zip.ZipUtil;
import org.zeroturnaround.zip.commons.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static com.ericsson.de.allure.service.application.ReportGenerationService.UPLOAD_COMPLETED;
import static java.lang.String.format;
import static java.nio.file.Files.newInputStream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

@RunWith(MockitoJUnitRunner.class)
public class ReportGenerationServiceTest {

    @InjectMocks
    private ReportGenerationService service;

    @Mock
    private StorageService storage;

    @Mock
    private AllureResultsFileCopier allureResultsFileCopier;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private Path resourceDirPath;

    private String reportId;
    private String combinedReportId;

    @Before
    public void setUp() {
        resourceDirPath = Paths.get(folder.getRoot().toURI());
        reportId = UUID.randomUUID().toString();
        combinedReportId = UUID.randomUUID().toString();
        doReturn(resourceDirPath).when(storage).getDirectory(eq(reportId));
    }

    @Test
    public void generate_report_with_no_data() {
        assertThatThrownBy(() -> service.generate(reportId))
            .isInstanceOf(NotFoundException.class)
            .hasMessage(format("Report with id '%s' not found", reportId));
    }

    @Test
    public void generate_report_with_service_error() throws IOException {
        folder.newFolder();
        doThrow(IOException.class).when(storage).getDirectory(eq(resourceDirPath), eq("report"));

        assertThatThrownBy(() -> service.generate(reportId))
            .isInstanceOf(ServiceException.class)
            .hasMessage("Unexpected error in " + reportId + " report generation");
    }

    @Test
    public void generate_existing_report() throws IOException, URISyntaxException {
        File reportArchive = createReportArchive("allure-report.zip");
        doReturn(resourceDirPath).when(storage).getDirectory(eq(resourceDirPath), eq("report"));

        File generateReport = service.generate(reportId);
        assertThat(generateReport).isEqualTo(reportArchive);
    }

    @Test
    public void when_report_already_generated_return_correct_zip_file_to_download() throws IOException, URISyntaxException {
        File root = folder.getRoot();

        File results = Paths.get(Resources.getResource("resultsContainingReport").toURI()).toFile();
        FileUtils.copyDirectory(results, root);

        assertThat(ReportGenerationService.hasNoGeneratedReport(resourceDirPath)).isFalse();
        doReturn(resourceDirPath).when(storage).getDirectory(eq(resourceDirPath), eq("report"));

        File generateReport = service.generate(reportId);
        assertThat(generateReport.getAbsolutePath()).endsWith("report.zip");
    }

    @Test
    public void directory_is_empty_by_default() throws IOException {
        assertThat(ReportGenerationService.isEmpty(resourceDirPath)).isTrue();
    }

    @Test
    public void directory_is_not_empty() throws IOException {
        folder.newFolder();
        assertThat(ReportGenerationService.isEmpty(resourceDirPath)).isFalse();
    }

    @Test
    public void directory_has_generated_report() throws IOException, URISyntaxException {
        File archiveDest = folder.newFile("allure-report.zip");
        InputStream allureStream = createInputStream("allure_xml_results_zip");
        FileUtils.copy(allureStream, archiveDest);
        assertThat(ReportGenerationService.hasNoGeneratedReport(resourceDirPath)).isFalse();
    }

    @Test
    public void directory_has_no_generated_report() throws IOException, URISyntaxException {
        File root = folder.getRoot();
        File results = Paths.get(Resources.getResource("results").toURI()).toFile();
        FileUtils.copyDirectory(results, root);
        assertThat(ReportGenerationService.hasNoGeneratedReport(resourceDirPath)).isTrue();
    }

    @Test
    public void directory_has_no_any_archive_by_default() throws IOException {
        assertThat(ReportGenerationService.hasNoGeneratedReport(resourceDirPath)).isTrue();
    }

    @Test
    public void complete_upload_with_create_file() throws IOException {
        service.completeUpload(reportId);

        Optional<String> foundCompleted = Arrays.stream(resourceDirPath.toFile().list())
                .filter(dir -> Objects.equals(dir, UPLOAD_COMPLETED))
                .findFirst();
        assertThat(foundCompleted).isPresent();
    }

    @Test
    public void by_default_no_complete_upload_file_created() throws IOException {
        assertThat(service.isUploadCompleted(reportId)).isFalse();
    }

    @Test
    public void generate_new_combined_report() throws IOException, URISyntaxException {
        List<String> executionIds = createReportsToCombine();
        setUpMockCalls();
        ReportGenerationService.disableFolderClearing();
        File combinedReport = service.generate(combinedReportId, executionIds);
        assertThat(combinedReport).isNotNull();
    }

    private void setUpMockCalls() throws IOException {
        doReturn(resourceDirPath).when(storage).getDirectory(eq("report1"));
        doReturn(resourceDirPath).when(storage).getDirectory(eq("report2"));
        doReturn(resourceDirPath).when(storage).getDirectory(eq(combinedReportId));
        doReturn(resourceDirPath).when(storage).getDirectory(any(), eq("report"));
        doReturn(FileVisitResult.CONTINUE).when(allureResultsFileCopier).preVisitDirectory(any(), any());
        doReturn(FileVisitResult.CONTINUE).when(allureResultsFileCopier).visitFile(any(), any());
        doCallRealMethod().when(allureResultsFileCopier).visitFileFailed(any(), any());
        doCallRealMethod().when(allureResultsFileCopier).postVisitDirectory(any(), any());
    }

    private List<String> createReportsToCombine() throws IOException, URISyntaxException {
        List<String> executionIds = new ArrayList<>();
        executionIds.add("report1");
        executionIds.add("report2");
        File folder1 = folder.newFolder("report1");
        File folder2 = folder.newFolder("report2");
        File report1 = createReportArchive("report1/allure-report.zip");
        ZipUtil.unpack(report1, folder1);
        File report2 = createReportArchive("report2/allure-report.zip");
        ZipUtil.unpack(report2, folder2);
        return executionIds;
    }

    @Test
    public void generate_combined_report_with_non_existent_data() {
        List<String> executionIds = new ArrayList<>();
        executionIds.add("non-existent");
        doReturn(resourceDirPath).when(storage).getDirectory(eq("non-existent"));
        doReturn(resourceDirPath).when(storage).getDirectory(eq(combinedReportId));
        assertThatThrownBy(() -> service.generate(combinedReportId, executionIds))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Execution id(s) not found: [non-existent]");
    }

    private static InputStream createInputStream(String fileName) throws URISyntaxException, IOException {
        return newInputStream(Paths.get(Resources.getResource(fileName).toURI()));
    }

    private File createReportArchive(final String fileName) throws IOException, URISyntaxException {
        File archiveDest = folder.newFile(fileName);
        FileUtils.copy(createInputStream("allure_xml_results_zip"), archiveDest);
        return archiveDest;
    }
}
