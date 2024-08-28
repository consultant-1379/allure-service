package com.ericsson.de.allure.plugin;

import java.util.List;

import com.google.common.collect.Lists;

import ru.yandex.qatools.allure.data.AllureTestCaseInfo;
import ru.yandex.qatools.allure.data.AllureTestSuiteInfo;
import ru.yandex.qatools.allure.model.Status;


public class NodeTypeTestSuiteInfo extends AllureTestSuiteInfo {
    private List<AllureTestCaseInfo> testCases = Lists.newArrayList();

    private int passedCount = 0;
    private int canceledCount = 0;
    private int brokenCount = 0;
    private int failedCount = 0;
    private int pendingCount = 0;
    private int totalCount = 0;

    public NodeTypeTestSuiteInfo(AllureTestSuiteInfo suite) {
        this.setUid(suite.getUid());
        this.setName(suite.getName());
        this.setTitle(suite.getTitle());
    }

    public int getPassedCount() {
        return passedCount;
    }

    public void setPassedCount(final int passedCount) {
        this.passedCount = passedCount;
    }

    public int getCanceledCount() {
        return canceledCount;
    }

    public void setCanceledCount(final int canceledCount) {
        this.canceledCount = canceledCount;
    }

    public int getBrokenCount() {
        return brokenCount;
    }

    public void setBrokenCount(final int brokenCount) {
        this.brokenCount = brokenCount;
    }

    public int getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(final int failedCount) {
        this.failedCount = failedCount;
    }

    public int getPendingCount() {
        return pendingCount;
    }

    public void setPendingCount(final int pendingCount) {
        this.pendingCount = pendingCount;
    }

    public List<AllureTestCaseInfo> getTestCases() {
        return testCases;
    }

    public void addTestCase(AllureTestCaseInfo testCase) {
        testCases.add(testCase);
    }

    public int incrementTotalCount() {
        return totalCount++;
    }

    public void setTotalCount(final int totalCount) {
        this.totalCount = totalCount;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void updateStatistic(final Status status) {
        switch (status.toString().toLowerCase()) {
            case "broken":
                setBrokenCount(getBrokenCount() + 1);
                break;
            case "failed":
                setFailedCount(getFailedCount() + 1);
                break;
            case "passed":
                setPassedCount(getPassedCount() + 1);
                break;
            case "canceled":
                setCanceledCount(getCanceledCount() + 1);
                break;
            case "pending":
                setPendingCount(getPendingCount() + 1);
                break;
            default:
                break;
        }
    }
}
