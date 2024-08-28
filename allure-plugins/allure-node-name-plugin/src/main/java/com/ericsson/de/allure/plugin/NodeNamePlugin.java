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

import java.util.List;
import java.util.Map;
import java.util.Set;

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

/**
 * Plugin for the nodeName.
 */
@Plugin.Name("nodeName")
@Plugin.Priority(700)
public class NodeNamePlugin extends DefaultTabPlugin {
    @Plugin.Data
    public final Map<String, NodeNameItem> suitesByNodeName = Maps.newHashMap();

    @Override
    public void process(final AllureTestCase testCase) {
        final Set<String> nodeNames = Sets.newHashSet();

        final List<Parameter> parameters = testCase.getParameters();
        if (!parameters.isEmpty()) {
            for (final Parameter parameter : parameters) {
                extractNodeNameFromParamaters(nodeNames, parameter);
            }

            for (final String nodeName : nodeNames) {
                final NodeNameItem nodeNameItem = createAndGetNodeNameGroupItem(nodeName);
                final NodeNameTestSuiteInfo testSuiteInfo = createAndGetTestSuiteInfo(testCase, nodeNameItem);
                testSuiteInfo.addTestCase((AllureTestCaseInfo) PluginUtils.toInfo(testCase));
                nodeNameItem.updateStatistic(testCase.getStatus());
                nodeNameItem.increaseTestCaseCount();
                testSuiteInfo.updateStatistic(testCase.getStatus());
                testSuiteInfo.incrementTotalCount();
            }
        }
    }

    private NodeNameTestSuiteInfo createAndGetTestSuiteInfo(final AllureTestCase testCase, final NodeNameItem nodeNameItem) {
        final AllureTestSuiteInfo suite = testCase.getSuite();
        final String suiteUid = nodeNameItem.getNodeName() + ":" + suite.getUid();
        final Map<String, NodeNameTestSuiteInfo> suitesMap = nodeNameItem.getSuitesMap();
        if (!nodeNameItem.isSuiteExist(suiteUid)) {
            final NodeNameTestSuiteInfo testSuite = new NodeNameTestSuiteInfo(suite);
            suitesMap.put(suiteUid, testSuite);
        }
        return suitesMap.get(suiteUid);
    }

    private void extractNodeNameFromParamaters(final Set<String> nodeNamesSet, final Parameter parameter) {
        if ("networkElementId".equalsIgnoreCase(parameter.getName())) {
            nodeNamesSet.add(parameter.getValue());
        }
    }

    private NodeNameItem createAndGetNodeNameGroupItem(final String nodeName) {
        if (!suitesByNodeName.containsKey(nodeName)) {
            final NodeNameItem nodeNameItem = new NodeNameItem();
            nodeNameItem.setNodeName(nodeName);
            nodeNameItem.setUid(TextUtils.generateUid());
            suitesByNodeName.put(nodeName, nodeNameItem);
        }
        return suitesByNodeName.get(nodeName);
    }
}
