package com.ericsson.de.allure.plugin;

import com.google.common.annotations.VisibleForTesting;
import ru.yandex.qatools.allure.data.plugins.Plugin;
import ru.yandex.qatools.allure.data.plugins.PreparePlugin;
import ru.yandex.qatools.commons.model.Environment;
import ru.yandex.qatools.commons.model.Parameter;

import java.util.Collections;
import java.util.List;

/**
 * This plugin sorts CSV Export Plugin data according to current sprint defined in environment properties.
 *
 */
@Plugin.Priority(490)
public class ExtensionForCsvExportPlugin implements PreparePlugin<Environment>  {

    private static final String CURRENT_SPRINT_ENVIRONMENT_PROPERTY = canonical("Current Sprint");

    @Override
    public void prepare(Environment environment) {
        List<Parameter> environmentParameters = environment.getParameter();
        for (Parameter environmentParameter : environmentParameters) {
            if (canonical(environmentParameter.getKey()).equalsIgnoreCase(CURRENT_SPRINT_ENVIRONMENT_PROPERTY)) {
                groupByCurrentSprint(environmentParameter.getValue());
                return;
            }
        }
    }

    private void groupByCurrentSprint(String currentSprint) { // NOSONAR

        // excluding header from being sorted
        List<CsvRow> rows = CsvExportPlugin.getSharedRows();
        CsvRow header = rows.remove(0);

        // grouping all test cases containing current sprint to the top
        Collections.sort(rows, new CurrentSprintComparator(currentSprint));

        // adding header back
        rows.add(0, header);
    }

    @VisibleForTesting
    protected static String canonical(String value) {
        return value.replaceAll(" ", "").toLowerCase();
    }

    @Override
    public Class<Environment> getType() {
        return Environment.class;
    }
}
