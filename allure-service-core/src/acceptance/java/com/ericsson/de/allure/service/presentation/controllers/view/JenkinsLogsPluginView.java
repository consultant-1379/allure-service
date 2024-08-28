package com.ericsson.de.allure.service.presentation.controllers.view;

import com.ericsson.cifwk.taf.ui.core.SelectorType;
import com.ericsson.cifwk.taf.ui.core.UiComponent;
import com.ericsson.cifwk.taf.ui.core.UiComponentMapping;
import com.ericsson.cifwk.taf.ui.sdk.GenericViewModel;
import com.ericsson.cifwk.taf.ui.sdk.Link;

import java.util.List;

public class JenkinsLogsPluginView extends GenericViewModel {

    private static final int SHORT_WAIT_TIMEOUT = 1000;

    private static final String PAGE_CONTENT = ".tab-content";
    private static final String JENKINS_LOGS_LIST_TABLE = ".jenkinsLogs-list";
    private static final String PRIORITIES_TABLE_PRIORITY_ROW = "tbody > tr.jenkins-log-row";
    private static final String PRIORITY_TABLE_CELL = "td";

    @UiComponentMapping(selectorType = SelectorType.XPATH, selector = "//a[@ui-sref='jenkins-logs']")
    private Link jenkinsLogsLink;

    @UiComponentMapping(".jenkinsLogs")
    private UiComponent jenkinsLogsPane;

    @UiComponentMapping(".attachment")
    private UiComponent attachmentPane;

    @UiComponentMapping(JENKINS_LOGS_LIST_TABLE)
    private UiComponent jenkinsLogsTable;

    @UiComponentMapping(JENKINS_LOGS_LIST_TABLE + " > " + PRIORITIES_TABLE_PRIORITY_ROW)
    private List<UiComponent> jenkinsLogsRows;

    public void waitForPageLoad() {
        waitUntilComponentIsDisplayed(PAGE_CONTENT, SHORT_WAIT_TIMEOUT);
    }

    public boolean isDisplayedJenkinsLogsTab() {
        return jenkinsLogsLink.isDisplayed();
    }

    public boolean isDisplayedJenkinsLogsPane() {
        return jenkinsLogsPane.isDisplayed();
    }
    public boolean isDisplayedAttachmentPane() {
        return attachmentPane.isDisplayed();
    }

    public void goToJenkinsLogs() {
        jenkinsLogsLink.click();
        waitUntilComponentIsDisplayed(jenkinsLogsPane, SHORT_WAIT_TIMEOUT);
    }

    public int getTableRowsCount() {
        return jenkinsLogsRows.size();
    }

    public int getJenkinsLogRowCellCount(int index) {
        return jenkinsLogsRows.get(index).getDescendantsBySelector(PRIORITY_TABLE_CELL).size();
    }

    public String getJenkinsLogRowCellText(int rowIndex, int cellIndex) {
        List<UiComponent> cells = jenkinsLogsRows.get(rowIndex).getDescendantsBySelector(PRIORITY_TABLE_CELL);
        return cells.get(cellIndex).getText();
    }

    public void clickOnJenkinsLogEntry(int index) {
        List<UiComponent> cells = jenkinsLogsRows.get(index).getDescendantsBySelector(PRIORITY_TABLE_CELL);
        cells.get(0).click();
    }
}
