package com.ericsson.de.allure.plugin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import static com.ericsson.de.allure.plugin.common.TafLabelName.COMMENT;
import static com.ericsson.de.allure.plugin.common.TafLabelName.EXECUTION_TYPE;
import static com.ericsson.de.allure.plugin.common.TafLabelName.GROUP;

import static ru.yandex.qatools.allure.model.LabelName.FEATURE;
import static ru.yandex.qatools.allure.model.LabelName.ISSUE;
import static ru.yandex.qatools.allure.model.LabelName.STORY;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import ru.yandex.qatools.allure.data.AllureTestCase;
import ru.yandex.qatools.allure.data.AllureTestSuiteInfo;
import ru.yandex.qatools.allure.model.Label;
import ru.yandex.qatools.allure.model.Parameter;
import ru.yandex.qatools.allure.model.SeverityLevel;
import ru.yandex.qatools.allure.model.Status;
import ru.yandex.qatools.allure.model.ParameterKind;


@RunWith(MockitoJUnitRunner.class)
public class CsvExportPluginTest {

    CsvExportPlugin plugin;

    @Mock
    AllureTestCase allureTestCase;
    List<Label> labels = new ArrayList<>();

    @Mock
    AllureTestSuiteInfo allureTestSuiteInfo;

    @Before
    public void setUp() {
        plugin = new CsvExportPlugin();
        List<Parameter> parameters = getParameter("ERBS","LTE17ERBS00042","TAF");

        when(allureTestCase.getStatus()).thenReturn(Status.PASSED);
        when(allureTestCase.getTitle()).thenReturn("Title");
        when(allureTestCase.getSeverity()).thenReturn(SeverityLevel.NORMAL);
        when(allureTestCase.getSuite()).thenReturn(allureTestSuiteInfo);
        when(allureTestCase.getParameters()).thenReturn(parameters);
        when(allureTestCase.getSuite()).thenReturn(allureTestSuiteInfo);
        doReturn("Test Suite").when(allureTestSuiteInfo).getName();

        when(allureTestCase.getLabels()).thenReturn(labels);

        labels.add(new Label().withName(FEATURE.value()).withValue("Feature1"));
        labels.add(new Label().withName(FEATURE.value()).withValue("Feature2"));
        labels.add(new Label().withName(STORY.value()).withValue("Story1"));
        labels.add(new Label().withName(STORY.value()).withValue("Story2"));
        labels.add(new Label().withName(ISSUE.value()).withValue("Issue1"));
        labels.add(new Label().withName(ISSUE.value()).withValue("Issue2"));

        labels.add(new Label().withName(EXECUTION_TYPE.value()).withValue("manual"));
        labels.add(new Label().withName(COMMENT.value()).withValue("Comment"));
        labels.add(new Label().withName(GROUP.value()).withValue("Group1"));
        labels.add(new Label().withName(GROUP.value()).withValue("Group2"));
        labels.add(new Label().withName(CsvExportPlugin.SPRINT_LABEL).withValue("Sprint1"));
        labels.add(new Label().withName(CsvExportPlugin.SPRINT_LABEL).withValue("Sprint2"));
    }

    @Test
    public void testPluginDataContainsHeaderRow() {
        CsvRow headerRow = plugin.rows.get(0);
        assertThat(headerRow.getTitle()).isEqualTo("Title");
        assertThat(headerRow.getComment()).isEqualTo("Comment");
        assertThat(headerRow.getComponent()).isEqualTo("Component");
        assertThat(headerRow.getNodeType()).isEqualTo("Node Type");
        assertThat(headerRow.getNodeName()).isEqualTo("Node Name");
        assertThat(headerRow.getTeamName()).isEqualTo("Team Name");
        assertThat(headerRow.getDefect()).isEqualTo("Defect");
        assertThat(headerRow.getPriority()).isEqualTo("Priority");
        assertThat(headerRow.getExecutionType()).isEqualTo("Execution type");
        assertThat(headerRow.getGroup()).isEqualTo("Group");
        assertThat(headerRow.getRequirement()).isEqualTo("Requirement");
        assertThat(headerRow.getResult()).isEqualTo("Result");
        assertThat(headerRow.getSprint()).isEqualTo("Sprint");
        assertThat(headerRow.getSuite()).isEqualTo("Suite");
    }

    @Test
    public void testPluginDataContainsCorrectlyFormedRow() {
        plugin.process(allureTestCase);

        assertThat(plugin.rows).hasSize(2);
        CsvRow dataRow = plugin.rows.get(1);

        checkDataRow(dataRow);
    }

    private void checkDataRow(CsvRow dataRow) {
        assertThat(dataRow.getTitle()).isEqualTo("Title");
        assertThat(dataRow.getComment()).isEqualTo("Comment");
        assertThat(dataRow.getComponent()).isEqualTo("Feature1;Feature2");
        assertThat(dataRow.getNodeType()).isEqualTo("ERBS");
        assertThat(dataRow.getNodeName()).isEqualTo("LTE17ERBS00042");
        assertThat(dataRow.getTeamName()).isEqualTo("TAF");
        assertThat(dataRow.getDefect()).isEqualTo("Issue1;Issue2");
        assertThat(dataRow.getPriority()).isEqualTo("normal");
        assertThat(dataRow.getExecutionType()).isEqualTo("manual");
        assertThat(dataRow.getGroup()).isEqualTo("Group1;Group2");
        assertThat(dataRow.getRequirement()).isEqualTo("Story1;Story2");
        assertThat(dataRow.getResult()).isEqualTo("passed");
        assertThat(dataRow.getSprint()).isEqualTo("Sprint1;Sprint2");
        assertThat(dataRow.getSuite()).isEqualTo("Test Suite");
    }

    @Test
    public void testPluginDataContainsAccumulatesRows() {
        plugin.process(allureTestCase);
        plugin.process(allureTestCase);
        plugin.process(allureTestCase);

        assertThat(plugin.rows).hasSize(4);
    }

    private List<Parameter> getParameter(String nodeType,String nodeName, String teamName) {
        Parameter parameterOld = new Parameter();
        Parameter parameterNew = new Parameter();
        Parameter paramTeam = new Parameter();
        parameterOld.setName("nodeType");
        parameterOld.setValue(nodeType);
        parameterNew.setName("networkElementId");
        parameterNew.setValue(nodeName);
        paramTeam.setName("teamName");
        paramTeam.setValue(teamName);
        parameterOld.setKind(ParameterKind.ARGUMENT);
        parameterNew.setKind(ParameterKind.ARGUMENT);
        paramTeam.setKind(ParameterKind.ARGUMENT);
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(parameterOld);
        parameters.add(parameterNew);
        parameters.add(paramTeam);
        return parameters;
    }
}
