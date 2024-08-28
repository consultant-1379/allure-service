package com.ericsson.de.allure.plugin;

public class ConfigProvider {

    public String getProperty(String property) {
        return System.getenv(property);
    }
}
