package com.ericsson.de.allure.service.presentation.controllers.scenario;

import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.ui.BrowserTab;
import com.ericsson.de.allure.service.presentation.controllers.view.PriorityPluginView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class PriorityTabScenario {

    private static final Logger LOGGER = LoggerFactory.getLogger(PriorityTabScenario.class);

    private PriorityPluginView view;

    public PriorityTabScenario(BrowserTab tab) {
        LOGGER.info("Load Allure 'Priority Defects' Report");
        view = tab.getView(PriorityPluginView.class);
        view.waitForPageLoad();
    }

    @TestStep(id = "Priority Defects Tab")
    public void run() {
        assertThat(view.isDisplayedPrioritiesTab()).isTrue();

        LOGGER.info("Go to Defects by priorities page");
        view.goToDefectsByPriorities();
        assertThat(view.isDisplayedPrioritiesPane()).isTrue();

        LOGGER.info("Check all table rows with it's test-suites");
        assertThat(view.getTableRowsCount()).isEqualTo(2);
        assertSuitesInTable(0, "BLOCKER", "1", 1);
        assertSuitesInTable(1, "NORMAL", "1", 1);

        LOGGER.info("Expand first priority row and click on test-suite");
        view.expandPriorityRow(0);

        LOGGER.info("Click on first Suite to Open Test Cases pane");
        view.clickOnSuiteRow(0);
        assertThat(view.isDisplayedTestCasesPane()).isTrue();
    }

    private void assertSuitesInTable(
            int priorityRowIndex,
            String priorityName,
            String testCasesCount,
            int testSuitesCount) {

        assertThat(view.getPriorityRowCellsCount(priorityRowIndex)).isEqualTo(3);

        assertThat(view.getPriorityRowCellText(priorityRowIndex, 1)).isEqualTo(priorityName);
        assertThat(view.getPriorityRowCellText(priorityRowIndex, 2)).isEqualTo(testCasesCount);

        view.expandPriorityRow(priorityRowIndex);
        assertThat(view.getSuitesRowsCount()).isEqualTo(testSuitesCount);
        view.collapsePriorityRow(priorityRowIndex);
    }

}
