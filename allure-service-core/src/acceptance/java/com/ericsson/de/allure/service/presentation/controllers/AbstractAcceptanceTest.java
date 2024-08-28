package com.ericsson.de.allure.service.presentation.controllers;

import com.ericsson.cifwk.taf.configuration.TafDataHandler;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.HostType;
import com.ericsson.cifwk.taf.ui.Browser;
import com.ericsson.cifwk.taf.ui.BrowserType;
import com.ericsson.cifwk.taf.ui.UI;
import com.ericsson.de.allure.service.AllureServiceApplication;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static com.ericsson.cifwk.taf.ui.spi.TafPropertiesKeyMapper.TAF_UI_DEFAULT_OS_PROPERTY;
import static com.ericsson.de.allure.service.infrastructure.Profiles.ACCEPTANCE_TEST;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@RunWith(SpringRunner.class)
@ActiveProfiles(ACCEPTANCE_TEST)
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = AllureServiceApplication.class)
public abstract class AbstractAcceptanceTest {

    static final String TEST_ENV_BASE_URL = "http://atvts3316.athtem.eei.ericsson.se/api";

    @LocalServerPort
    private int randomServerPort;

    String getBaseUrl() {
        if (isSeleniumGridExecution()) {
            return TEST_ENV_BASE_URL;
        }
        return "http://localhost:" + randomServerPort;
    }

    @BeforeClass
    public static void setUpGridConfiguration() throws Exception {
        System.setProperty(TAF_UI_DEFAULT_OS_PROPERTY, "LINUX");
    }

    @AfterClass
    public static void tearDownGridConfiguration() throws Exception {
        System.getProperties().remove(TAF_UI_DEFAULT_OS_PROPERTY);
    }

    Browser createBrowser() {
        if (isSeleniumGridExecution()) {
            // could be modified depending on currently used grid
            return UI.newBrowser(BrowserType.FIREFOX);
        }

        // for local execution - download http://chromedriver.storage.googleapis.com/2.30/chromedriver_win32.zip
        return UI.newBrowser(BrowserType.CHROME);
    }

    boolean isSeleniumGridExecution() {
        List<Host> gridHosts = TafDataHandler.findHost().withType(HostType.SELENIUM_GRID.getName()).getAll();
        return !gridHosts.isEmpty();
    }

}
