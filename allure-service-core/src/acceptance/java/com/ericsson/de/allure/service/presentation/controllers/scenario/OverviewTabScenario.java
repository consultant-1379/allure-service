package com.ericsson.de.allure.service.presentation.controllers.scenario;

import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.ui.BrowserTab;
import com.ericsson.de.allure.service.presentation.controllers.view.OverviewTabView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class OverviewTabScenario {

    private static final Logger LOGGER = LoggerFactory.getLogger(OverviewTabScenario.class);

    private OverviewTabView view;

    public OverviewTabScenario(BrowserTab tab) {
        LOGGER.info("Load Allure 'Overview' Report");
        view = tab.getView(OverviewTabView.class);
        view.goToOverviewTab();
        tab.waitUntilComponentIsDisplayed(view.getEnvSection(), 10000);
    }

    @TestStep(id = "Overview Tab")
    public void run() {
        assertThat(view.isEnvSectionVisible()).isTrue();
        Map<String, String> envData = view.getEnvironmentData();
        LOGGER.info("Verifying trigger environment data are present");
        assertThat(envData.get("SUT")).isEqualTo("localhost");
    }

}
