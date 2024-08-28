package com.ericsson.de.allure.service.presentation.controllers;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.UUID;

import static com.ericsson.de.allure.service.presentation.controllers.AbstractAcceptanceTest.TEST_ENV_BASE_URL;
import static java.nio.file.Files.createTempDirectory;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 30.06.2017
 */
public class AllureClientTest {

    private static final Logger LOG = LoggerFactory.getLogger(AllureClientTest.class);

    private static final String REPORT_URL = "http://localhost:8080/reports/123";

    private static final String BROWSER_URL = "http://localhost:8080/reports/123/index.html";

    private static final String XML_REPORT = "allure_xml_results.zip";

    private static final String BASE_URL = "http://localhost:8080";
    private static final String COMBINED_URL = "http://localhost:8080/reports/combined/123?executionId=abc&executionId=def&executionId=ghi";

    private AllureClient client;

    @Before
    public void setUp() {
        client = new AllureClient("http://localhost:8080");
    }

    @After
    public void tearDown() {
        client.close();
    }

    @Test
    public void constructor() {
        assertBaseUrl(BASE_URL);
        assertBaseUrl(BASE_URL + "/");
    }

    private void assertBaseUrl(String providedUrl) {
        AllureClient client = new AllureClient(providedUrl);
        assertThat(client.getBaseUrl()).isEqualTo(BASE_URL + "/");
        client.close();
    }

    @Test
    public void get_urls() {
        assertThat(client.getUploadUrl("123"))
                .as("Upload URL")
                .isEqualTo(REPORT_URL);
        assertThat(client.getDownloadUrl("123"))
                .as("Download URL")
                .isEqualTo(REPORT_URL);
        assertThat(client.getReportUrl("123"))
                .as("Report URL")
                .isEqualTo(BROWSER_URL);
        assertThat(client.getCombinedUrl("123", "abc", "def", "ghi"))
                .as("Combined URL")
                .isEqualTo(COMBINED_URL);
    }

    @Test
    public void integration_test_against_test_SUT() throws IOException {

        // input
        client = new AllureClient(TEST_ENV_BASE_URL);
        String reportId = UUID.randomUUID().toString();
        InputStream xmlZip = AllureClientTest.class.getClassLoader().getResourceAsStream(XML_REPORT);

        // output
        File htmlZip = setupOutput();

        // client execution
        client.uploadReport(reportId, xmlZip);
        client.downloadReport(reportId, new FileOutputStream(htmlZip));
        LOG.info("HTML Report downloaded to {}", htmlZip.getAbsolutePath());
    }

    @Test
    public void combined_report_test_against_test_sut() throws IOException{
        // input
        client = new AllureClient(TEST_ENV_BASE_URL);
        InputStream xmlZip1 = AllureClientTest.class.getClassLoader().getResourceAsStream("results1.zip");
        InputStream xmlZip2 = AllureClientTest.class.getClassLoader().getResourceAsStream("results2.zip");

        // output
        File htmlZip = setupOutput();

        // client execution
        client.uploadReport("report1", xmlZip1);
        client.uploadReport("report2", xmlZip2);
        client.downloadCombinedReport("combinedReport", new FileOutputStream(htmlZip), "report1", "report2");
        LOG.info("HTML Report downloaded to {}", htmlZip.getAbsolutePath());
    }

    private File setupOutput() throws IOException {
        Path tempDir = createTempDirectory("AllureClientTest");
        File htmlZip = tempDir.resolve("AllureHtmlReport.zip").toFile();
        htmlZip.createNewFile();
        return htmlZip;
    }
}
