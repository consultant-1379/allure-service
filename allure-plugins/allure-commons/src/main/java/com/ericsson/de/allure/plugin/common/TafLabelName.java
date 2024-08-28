package com.ericsson.de.allure.plugin.common;

public enum TafLabelName {
    COMMENT("comment"),
    GROUP("group"),
    EXECUTION_TYPE("execution_type");

    private final String value;

    TafLabelName(String name) {
        value = name;
    }

    public String value() {
        return value;
    }

}
