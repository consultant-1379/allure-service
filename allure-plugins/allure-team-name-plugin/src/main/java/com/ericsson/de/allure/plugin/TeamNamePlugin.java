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

@Plugin.Name("teamName")
@Plugin.Priority(710)
public class TeamNamePlugin extends DefaultTabPlugin {

    @Plugin.Data
    Map<String, TeamNameItem> suitesByTeamName = Maps.newHashMap();

    @Override
    public void process(AllureTestCase testCase) {
        HashSet<String> teamNames = Sets.newHashSet();

        List<Parameter> parameters = testCase.getParameters();
        if (!parameters.isEmpty()) {
            for (Parameter parameter : parameters) {
                extractTeamNameFromParamaters(teamNames, parameter);
            }

            for (String teamName : teamNames) {
                TeamNameItem teamNameItem = createAndGetTeamNameGroupItem(teamName);
                TeamNameTestSuiteInfo testSuiteInfo = createAndGetTestSuiteInfo(testCase, teamNameItem);
                testSuiteInfo.addTestCase((AllureTestCaseInfo) PluginUtils.toInfo(testCase));
                teamNameItem.updateStatistic(testCase.getStatus());
                teamNameItem.increaseTestCaseCount();
                testSuiteInfo.updateStatistic(testCase.getStatus());
                testSuiteInfo.incrementTotalCount();
            }
        }
    }

    private TeamNameTestSuiteInfo createAndGetTestSuiteInfo(AllureTestCase testCase, TeamNameItem teamNameItem){
        AllureTestSuiteInfo suite = testCase.getSuite();
        String suiteUid = teamNameItem.getTeamName() + ":" + suite.getUid();
        Map<String, TeamNameTestSuiteInfo> suitesMap = teamNameItem.getSuitesMap();
        if (!teamNameItem.isSuiteExist(suiteUid)) {
            TeamNameTestSuiteInfo testSuite = new TeamNameTestSuiteInfo(suite);
            suitesMap.put(suiteUid, testSuite);
        }
        return suitesMap.get(suiteUid);
    }

    private void extractTeamNameFromParamaters(HashSet<String> teamNamesSet, Parameter parameter) {
        if ("teamName".equals(parameter.getName())) {
            teamNamesSet.add(parameter.getValue());
        }
    }

    private TeamNameItem createAndGetTeamNameGroupItem(String teamName) {
        if (!suitesByTeamName.containsKey(teamName)) {
            TeamNameItem teamNameItem = new TeamNameItem();
            teamNameItem.setTeamName(teamName);
            teamNameItem.setUid(TextUtils.generateUid());
            suitesByTeamName.put(teamName, teamNameItem);
        }
        return suitesByTeamName.get(teamName);
    }
}
