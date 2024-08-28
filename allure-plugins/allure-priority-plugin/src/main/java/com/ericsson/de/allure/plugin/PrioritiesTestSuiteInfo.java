package com.ericsson.de.allure.plugin;

import com.google.common.collect.Lists;
import ru.yandex.qatools.allure.data.AllureTestCaseInfo;
import ru.yandex.qatools.allure.data.AllureTestSuiteInfo;

import java.util.List;

public class PrioritiesTestSuiteInfo extends AllureTestSuiteInfo {

    private List<AllureTestCaseInfo> testCases = Lists.newArrayList();

    public PrioritiesTestSuiteInfo(AllureTestSuiteInfo suite) {
        this.setUid(suite.getUid());
        this.setName(suite.getName());
        this.setTitle(suite.getTitle());
    }

    public List<AllureTestCaseInfo> getTestCases() {
        return testCases;
    }

    public void addTestCase(AllureTestCaseInfo testCase) {
        testCases.add(testCase);
    }

    public int getTestCasesCount() {
        return testCases.size();
    }

}
