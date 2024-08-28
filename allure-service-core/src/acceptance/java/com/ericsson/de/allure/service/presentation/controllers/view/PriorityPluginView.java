package com.ericsson.de.allure.service.presentation.controllers.view;

import com.ericsson.cifwk.taf.ui.core.SelectorType;
import com.ericsson.cifwk.taf.ui.core.UiComponent;
import com.ericsson.cifwk.taf.ui.core.UiComponentMapping;
import com.ericsson.cifwk.taf.ui.sdk.GenericViewModel;
import com.ericsson.cifwk.taf.ui.sdk.Link;

import java.util.List;

public class PriorityPluginView extends GenericViewModel {

    private static final int SHORT_WAIT_TIMEOUT = 1000;

    private static final String PAGE_CONTENT = ".tab-content";
    private static final String PRIORITIES_LIST_TABLE = ".priorities-list";
    private static final String PRIORITIES_TABLE_PRIORITY_ROW = "tbody > tr.defect-by-priority";
    private static final String PRIORITIES_TABLE_SUITE_ROW = "tbody > tr.suite";
    private static final String PRIORITY_TABLE_CELL = "td";

    @UiComponentMapping(selectorType = SelectorType.XPATH, selector = "//a[@ui-sref='priorities']")
    private Link prioritiesLink;

    @UiComponentMapping(".priorities")
    private UiComponent prioritiesPane;

    @UiComponentMapping(".testCases-pane")
    private UiComponent testCasesPane;

    @UiComponentMapping(PRIORITIES_LIST_TABLE)
    private UiComponent prioritiesTable;

    @UiComponentMapping(PRIORITIES_LIST_TABLE + " > " + PRIORITIES_TABLE_PRIORITY_ROW)
    private List<UiComponent> prioritiesRows;

    @UiComponentMapping(PRIORITIES_LIST_TABLE + " > " + PRIORITIES_TABLE_SUITE_ROW)
    private List<UiComponent> suitesRows;

    public void waitForPageLoad() {
        waitUntilComponentIsDisplayed(PAGE_CONTENT, SHORT_WAIT_TIMEOUT);
    }

    public boolean isDisplayedPrioritiesTab() {
        return prioritiesLink.isDisplayed();
    }

    public boolean isDisplayedPrioritiesPane() {
        return prioritiesPane.isDisplayed();
    }

    public boolean isDisplayedTestCasesPane() {
        return testCasesPane.isDisplayed();
    }

    public void goToDefectsByPriorities() {
        prioritiesLink.click();
        waitUntilComponentIsDisplayed(prioritiesPane, SHORT_WAIT_TIMEOUT);
    }

    public int getTableRowsCount() {
        return prioritiesRows.size();
    }

    public int getPriorityRowCellsCount(int index) {
        return prioritiesRows.get(index).getDescendantsBySelector(PRIORITY_TABLE_CELL).size();
    }

    public String getPriorityRowCellText(int rowIndex, int cellIndex) {
        List<UiComponent> cells = prioritiesRows.get(rowIndex).getDescendantsBySelector(PRIORITY_TABLE_CELL);
        return cells.get(cellIndex).getText();
    }

    public void expandPriorityRow(int index) {
        prioritiesRows.get(index).click();
        waitUntilComponentIsDisplayed(PRIORITIES_LIST_TABLE + " > " + PRIORITIES_TABLE_SUITE_ROW, SHORT_WAIT_TIMEOUT);
    }

    public void collapsePriorityRow(int index) {
        prioritiesRows.get(index).click();
    }

    public int getSuitesRowsCount() {
        return suitesRows.size();
    }

    public void clickOnSuiteRow(int index) {
        suitesRows.get(index).click();
        waitUntilComponentIsDisplayed(testCasesPane, SHORT_WAIT_TIMEOUT);
    }
}
