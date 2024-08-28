package com.ericsson.de.allure.plugin;

import static com.ericsson.de.allure.plugin.common.TafLabelName.COMMENT;
import static com.ericsson.de.allure.plugin.common.TafLabelName.EXECUTION_TYPE;
import static com.ericsson.de.allure.plugin.common.TafLabelName.GROUP;


import static ru.yandex.qatools.allure.model.LabelName.FEATURE;
import static ru.yandex.qatools.allure.model.LabelName.ISSUE;
import static ru.yandex.qatools.allure.model.LabelName.STORY;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import ru.yandex.qatools.allure.data.AllureTestCase;
import ru.yandex.qatools.allure.data.plugins.DefaultTabPlugin;
import ru.yandex.qatools.allure.data.plugins.Plugin;
import ru.yandex.qatools.allure.model.Label;
import ru.yandex.qatools.allure.model.Parameter;

/**
 * Plugin prepares content to be exported in CSV.
 * Content is dumped to json file and export to CSV happens on client side with ngCSV directive help
 *
 */
@Plugin.Name("csvExport")
@Plugin.Priority(499)
public class CsvExportPlugin extends DefaultTabPlugin {

    static final String SPRINT_LABEL = "sprint";
    static final String NA = "-";
    static LinkedList<String> dataRaw = Lists.newLinkedList();

    private static List<CsvRow> sharedRows;
    private static final Logger LOGGER = LoggerFactory.getLogger(CsvExportPlugin.class);

    @Plugin.Data
    List<CsvRow> rows;

    public CsvExportPlugin() {
        rows = Lists.newArrayList();
        rows.add(CsvRow.HEADER);
        sharedRows = rows;
        dataRaw.add("nodeType");
        dataRaw.add("networkElementId");
        dataRaw.add("teamName");
    }

    @Override
    public void process(AllureTestCase data) {
        LinkedList<String> typeInfo = Lists.newLinkedList();
        CsvRow row = new CsvRow();
        row.setTitle(data.getTitle());
        row.setResult(data.getStatus().value());
        row.setPriority(data.getSeverity().value());
        row.setSuite(data.getSuite().getName());
        row.setTime(data.getTime());

        List<Parameter> parameters = data.getParameters();
        if (!parameters.isEmpty()) {
            for (String dataCheck : dataRaw){
                extractInfoFromParamaters(typeInfo, parameters, dataCheck);
            }
        }
        try {
            row.setNodeType(typeInfo.get(0));
            row.setNodeName(typeInfo.get(1));
            row.setTeamName(typeInfo.get(2));
        } catch(IndexOutOfBoundsException e) {
              LOGGER.info("Parameter List appears to be empty!!");
        }
        Map<String, List<String>> labels = Maps.newHashMap();
        for (Label label : data.getLabels()) {
            collectValueTo(labels, label);
        }

        row.setComment(labels.get(COMMENT.value()));
        row.setExecutionType(labels.get(EXECUTION_TYPE.value()));
        row.setGroup(labels.get(GROUP.value()));

        row.setComponent(labels.get(FEATURE.value()));
        row.setRequirement(labels.get(STORY.value()));
        row.setDefect(labels.get(ISSUE.value()));
        row.setSprint(labels.get(SPRINT_LABEL));

        rows.add(row);
    }

    private void collectValueTo(Map<String, List<String>> labels, Label label) {
        List<String> values = labels.get(label.getName());
        if (values != null) {
            values.add(label.getValue());
        } else {
            values = Lists.newArrayList();
            values.add(label.getValue());
            labels.put(label.getName(), values);
        }
    }

    public static List<CsvRow> getSharedRows() {
        return sharedRows;
    }

    private void extractInfoFromParamaters(LinkedList<String> typesList, List<Parameter> parameter, String dataOne) {
        Parameter param = parameter.stream()
                .filter(parameterSingle -> dataOne.equals(parameterSingle.getName()))
                .findAny()
                .orElse(null);
        if(param != null) {
            typesList.add(param.getValue());
        } else {
            typesList.add(NA);
        }
    }

}
