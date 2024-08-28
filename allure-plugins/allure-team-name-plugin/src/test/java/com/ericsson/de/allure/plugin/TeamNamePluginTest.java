package com.ericsson.de.allure.plugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import static junit.framework.Assert.assertNotNull;
import static ru.yandex.qatools.allure.model.Status.BROKEN;
import static ru.yandex.qatools.allure.model.Status.FAILED;
import static ru.yandex.qatools.allure.model.Status.PASSED;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.de.allure.plugin.common.TafAllureSuiteUtils;

import ru.yandex.qatools.allure.data.AllureTestCase;
import ru.yandex.qatools.allure.data.AllureTestSuiteInfo;
import ru.yandex.qatools.allure.model.Parameter;
import ru.yandex.qatools.allure.model.ParameterKind;
import ru.yandex.qatools.allure.model.Status;

@RunWith(MockitoJUnitRunner.class)
public class TeamNamePluginTest {

    TeamNamePlugin plugin;

    @Before
    public void setUp() {
        plugin = new TeamNamePlugin();
    }

    @Test
    public void shouldAddTestCase() {
        AllureTestSuiteInfo suite = TafAllureSuiteUtils.createSuite("UID1", "Suite Name", "Suite Title");
        List<Parameter> parameters = getParameter("ERBS");

        plugin.process(createTestCase("Passed TestCase", parameters, PASSED, suite));

        assertTrue(plugin.suitesByTeamName.size() == 1);
    }

    @Test
    public void shouldNotAddTestCaseWhenParameterIsMissing() {
        AllureTestSuiteInfo suite = TafAllureSuiteUtils.createSuite("UID1", "Suite Name", "Suite Title");
        List<Parameter> parameters = new ArrayList<>();
        plugin.process(createTestCase("Passed TestCase", parameters, PASSED, suite));

        assertTrue(plugin.suitesByTeamName.isEmpty());
    }

    @Test
    public void shouldAddAndGroupTestCasesByNodeType() {
        AllureTestSuiteInfo suite1 = TafAllureSuiteUtils.createSuite("UID1", "Suite1", "Suite Nr1");
        AllureTestSuiteInfo suite2 = TafAllureSuiteUtils.createSuite("UID2", "Suite2", "Suite Nr2");

        plugin.process(createTestCase("S1:1st NodeType passed TestCase", getParameter("ERBS"), PASSED, suite1));
        plugin.process(createTestCase("S1:2nd NodeType Failed TestCase", getParameter("SGSN"), FAILED, suite1));
        plugin.process(createTestCase("S2:3rd NodeType Broken TestCase", getParameter("ERBS"), BROKEN, suite2));
        plugin.process(createTestCase("S1:4th No NodeType Failed TestCase", new ArrayList<>(), FAILED, suite1));
        plugin.process(createTestCase("S1:5th No NodeType Broken TestCase", new ArrayList<>(), BROKEN, suite1));
        plugin.process(createTestCase("S2:6th No NodeType PASSED TestCase", new ArrayList<>(), PASSED, suite2));
        plugin.process(createTestCase("S2:6th NodeType Passed TestCase", new ArrayList<>(), BROKEN, suite2));

        Map<String, TeamNameItem> suitesByNodeType = plugin.suitesByTeamName;
        assertEquals(suitesByNodeType.size(), 2);

        TeamNameItem teamNameItem = suitesByNodeType.get("ERBS");
        assertTrue(teamNameItem.getSuitesMap().size() == 2);
        assertTrue(teamNameItem.getTestCasesCount() == 2);
        TeamNameItem teamNameItem1 = suitesByNodeType.get("SGSN");
        assertTrue(teamNameItem1.getSuitesMap().size() == 1);
        assertTrue(teamNameItem1.getTestCasesCount() == 1);

        Map<String, TeamNameTestSuiteInfo> suitesMap = teamNameItem.getSuitesMap();
        assertSuiteDetails(suitesMap, "ERBS", suite1.getUid(), 1);

        Map<String, TeamNameTestSuiteInfo> suitesMap1 = teamNameItem1.getSuitesMap();
        assertSuiteDetails(suitesMap1, "SGSN", suite1.getUid(), 1);

        Map<String, TeamNameTestSuiteInfo> suitesMap3 = teamNameItem.getSuitesMap();
        assertSuiteDetails(suitesMap, "ERBS", suite2.getUid(), 1);
    }

    private List<Parameter> getParameter(String teamName) {
        Parameter parameter = new Parameter();
        parameter.setName("teamName");
        parameter.setValue(teamName);
        parameter.setKind(ParameterKind.ARGUMENT);
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(parameter);
        return parameters;
    }

    private void assertSuiteDetails(
            Map<String, TeamNameTestSuiteInfo> suitesMap,
            String teamName,
            String suiteUid,
            int testCasesCount) {

        TeamNameTestSuiteInfo actualSuite = suitesMap.get(teamName + ":" + suiteUid);
        assertNotNull(actualSuite);
        assertEquals(testCasesCount, actualSuite.getTotalCount());
    }


    private AllureTestCase createTestCase(String name, List<Parameter> parameter, Status status, AllureTestSuiteInfo suite) {
        AllureTestCase testCase = new AllureTestCase();
        testCase.setName(name);
        testCase.setStatus(status);
        testCase.setParameters(parameter);
        testCase.setSuite(suite);
        return testCase;
    }

}
