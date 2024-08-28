package com.ericsson.de.allure.plugin;

import java.util.Map;

import com.google.common.collect.Maps;

import ru.yandex.qatools.allure.model.Status;

public class TeamNameItem {

    private Map<String, TeamNameTestSuiteInfo> suitesMap = Maps.newHashMap();

    private String uid;
    private String teamName;
    private int testCasesPassedCount = 0;
    private int testCasesCanceledCount = 0;
    private int testCasesBrokenCount = 0;
    private int testCasesFailedCount = 0;
    private int testCasesPendingCount = 0;
    private int testCasesTotalCount = 0;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public int getTestCasesPassedCount() {
        return testCasesPassedCount;
    }

    public void setTestCasesPassedCount(final int testCasesPassedCount) {
        this.testCasesPassedCount = testCasesPassedCount;
    }

    public int getTestCasesCanceledCount() {
        return testCasesCanceledCount;
    }

    public void setTestCasesCanceledCount(final int testCasesCanceledCount) {
        this.testCasesCanceledCount = testCasesCanceledCount;
    }

    public int getTestCasesBrokenCount() {
        return testCasesBrokenCount;
    }

    public void setTestCasesBrokenCount(final int testCasesBrokenCount) {
        this.testCasesBrokenCount = testCasesBrokenCount;
    }

    public int getTestCasesFailedCount() {
        return testCasesFailedCount;
    }

    public void setTestCasesFailedCount(final int testCasesFailedCount) {
        this.testCasesFailedCount = testCasesFailedCount;
    }

    public int getTestCasesPendingCount() {
        return testCasesPendingCount;
    }

    public void setTestCasesPendingCount(final int testCasesPendingCount) {
        this.testCasesPendingCount = testCasesPendingCount;
    }

    public Map<String, TeamNameTestSuiteInfo> getSuitesMap() {
        return suitesMap;
    }

    public int getTestCasesCount() {
        return testCasesTotalCount;
    }

    public void increaseTestCaseCount() {
        testCasesTotalCount++;
    }

    public boolean isSuiteExist(String teamName) {
        return suitesMap.containsKey(teamName);
    }

    public void updateStatistic(final Status status) {
        switch (status.toString().toLowerCase()) {
            case "broken":
                setTestCasesBrokenCount(getTestCasesBrokenCount() + 1);
                break;
            case "failed":
                setTestCasesFailedCount(getTestCasesFailedCount() + 1);
                break;
            case "passed":
                setTestCasesPassedCount(getTestCasesPassedCount() + 1);
                break;
            case "CANCELED":
                setTestCasesCanceledCount(getTestCasesCanceledCount() + 1);
                break;
            case "PENDING":
                setTestCasesPendingCount(getTestCasesPendingCount() + 1);
                break;
            default:
                break;
        }
    }
}
