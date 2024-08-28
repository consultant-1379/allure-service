package com.ericsson.de.allure.service.presentation.controllers;

import com.ericsson.cifwk.taf.ui.Browser;
import com.ericsson.cifwk.taf.ui.BrowserTab;
import com.ericsson.de.allure.service.presentation.controllers.scenario.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.UUID;

import static org.apache.commons.io.output.NullOutputStream.NULL_OUTPUT_STREAM;

// performance optimization possible - do not start embedded server if tested against remote SUT
public class ReportUiAcceptanceTest extends AbstractAcceptanceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportUiAcceptanceTest.class);

    private static final String XML_REPORT = "allure_xml_results.zip";

    private String reportId = UUID.randomUUID().toString();

    private AllureClient allureClient;

    private Browser browser;

    @Before
    public void setUp() throws Exception {
        allureClient = new AllureClient(getBaseUrl());
        browser = createBrowser();
    }

    @After
    public void tearDown() {
        browser.close();
        allureClient.close();
    }

    @Test
    public void inspect_allure_report_UI() throws Exception {

        // generating report
        InputStream xmlReport = ReportUiAcceptanceTest.class.getClassLoader().getResourceAsStream(XML_REPORT);
        allureClient.uploadReport(reportId, xmlReport);
        allureClient.downloadReport(reportId, NULL_OUTPUT_STREAM);

        // opening report
        String browserUrl = allureClient.getReportUrl(reportId);
        LOGGER.info("Opening page {}", browserUrl);
        BrowserTab tab = browser.open(browserUrl);
        new OverviewTabScenario(tab).run();
        new XUnitTabScenario(tab)
                .hasSuites(2)
                .hasSuites("Failed to get executed", "AutoProvisioning - Odin_ScrumU");
        new PriorityTabScenario(tab).run();
        new JenkinsLogsTabScenario(tab)
                .hasLogs(1)
                .hasLogAtIndex("AutoProvisioningOdin_ScrumU.log", 0);
    }

    @Test
    public void inspect_combined_report_UI() throws Exception{
        InputStream report1 = ReportUiAcceptanceTest.class.getClassLoader().getResourceAsStream("results1.zip");
        InputStream report2 = ReportUiAcceptanceTest.class.getClassLoader().getResourceAsStream("results2.zip");
        allureClient.uploadReport("report1", report1);
        allureClient.uploadReport("report2", report2);
        allureClient.downloadCombinedReport("dailyReport", NULL_OUTPUT_STREAM, "report1", "report2");

        String browserUrl = allureClient.getReportUrl("dailyReport");
        LOGGER.info("Opening page {}", browserUrl);
        BrowserTab tab = browser.open(browserUrl);
        new XUnitTabScenario(tab)
                .hasSuites(1)
                .hasTestCasesInSuite(14, "Http_Tool_Suite");
        new JenkinsLogsTabScenario(tab)
                .hasLogs(2)
                .hasLogAtIndex("Http_Tool.log.1", 1);
    }

}
