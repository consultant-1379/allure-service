package plugin;

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

import com.ericsson.de.allure.plugin.NodeNameItem;
import com.ericsson.de.allure.plugin.NodeNamePlugin;
import com.ericsson.de.allure.plugin.NodeNameTestSuiteInfo;
import com.ericsson.de.allure.plugin.common.TafAllureSuiteUtils;

import ru.yandex.qatools.allure.data.AllureTestCase;
import ru.yandex.qatools.allure.data.AllureTestSuiteInfo;
import ru.yandex.qatools.allure.model.Parameter;
import ru.yandex.qatools.allure.model.ParameterKind;
import ru.yandex.qatools.allure.model.Status;

@RunWith(MockitoJUnitRunner.class)
public class NodeNamePluginTest {

    NodeNamePlugin plugin;

    @Before
    public void setUp() {
        plugin = new NodeNamePlugin();
    }

    //@Test
    public void shouldAddTestCase() {
        AllureTestSuiteInfo suite = TafAllureSuiteUtils.createSuite("UID1", "Suite Name", "Suite Title");
        List<Parameter> parameters = getParameter("LTE01");

        plugin.process(createTestCase("Passed TestCase", parameters, PASSED, suite));

        assertTrue(plugin.suitesByNodeName.size() == 1);
    }

    //@Test
    public void shouldNotAddTestCaseWhenParameterIsMissing() {
        AllureTestSuiteInfo suite = TafAllureSuiteUtils.createSuite("UID1", "Suite Name", "Suite Title");
        List<Parameter> parameters = new ArrayList<>();
        plugin.process(createTestCase("Passed TestCase", parameters, PASSED, suite));

        assertTrue(plugin.suitesByNodeName.isEmpty());
    }

    @Test
    public void shouldAddAndGroupTestCasesByNodeType() {
        AllureTestSuiteInfo suite1 = TafAllureSuiteUtils.createSuite("UID1", "Suite1", "Suite Nr1");
        AllureTestSuiteInfo suite2 = TafAllureSuiteUtils.createSuite("UID2", "Suite2", "Suite Nr2");

        plugin.process(createTestCase("S1:1st NodeName Passed TestCase", getParameter("LTE01"), PASSED, suite1));
        plugin.process(createTestCase("S1:2nd NodeName Failed TestCase", getParameter("LTE02"), FAILED, suite1));
        plugin.process(createTestCase("S2:3rd NodeName Broken TestCase", getParameter("LTE01"), BROKEN, suite2));
        //plugin.process(createTestCase("S1:4th No NodeName Failed TestCase", new ArrayList<>(), FAILED, suite1));
        //plugin.process(createTestCase("S1:5th No NodeName Broken TestCase", new ArrayList<>(), BROKEN, suite1));
        //plugin.process(createTestCase("S2:6th No NodeName Passed TestCase", new ArrayList<>(), PASSED, suite2));
        //plugin.process(createTestCase("S2:7th No NodeName Passed TestCase", new ArrayList<>(), BROKEN, suite2));

        Map<String, NodeNameItem> suitesByNodeName = plugin.suitesByNodeName;
        assertEquals(suitesByNodeName.size(), 2);

        NodeNameItem nodeNameItem1 = suitesByNodeName.get("LTE01");
        assertTrue(nodeNameItem1.getSuitesMap().size() == 2);
        assertTrue(nodeNameItem1.getTestCasesCount() == 2);
        NodeNameItem nodeNameItem2 = suitesByNodeName.get("LTE02");
        assertTrue(nodeNameItem2.getSuitesMap().size() == 1);
        assertTrue(nodeNameItem2.getTestCasesCount() == 1);

        Map<String, NodeNameTestSuiteInfo> suitesMap = nodeNameItem1.getSuitesMap();
        assertSuiteDetails(suitesMap, "LTE01", suite1.getUid(), 1);

        Map<String, NodeNameTestSuiteInfo> suitesMap1 = nodeNameItem2.getSuitesMap();
        assertSuiteDetails(suitesMap1, "LTE02", suite1.getUid(), 1);

        Map<String, NodeNameTestSuiteInfo> suitesMap3 = nodeNameItem1.getSuitesMap();
        assertSuiteDetails(suitesMap3, "LTE01", suite2.getUid(), 1);
    }

    private List<Parameter> getParameter(String nodeName) {
        Parameter parameter = new Parameter();
        parameter.setName("networkElementId");
        parameter.setValue(nodeName);
        parameter.setKind(ParameterKind.ARGUMENT);
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(parameter);
        return parameters;
    }

    private void assertSuiteDetails(
            Map<String, NodeNameTestSuiteInfo> suitesMap,
            String nodeName,
            String suiteUid,
            int testCasesCount) {
        NodeNameTestSuiteInfo actualSuite = suitesMap.get(nodeName + ":" + suiteUid);
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
