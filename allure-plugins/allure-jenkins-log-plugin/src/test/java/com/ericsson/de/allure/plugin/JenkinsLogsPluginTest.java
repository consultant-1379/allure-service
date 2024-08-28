package com.ericsson.de.allure.plugin;

import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ru.yandex.qatools.allure.data.plugins.PluginData;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.AnyOf.anyOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JenkinsLogsPluginTest {

    private JenkinsLogsPlugin jenkinsLogsPlugin;
    private File tempDir;
    private File emptyTempDir;
    private File file;

    private ConfigProvider configProvider;

    @Before
    public void setUp() throws IOException {
        configProvider = mock(ConfigProvider.class);
        jenkinsLogsPlugin = new JenkinsLogsPlugin();
        jenkinsLogsPlugin.configProvider = configProvider;

        tempDir = Files.createTempDir();

        emptyTempDir = Files.createTempDir();

        file = new File(tempDir.getAbsolutePath() + "/executor1.log");
        FileUtils.write(file, "logs");

        File file2 = new File(tempDir.getAbsolutePath() + "/executor2.log");
        FileUtils.write(file2, "another log");
    }

    @Test
    public void addTrailingSlash() {
        assertThat(jenkinsLogsPlugin.addTrailingSlash("folder/")).isEqualTo("folder/");
        assertThat(jenkinsLogsPlugin.addTrailingSlash("folder")).isEqualTo("folder/");
    }

    @Test
    public void shouldCreateCorrectInventoryObject() {
        when(configProvider.getProperty(JenkinsLogsPlugin.JENKINS_LOGS_SCAN_PATH)).thenReturn(tempDir.getAbsolutePath());

        List<PluginData> pluginData = jenkinsLogsPlugin.getPluginData();

        assertThat(pluginData.size(), is(1));

        PluginData jenkinsLogsPluginData = pluginData.get(0);

        assertThat(jenkinsLogsPluginData.getName(), is(PluginName.JENKINS_LOGS + ".json"));

        JenkinsLogsInventory data = (JenkinsLogsInventory) jenkinsLogsPluginData.getData();

        assertThat(data.getLogFiles().size(), is(2));

        assertThat(data.getLogFiles().get(0).getName(), anyOf(is("executor1.log"), is("executor2.log")));
        assertThat(data.getLogFiles().get(0).getSize(), anyOf(is(4L), is(11L)));

        assertThat(data.getLogFiles().get(1).getName(), anyOf(is("executor1.log"), is("executor2.log")));
        assertThat(data.getLogFiles().get(1).getSize(), anyOf(is(4L), is(11L)));

        assertThat(data.getErrorMessage(), nullValue());
        assertThat(data.getLogSourcePath(), is(JenkinsLogsPlugin.JENKINS_LOGS_SOURCE_PATH_DEFAULT));
    }

    @Test
    public void shouldSetErrorInInventoryIfScanPathMissing() {
        when(configProvider.getProperty(JenkinsLogsPlugin.JENKINS_LOGS_SCAN_PATH)).thenReturn(null);

        List<PluginData> pluginData = jenkinsLogsPlugin.getPluginData();

        assertThat(pluginData.size(), is(1));

        PluginData jenkinsLogsPluginData = pluginData.get(0);

        assertThat(jenkinsLogsPluginData.getName(), is(PluginName.JENKINS_LOGS + ".json"));

        JenkinsLogsInventory data = (JenkinsLogsInventory) jenkinsLogsPluginData.getData();

        String msg = String.format(JenkinsLogsPlugin.PROPERTY_NOT_DIRECTORY_ERROR_MSG, JenkinsLogsPlugin.JENKINS_LOGS_SOURCE_PATH_DEFAULT);
        assertThat(data.getLogFiles().size(), is(0));
        assertThat(data.getErrorMessage(), containsString(msg));
        assertThat(data.getLogSourcePath(), nullValue());
    }

    @Test
    public void shouldSetErrorInInventoryIfScanPathNotDirectory() {

        when(configProvider.getProperty(JenkinsLogsPlugin.JENKINS_LOGS_SCAN_PATH)).thenReturn(file.getAbsolutePath());

        List<PluginData> pluginData = jenkinsLogsPlugin.getPluginData();

        assertThat(pluginData.size(), is(1));

        PluginData jenkinsLogsPluginData = pluginData.get(0);

        assertThat(jenkinsLogsPluginData.getName(), is(PluginName.JENKINS_LOGS + ".json"));

        JenkinsLogsInventory data = (JenkinsLogsInventory) jenkinsLogsPluginData.getData();

        assertThat(data.getLogFiles().size(), is(0));
        assertThat(data.getErrorMessage(), containsString("is not directory"));
        assertThat(data.getLogSourcePath(), nullValue());
    }

    @Test
    public void shouldSetErrorInInventoryIfScanPathDirectoryEmpty() {

        when(configProvider.getProperty(JenkinsLogsPlugin.JENKINS_LOGS_SCAN_PATH)).thenReturn(emptyTempDir.getAbsolutePath());

        List<PluginData> pluginData = jenkinsLogsPlugin.getPluginData();

        assertThat(pluginData.size(), is(1));

        PluginData jenkinsLogsPluginData = pluginData.get(0);

        assertThat(jenkinsLogsPluginData.getName(), is(PluginName.JENKINS_LOGS + ".json"));

        JenkinsLogsInventory data = (JenkinsLogsInventory) jenkinsLogsPluginData.getData();

        assertThat(data.getLogFiles().size(), is(0));
        assertThat(data.getErrorMessage(), containsString("has no files inside"));
        assertThat(data.getLogSourcePath(), nullValue());
    }


    @After
    public void tearDown() throws IOException {
        FileUtils.deleteDirectory(tempDir);
        FileUtils.deleteDirectory(emptyTempDir);
    }
}
