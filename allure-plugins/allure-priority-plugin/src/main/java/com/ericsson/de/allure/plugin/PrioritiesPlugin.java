package com.ericsson.de.allure.plugin;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import ru.yandex.qatools.allure.data.AllureTestCase;
import ru.yandex.qatools.allure.data.AllureTestCaseInfo;
import ru.yandex.qatools.allure.data.AllureTestSuiteInfo;
import ru.yandex.qatools.allure.data.plugins.DefaultTabPlugin;
import ru.yandex.qatools.allure.data.plugins.Plugin;
import ru.yandex.qatools.allure.data.utils.PluginUtils;
import ru.yandex.qatools.allure.data.utils.TextUtils;
import ru.yandex.qatools.allure.model.SeverityLevel;

import java.util.List;
import java.util.Map;

import static ru.yandex.qatools.allure.model.Status.BROKEN;
import static ru.yandex.qatools.allure.model.Status.FAILED;

@Plugin.Name("priorities")
@Plugin.Priority(601)
public class PrioritiesPlugin extends DefaultTabPlugin {

    @Plugin.Data
    Map<SeverityLevel, DefectGroupItem> defectsBySeverity = Maps.newHashMap();

    private final List statuses = Lists.newArrayList(BROKEN, FAILED);

    /**
     * Process given test cases and group by priority
     */
    @Override
    public void process(AllureTestCase testCase) {
        if (!statuses.contains(testCase.getStatus())) {
            return;
        }

        SeverityLevel severity = testCase.getSeverity();
        DefectGroupItem defectGroupItem = createAndGetDefectGroupItem(severity);
        defectGroupItem.increaseTestCaseCount();

        PrioritiesTestSuiteInfo testSuiteInfo = createAndGetTestSuiteInfo(testCase, severity, defectGroupItem);

        testSuiteInfo.addTestCase((AllureTestCaseInfo) PluginUtils.toInfo(testCase));
    }

    private PrioritiesTestSuiteInfo createAndGetTestSuiteInfo(AllureTestCase testCase, SeverityLevel severity, DefectGroupItem defectGroupItem) {
        AllureTestSuiteInfo suite = testCase.getSuite();
        String severitySuiteUid = severity.name() + ":" + suite.getUid();
        Map<String, PrioritiesTestSuiteInfo> suitesMap = defectGroupItem.getSuitesMap();
        if (!defectGroupItem.isSuiteExist(severitySuiteUid)) {
            PrioritiesTestSuiteInfo testSuite = new PrioritiesTestSuiteInfo(suite);
            suitesMap.put(severitySuiteUid, testSuite);
        }
        return suitesMap.get(severitySuiteUid);
    }

    private DefectGroupItem createAndGetDefectGroupItem(SeverityLevel severity) {
        if (!defectsBySeverity.containsKey(severity)) {
            DefectGroupItem defectItem = new DefectGroupItem();
            defectItem.setUid(TextUtils.generateUid());
            defectItem.setSeverity(severity);

            defectsBySeverity.put(severity, defectItem);
        }
        return defectsBySeverity.get(severity);
    }

}
