package com.ericsson.de.allure.service.presentation.controllers.scenario;

import com.ericsson.cifwk.taf.ui.BrowserTab;
import com.ericsson.de.allure.service.presentation.controllers.view.XUnitView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

public class XUnitTabScenario {

    private static final Logger LOGGER = LoggerFactory.getLogger(XUnitTabScenario.class);

    private XUnitView view;

    public XUnitTabScenario(BrowserTab tab) {
        LOGGER.info("Load Allure 'xUnit' Report");
        view = tab.getView(XUnitView.class);
        view.goToXUnitTab();
        tab.waitUntilComponentIsDisplayed(view.getSuitesSection(), 10000);
    }

    public XUnitTabScenario hasSuites(final int numberOfSuites){
        assertTrue(view.isSuitesSectionAvailable());
        List<String> testSuites = view.getTestSuites();
        assertThat(testSuites).hasSize(numberOfSuites);
        return this;
    }

    public XUnitTabScenario hasSuites(final String... suites){
        List<String> testSuites = view.getTestSuites();
        assertThat(testSuites).containsExactlyInAnyOrder(suites);
        return this;
    }

    public XUnitTabScenario hasTestCasesInSuite(final int numberOfTestCases, final String suite){
        assertThat(view.getTestCasesForSuite(suite)).hasSize(numberOfTestCases);
        return this;
    }

}
