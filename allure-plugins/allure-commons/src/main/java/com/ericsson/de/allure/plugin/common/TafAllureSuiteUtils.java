package com.ericsson.de.allure.plugin.common;

import ru.yandex.qatools.allure.data.AllureTestSuiteInfo;

public class TafAllureSuiteUtils {

    private TafAllureSuiteUtils() {
    }

    public static AllureTestSuiteInfo createSuite(String uid, String name, String title) {
        AllureTestSuiteInfo suite = new AllureTestSuiteInfo();
        suite.setUid(uid);
        suite.setName(name);
        suite.setTitle(title);
        return suite;
    }
}
