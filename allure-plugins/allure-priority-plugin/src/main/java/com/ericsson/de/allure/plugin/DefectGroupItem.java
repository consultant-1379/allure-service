package com.ericsson.de.allure.plugin;

import com.google.common.collect.Maps;
import ru.yandex.qatools.allure.model.SeverityLevel;

import java.util.Map;

public class DefectGroupItem {

    private String uid;
    private SeverityLevel severity;
    private int testCasesCount = 0;
    private Map<String, PrioritiesTestSuiteInfo> suitesMap = Maps.newHashMap();

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public SeverityLevel getSeverity() {
        return severity;
    }

    public void setSeverity(SeverityLevel severity) {
        this.severity = severity;
    }

    public Map<String, PrioritiesTestSuiteInfo> getSuitesMap() {
        return suitesMap;
    }

    public boolean isSuiteExist(String severitySuiteUid) {
        return suitesMap.containsKey(severitySuiteUid);
    }

    public void increaseTestCaseCount() {
        testCasesCount++;
    }

    public int getTestCasesCount() {
        return testCasesCount;
    }

    public void setTestCasesCount(int testCasesCount) {
        this.testCasesCount = testCasesCount;
    }
}
