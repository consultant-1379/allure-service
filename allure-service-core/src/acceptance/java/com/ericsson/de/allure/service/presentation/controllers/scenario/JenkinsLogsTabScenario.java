package com.ericsson.de.allure.service.presentation.controllers.scenario;

import com.ericsson.cifwk.taf.ui.BrowserTab;
import com.ericsson.de.allure.service.presentation.controllers.view.JenkinsLogsPluginView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class JenkinsLogsTabScenario {

    private static final Logger LOGGER = LoggerFactory.getLogger(JenkinsLogsTabScenario.class);

    private JenkinsLogsPluginView view;

    public JenkinsLogsTabScenario(BrowserTab tab) {
        LOGGER.info("Load Allure 'Jenkins Logs' Report");
        view = tab.getView(JenkinsLogsPluginView.class);
        view.waitForPageLoad();
    }

    public JenkinsLogsTabScenario hasLogs(final int numberOfLogs) throws InterruptedException {
        assertThat(view.isDisplayedJenkinsLogsTab()).isTrue();

        LOGGER.info("Go to Jenkins Logs page");
        view.goToJenkinsLogs();
        assertThat(view.isDisplayedJenkinsLogsPane()).isTrue();

        LOGGER.info("Check all table rows with it's log files");
        assertThat(view.getTableRowsCount()).isEqualTo(numberOfLogs);
        return this;
    }

    public JenkinsLogsTabScenario hasLogAtIndex(final String jenkinsLogName, final int rowIndex){
        assertSuitesInTable(rowIndex, jenkinsLogName);

        LOGGER.info("Click first jenkins log row");
        view.clickOnJenkinsLogEntry(rowIndex);

        assertThat(view.isDisplayedAttachmentPane()).isTrue();
        return this;
    }

    private void assertSuitesInTable(
            int rowIndex,
            String jenkinsLogName) {
        assertThat(view.getJenkinsLogRowCellCount(rowIndex)).isEqualTo(2);
        assertThat(view.getJenkinsLogRowCellText(rowIndex, 0)).isEqualTo(jenkinsLogName);
    }

}
