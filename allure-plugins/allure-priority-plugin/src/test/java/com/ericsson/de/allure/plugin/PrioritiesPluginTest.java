package com.ericsson.de.allure.plugin;

import static org.junit.Assert.assertTrue;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static ru.yandex.qatools.allure.model.SeverityLevel.CRITICAL;
import static ru.yandex.qatools.allure.model.SeverityLevel.NORMAL;
import static ru.yandex.qatools.allure.model.Status.BROKEN;
import static ru.yandex.qatools.allure.model.Status.FAILED;
import static ru.yandex.qatools.allure.model.Status.PASSED;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.de.allure.plugin.common.TafAllureSuiteUtils;

import ru.yandex.qatools.allure.data.AllureTestCase;
import ru.yandex.qatools.allure.data.AllureTestSuiteInfo;
import ru.yandex.qatools.allure.model.SeverityLevel;
import ru.yandex.qatools.allure.model.Status;

@RunWith(MockitoJUnitRunner.class)
public class PrioritiesPluginTest {

    PrioritiesPlugin plugin;

    @Before
    public void setUp() {
        plugin = new PrioritiesPlugin();
    }

    @Test
    public void shouldNotAddNotBrokenOrFailedTestCase() {
        AllureTestSuiteInfo suite = TafAllureSuiteUtils.createSuite("UID1", "Suite Name", "Suite Title");
        plugin.process(createTestCase("Passed TestCase", CRITICAL, PASSED, suite));

        assertTrue(plugin.defectsBySeverity.isEmpty());
    }

    @Test
    public void shouldAddAndGroupTestCasesBySeverity() {
        AllureTestSuiteInfo suite1 = TafAllureSuiteUtils.createSuite("UID1", "Suite1", "Suite Nr1");
        AllureTestSuiteInfo suite2 = TafAllureSuiteUtils.createSuite("UID2", "Suite2", "Suite Nr2");

        plugin.process(createTestCase("S1:1st Critical Broken TestCase", CRITICAL, BROKEN, suite1));
        plugin.process(createTestCase("S1:2nd Critical Failed TestCase", CRITICAL, FAILED, suite1));
        plugin.process(createTestCase("S2:3rd Critical Broken TestCase", CRITICAL, BROKEN, suite2));
        plugin.process(createTestCase("S1:4th Critical Failed TestCase", CRITICAL, FAILED, suite1));
        plugin.process(createTestCase("S1:5th Normal Broken TestCase", NORMAL, BROKEN, suite1));
        plugin.process(createTestCase("S2:6th Normal Failed TestCase", NORMAL, FAILED, suite2));
        plugin.process(createTestCase("S2:6th Normal Broken TestCase", NORMAL, BROKEN, suite2));

        Map<SeverityLevel, DefectGroupItem> defectsBySeverity = plugin.defectsBySeverity;
        assertEquals(defectsBySeverity.size(), 2);

        DefectGroupItem criticalDefectsGroup = defectsBySeverity.get(CRITICAL);
        assertNotNull(criticalDefectsGroup);
        assertEquals(CRITICAL, criticalDefectsGroup.getSeverity());
        Map<String, PrioritiesTestSuiteInfo> criticalSuitesMap = criticalDefectsGroup.getSuitesMap();
        assertEquals(2, criticalSuitesMap.size());
        assertEquals(4, criticalDefectsGroup.getTestCasesCount());

        assertSuiteDetails(criticalSuitesMap, CRITICAL, suite1.getUid(), 3);
        assertSuiteDetails(criticalSuitesMap, CRITICAL, suite2.getUid(), 1);

        DefectGroupItem normalDefectsGroup = defectsBySeverity.get(NORMAL);
        assertNotNull(normalDefectsGroup);
        assertEquals(NORMAL, normalDefectsGroup.getSeverity());
        Map<String, PrioritiesTestSuiteInfo> normalSuitesMap = normalDefectsGroup.getSuitesMap();
        assertEquals(2, normalSuitesMap.size());
        assertEquals(3, normalDefectsGroup.getTestCasesCount());

        assertSuiteDetails(normalSuitesMap, NORMAL, suite1.getUid(), 1);
        assertSuiteDetails(normalSuitesMap, NORMAL, suite2.getUid(), 2);
    }

    private void assertSuiteDetails(
            Map<String, PrioritiesTestSuiteInfo> suitesMap,
            SeverityLevel severityLevel,
            String suiteUid,
            int testCasesCount) {

        PrioritiesTestSuiteInfo actualSuite = suitesMap.get(severityLevel.name() + ":" + suiteUid);
        assertNotNull(actualSuite);
        assertEquals(testCasesCount, actualSuite.getTestCasesCount());
    }

    private AllureTestCase createTestCase(String name, SeverityLevel severity, Status status, AllureTestSuiteInfo suite) {
        AllureTestCase testCase = new AllureTestCase();
        testCase.setName(name);
        testCase.setStatus(status);
        testCase.setSeverity(severity);
        testCase.setSuite(suite);
        return testCase;
    }

}
