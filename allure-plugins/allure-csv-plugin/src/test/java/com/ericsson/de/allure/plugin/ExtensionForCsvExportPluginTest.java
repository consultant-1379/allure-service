package com.ericsson.de.allure.plugin;

import org.junit.Test;
import ru.yandex.qatools.allure.data.plugins.Plugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ExtensionForCsvExportPluginTest {

    private ExtensionForCsvExportPlugin plugin = new ExtensionForCsvExportPlugin();

    @Test
    public void canonical() {
        assertEquals("", plugin.canonical(""));
        assertEquals("currentsprint", plugin.canonical("Current Sprint"));
    }

    @Test
    public void priority() {
        int csvExportPluginPriority = CsvExportPlugin.class.getAnnotation(Plugin.Priority.class).value();
        int csvExportExtensionPriority = ExtensionForCsvExportPlugin.class.getAnnotation(Plugin.Priority.class).value();
        String errorMessage = "CSV Plugin should be run before Extension (CSV Plugin priority should be higher)";
        assertTrue(errorMessage, csvExportPluginPriority > csvExportExtensionPriority);
    }
}
