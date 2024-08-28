package com.ericsson.de.allure.service.presentation.controllers;

import com.ericsson.de.allure.service.AllureServiceApplication;
import com.ericsson.de.allure.service.api.resource.dto.ErrorResponse;
import com.ericsson.de.allure.service.application.storage.StorageService;
import com.google.common.io.Resources;
import org.apache.tika.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.zeroturnaround.zip.ZipUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ericsson.de.allure.service.application.util.UnsafeIOConsumer.safely;
import static com.ericsson.de.allure.service.infrastructure.Profiles.INTEGRATION_TEST;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static java.nio.file.Files.newInputStream;
import static java.nio.file.Files.walk;
import static java.util.Comparator.reverseOrder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CACHE_CONTROL;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpHeaders.CONTENT_LENGTH;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpHeaders.EXPIRES;
import static org.springframework.http.HttpHeaders.PRAGMA;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;

@RunWith(SpringRunner.class)
@ActiveProfiles(INTEGRATION_TEST)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = AllureServiceApplication.class)
public class ReportControllerITest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportControllerITest.class);

    private static final String REPORT_ID = "12345";

    @Rule
    public TemporaryFolder tempDirRule = new TemporaryFolder();

    @Autowired
    private StorageService storageService;

    @Autowired
    private TestRestTemplate restTemplate;

    private RequestCallback requestCallback;
    private RequestCallback requestCallback1;
    private RequestCallback requestCallback2;
    private RequestCallback badRequestCallback;
    private ResponseExtractor<ClientHttpResponse> responseExtractor;

    @Before
    public void setUp() throws IOException, URISyntaxException {
        deleteReportsDirectory();
        requestCallback = createRequestCallback("allure_xml_results_zip");
        requestCallback1 = createRequestCallback("results3.zip");
        requestCallback2 = createRequestCallback("results4.zip");
        badRequestCallback = createRequestCallback("xunit_json");
        responseExtractor = response -> {
            logHeader(response);
            return response;
        };
    }

    @Test
    public void upload_invalid_data() throws Exception {
        ResponseExtractor<ClientHttpResponse> extractor = response -> {
            logHeader(response);
            assertThat(response.getStatusCode()).as("Response code").isEqualTo(BAD_REQUEST);
            assertThat(IOUtils.readLines(response.getBody()).toString()).contains("Invalid upload content type");
            return response;
        };
        restTemplate.execute("/reports/" + REPORT_ID, PUT, badRequestCallback, extractor);
    }

    @Test
    public void get_non_existing_report() throws IOException {
        ResponseEntity<ErrorResponse> entity = restTemplate.getForEntity("/reports/" + REPORT_ID, ErrorResponse.class);
        assertThat(entity.getStatusCode()).as("Response code").isEqualTo(NOT_FOUND);
        assertThat(entity.getBody().getMessage()).isEqualTo(format("Report with id '%s' not found", REPORT_ID));
    }

    @Test
    public void upload_xml_results() throws Exception {
        ClientHttpResponse uploadResponse = uploadXmlReport(REPORT_ID, requestCallback);

        assertThat(uploadResponse.getHeaders().getFirst(CACHE_CONTROL))
                .as(CACHE_CONTROL)
                .isEqualTo("no-cache, no-store, max-age=0, must-revalidate");
        assertThat(uploadResponse.getHeaders().getFirst(PRAGMA))
                .as(PRAGMA)
                .isEqualTo("no-cache");
        assertThat(uploadResponse.getHeaders().getFirst(EXPIRES))
                .as(EXPIRES)
                .isEqualTo("0");

        ResponseEntity<byte[]> reportResponse = restTemplate.getForEntity("/reports/{reportId}", byte[].class, REPORT_ID);
        assertThat(reportResponse.getStatusCode())
                .as("Response code")
                .isEqualTo(OK);
        assertThat(reportResponse.getHeaders().getFirst(CACHE_CONTROL))
                .as(CACHE_CONTROL)
                .isEqualTo("no-transform, max-age=3600");
        assertThat(reportResponse.getHeaders().getFirst(CONTENT_DISPOSITION))
                .as(CONTENT_DISPOSITION)
                .isEqualTo("attachment; filename=\"report-12345.zip\"");
        assertThat(reportResponse.getHeaders().getFirst(CONTENT_TYPE))
                .as(CONTENT_TYPE)
                .isEqualTo(APPLICATION_OCTET_STREAM_VALUE);
        assertThat(reportResponse.getHeaders().getFirst(CONTENT_LENGTH))
                .as(CONTENT_LENGTH)
                .isNotEmpty();

        File actualReport = save(reportResponse.getBody());
        File expectedReport = getResource("expected_report.zip").toFile();
        List<String> actualZipEntries = getZipEntries(actualReport);
        List<String> expectedZipEntries = getZipEntries(expectedReport);

        assertThat(actualZipEntries)
                .hasSameElementsAs(expectedZipEntries);
    }

    @Test
    public void generate() throws Exception {

        // generating report
        uploadXmlReport(REPORT_ID, requestCallback);
        String url = "/reports/" + REPORT_ID;
        ClientHttpResponse response = restTemplate.execute(url, GET, requestCallback, responseExtractor);
        File generated = unpackReport(response);
        verifyJenkinsLogPlugin(generated, "AutoProvisioningOdin_ScrumU.log");
        verifyLogsInCorrectLocation(generated, "AutoProvisioningOdin_ScrumU.log");
        verifyEnvironmentProperties(generated);
        checkStaticContent(REPORT_ID);

    }

    private File unpackReport(final ClientHttpResponse response) throws IOException {
        File generated = tempDirRule.newFolder("generated"+System.currentTimeMillis());
        Path reportZip = generated.toPath().resolve("generated.zip");
        Files.copy(response.getBody(), reportZip);
        ZipUtil.unpack(reportZip.toFile(), generated);
        return generated;
    }

    private void checkStaticContent(final String reportId) throws IOException {
        // checking static content
        assertResourceExists("/index.html", reportId);
        assertResourceExists("/css/app.css", reportId);
        assertResourceExists("/js/app.js", reportId);
        assertResourceExists("/data/xunit.json", reportId);
    }

    private void verifyEnvironmentProperties(final File generated) throws IOException {
        // checking environment properties file was used
        Path environmentJsonPath = generated.toPath().resolve("data").resolve("environment.json");
        assertThat(environmentJsonPath).exists();
        String environmentJson = Files
                .readAllLines(environmentJsonPath).toString();
        assertThat(environmentJson).contains("\"key\" : \"SUT\"");
        assertThat(environmentJson).contains("\"value\" : \"localhost\"");
    }

    private void verifyLogsInCorrectLocation(final File generated, final String... logFileNames) throws IOException {
        // checking that logs folder was copied into proper location
        Path logsFolder = generated.toPath().resolve("te-console-logs");
        assertThat(logsFolder).exists();
        for(String logFileName : logFileNames){
            Path log = logsFolder.resolve(logFileName);
            assertThat(log).exists();
            assertThat(Files
                    .readAllLines(log).size()).isGreaterThan(0);
        }
    }

    private void verifyJenkinsLogPlugin(final File generated, final String... logFileNames) throws IOException {
        // checking Jenkins Logs plugin
        Path logsJsonPath = generated.toPath().resolve("data").resolve("jenkins-logs.json");
        assertThat(logsJsonPath).exists();
        String logsJson = Files
                .readAllLines(logsJsonPath).toString();
        assertThat(logsJson).contains("te-console-logs/");
        for(String logFileName: logFileNames) {
            assertThat(logsJson).contains(logFileName);
        }
    }

    @Test
    public void leaveExecutionIdBlank() throws IOException {
        uploadXmlReport("report1", requestCallback);
        ResponseEntity<ErrorResponse> entity = restTemplate.getForEntity("/reports/combined/badRequest?executionId=", ErrorResponse.class);
        assertThat(entity.getStatusCode()).as("Response code").isEqualTo(BAD_REQUEST);
        assertThat(entity.getBody().getMessage()).isEqualTo("One of the executionIds was blank");
    }

    @Test
    public void generateCombinedReport() throws IOException {
        uploadXmlReport("report1", requestCallback);
        uploadXmlReport("report2", requestCallback1);
        String url = "/reports/combined/dailyReport?executionId=report1&executionId=report2";
        ClientHttpResponse response = restTemplate.execute(url, GET, null, responseExtractor);
        File generated = unpackReport(response);
        verifyJenkinsLogPlugin(generated, "AutoProvisioningOdin_ScrumU.log", "Http_Tool.log");
        verifyLogsInCorrectLocation(generated, "AutoProvisioningOdin_ScrumU.log", "Http_Tool.log");
        verifyEnvironmentProperties(generated);
        checkStaticContent("dailyReport");

    }

    @Test
    public void generateCombinedReportWithNonExistingReport() throws IOException {
        uploadXmlReport("report1", requestCallback);
        String url = "/reports/combined/dailyReport?executionId=report1&executionId=report3";
        ResponseEntity<ErrorResponse> response = restTemplate.getForEntity(url, ErrorResponse.class);
        assertThat(response.getStatusCode()).as("Response code").isEqualTo(NOT_FOUND);
        assertThat(response.getBody().getMessage()).isEqualTo(format("Execution id(s) not found: [%s]", "report3"));
    }

    @Test
    public void generateCombinedReportWithSameSuiteFromDifferentExecutions() throws IOException {
        uploadXmlReport("report1", requestCallback1);
        uploadXmlReport("report2", requestCallback2);

        String url = "/reports/combined/httpCombined?executionId=report1&executionId=report2";
        ClientHttpResponse response = restTemplate.execute(url, GET, null, responseExtractor);
        File generated = unpackReport(response);

        verifyCorrectNumberOfTestcases(generated, 14);
        verifyJenkinsLogPlugin(generated, "Http_Tool.log", "Http_Tool.log.1");
        verifyLogsInCorrectLocation(generated, "Http_Tool.log", "Http_Tool.log.1");
    }

    @Test
    public void regenerateCombinedReportWithSameSuiteFromDifferentExecutions() throws IOException {
        uploadXmlReport("report1", requestCallback1);
        uploadXmlReport("report2", requestCallback2);

        String url = "/reports/combined/httpCombined?executionId=report1&executionId=report2";
        restTemplate.execute(url, GET, null, responseExtractor);

        ClientHttpResponse response = restTemplate.execute(url, GET, null, responseExtractor);
        File generated = unpackReport(response);

        verifyCorrectNumberOfTestcases(generated, 14);
        verifyJenkinsLogPlugin(generated, "Http_Tool.log", "Http_Tool.log.1");
        verifyLogsInCorrectLocation(generated, "Http_Tool.log", "Http_Tool.log.1");
    }

    private void verifyCorrectNumberOfTestcases(final File generated, final int expectedNumberOfTestcases) throws IOException {
        assertThat(walk(generated.toPath()).filter(path -> path.toString().endsWith("testcase.json")).count()).isEqualTo(expectedNumberOfTestcases);
    }

    @Test
    public void reGenerateCombinedReport() throws IOException {
        uploadXmlReport("report1", requestCallback);
        uploadXmlReport("report2", requestCallback1);
        String url = "/reports/combined/dailyReport?executionId=report1";
        ClientHttpResponse response = restTemplate.execute(url, GET, null, responseExtractor);
        File generated1 = unpackReport(response);
        verifyJenkinsLogPlugin(generated1, "AutoProvisioningOdin_ScrumU.log");
        verifyLogsInCorrectLocation(generated1, "AutoProvisioningOdin_ScrumU.log");
        verifyEnvironmentProperties(generated1);
        checkStaticContent("dailyReport");

        url = "/reports/combined/dailyReport?executionId=report2";
        response = restTemplate.execute(url, GET, null, responseExtractor);
        File generated = unpackReport(response);
        verifyJenkinsLogPlugin(generated, "Http_Tool.log");
        verifyLogsInCorrectLocation(generated, "Http_Tool.log");
        checkStaticContent("dailyReport");

    }

    private void assertResourceExists(String url, final String reportId) throws IOException {
        ClientHttpResponse response = restTemplate.execute("/reports/" + reportId + url, GET, requestCallback, responseExtractor);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
    }

    private ClientHttpResponse uploadXmlReport(final String reportId, final RequestCallback requestCallback) throws IOException {
        String url = "/reports/" + reportId;
        ClientHttpResponse response = restTemplate.execute(url, PUT, requestCallback, responseExtractor);
        assertThat(response.getStatusCode())
                .as("Response code")
                .isEqualTo(OK);
        return response;
    }

    private void deleteReportsDirectory() throws IOException {
        final List<String> reportIds = Stream.of(REPORT_ID, "report1", "report2", "dailyReport").collect(Collectors.toList());
        for(String reportId: reportIds) {
            Path report = storageService.getRootDirectory().resolve(reportId);
            if (Files.exists(report)) {
                walk(report).sorted(reverseOrder()).forEach(safely(Files::delete));
            }
        }
    }

    private static List<String> getZipEntries(File archive) {
        List<String> actualZipEntries = newArrayList();
        ZipUtil.iterate(archive, zipEntry -> {
            // exclude random file generation
            if (!zipEntry.getName().endsWith("-testcase.json")) {
                actualZipEntries.add(zipEntry.getName());
            }
        });
        return actualZipEntries;
    }

    private static RequestCallback createRequestCallback(String resourceName) throws URISyntaxException, IOException {
        final InputStream inputStream = createInputStream(resourceName);
        return request -> {
            request.getHeaders().setContentType(APPLICATION_OCTET_STREAM);
            IOUtils.copy(inputStream, request.getBody());
        };
    }

    private File save(byte[] data) throws IOException {
        String fileName = UUID.randomUUID().toString() + ".zip";
        Path filePath = tempDirRule.newFile(fileName).toPath();
        Files.write(filePath, data);
        return filePath.toFile();
    }

    private static InputStream createInputStream(String fileName) throws URISyntaxException, IOException {
        return newInputStream(getResource(fileName));
    }

    private static Path getResource(String resourceName) throws URISyntaxException {
        return Paths.get(Resources.getResource(resourceName).toURI());
    }

    private void logHeader(ClientHttpResponse response) {
        LOGGER.info("- response headers: " + response.getHeaders());
    }
}
