package com.ericsson.de.allure.plugin;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import ru.yandex.qatools.allure.data.AllureTestCase;
import ru.yandex.qatools.allure.data.AllureTestCaseInfo;
import ru.yandex.qatools.allure.data.AllureTestSuiteInfo;
import ru.yandex.qatools.allure.data.plugins.DefaultTabPlugin;
import ru.yandex.qatools.allure.data.plugins.Plugin;
import ru.yandex.qatools.allure.data.utils.PluginUtils;
import ru.yandex.qatools.allure.data.utils.TextUtils;
import ru.yandex.qatools.allure.model.Parameter;

@Plugin.Name("nodeType")
@Plugin.Priority(700)
public class NodetypesPlugin extends DefaultTabPlugin {

    @Plugin.Data
    Map<String, NodeTypeItem> suitesByNodeType = Maps.newHashMap();

    @Override
    public void process(AllureTestCase testCase) {
        HashSet<String> nodeTypes = Sets.newHashSet();

        List<Parameter> parameters = testCase.getParameters();
        if (!parameters.isEmpty()) {
            for (Parameter parameter : parameters) {
                extractNodeTypeFromParamaters(nodeTypes, parameter);
            }

            for (String nodeType : nodeTypes) {
                NodeTypeItem nodeTypeItem = createAndGetNodeTypeGroupItem(nodeType);
                NodeTypeTestSuiteInfo testSuiteInfo = createAndGetTestSuiteInfo(testCase, nodeTypeItem);
                testSuiteInfo.addTestCase((AllureTestCaseInfo) PluginUtils.toInfo(testCase));
                nodeTypeItem.updateStatistic(testCase.getStatus());
                nodeTypeItem.increaseTestCaseCount();
                testSuiteInfo.updateStatistic(testCase.getStatus());
                testSuiteInfo.incrementTotalCount();
            }
        }
    }

    private NodeTypeTestSuiteInfo createAndGetTestSuiteInfo(AllureTestCase testCase, NodeTypeItem nodeTypeItem){
        AllureTestSuiteInfo suite = testCase.getSuite();
        String suiteUid = nodeTypeItem.getNodeType() + ":" + suite.getUid();
        Map<String, NodeTypeTestSuiteInfo> suitesMap = nodeTypeItem.getSuitesMap();
        if (!nodeTypeItem.isSuiteExist(suiteUid)) {
            NodeTypeTestSuiteInfo testSuite = new NodeTypeTestSuiteInfo(suite);
            suitesMap.put(suiteUid, testSuite);
        }
        return suitesMap.get(suiteUid);
    }

    private void extractNodeTypeFromParamaters(HashSet<String> nodeTypesSet, Parameter parameter) {
        if ("nodeType".equals(parameter.getName())) {
            nodeTypesSet.add(parameter.getValue());
        }
    }

    private NodeTypeItem createAndGetNodeTypeGroupItem(String nodeType) {
        if (!suitesByNodeType.containsKey(nodeType)) {
            NodeTypeItem nodeTypeItem = new NodeTypeItem();
            nodeTypeItem.setNodeType(nodeType);
            nodeTypeItem.setUid(TextUtils.generateUid());
            suitesByNodeType.put(nodeType, nodeTypeItem);
        }
        return suitesByNodeType.get(nodeType);
    }
}
