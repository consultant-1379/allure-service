package com.ericsson.de.allure.plugin.common;

import ru.yandex.qatools.allure.model.Label;

/**
 * Helper class for easy label creation
 *
 */
public final class TafAllureModelUtils {

    private TafAllureModelUtils() {
        // hiding constructor
    }

    public static Label createManualExecutionTypeLabel() {
        return createLabel(TafLabelName.EXECUTION_TYPE, "manual");
    }

    public static Label createAutomatedExecutionTypeLabel() {
        return createLabel(TafLabelName.EXECUTION_TYPE, "automated");
    }

    public static Label createCommentLabel(String comment) {
        String tempComment = comment;
        if (comment == null) {
            tempComment = "";
        }
        return createLabel(TafLabelName.COMMENT, tempComment);
    }

    public static Label createGroupLabel(String group) {
        return createLabel(TafLabelName.GROUP, group);
    }

    public static Label createLabel(TafLabelName name, String value) {
        return new Label().withName(name.value()).withValue(value);
    }


}
