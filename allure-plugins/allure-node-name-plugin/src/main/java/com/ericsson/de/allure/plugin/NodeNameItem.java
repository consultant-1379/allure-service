/*
 * ------------------------------------------------------------------------------
 * ******************************************************************************
 * COPYRIGHT Ericsson 2019
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 * ******************************************************************************
 * ----------------------------------------------------------------------------
 */

package com.ericsson.de.allure.plugin;

import java.util.Map;

import com.google.common.collect.Maps;

import ru.yandex.qatools.allure.model.Status;

/**
 * NodeNameItem.
 */
public class NodeNameItem {
    private final Map<String, NodeNameTestSuiteInfo> suitesMap = Maps.newHashMap();

    private String uid;
    private String nodeName;
    private int testCasesPassedCount;
    private int testCasesCanceledCount;
    private int testCasesBrokenCount;
    private int testCasesFailedCount;
    private int testCasesPendingCount;
    private int testCasesTotalCount;

    public String getUid() {
        return uid;
    }

    public void setUid(final String uid) {
        this.uid = uid;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(final String nodeName) {
        this.nodeName = nodeName;
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

    public Map<String, NodeNameTestSuiteInfo> getSuitesMap() {
        return suitesMap;
    }

    public int getTestCasesCount() {
        return testCasesTotalCount;
    }

    public void increaseTestCaseCount() {
        testCasesTotalCount++;
    }

    public boolean isSuiteExist(final String nodeName) {
        return suitesMap.containsKey(nodeName);
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
