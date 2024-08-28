package com.ericsson.de.allure.plugin;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.qatools.allure.data.AllureTestCase;
import ru.yandex.qatools.allure.data.plugins.DefaultTabPlugin;
import ru.yandex.qatools.allure.data.plugins.Plugin;
import ru.yandex.qatools.allure.data.plugins.PluginData;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static com.ericsson.de.allure.plugin.ReportParametersProvider.getInputDirectories;
import static com.ericsson.de.allure.plugin.ReportParametersProvider.getOutputDirectory;
import static com.google.common.base.MoreObjects.firstNonNull;
import static java.nio.file.Paths.get;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Plugin.Name(PluginName.JENKINS_LOGS)
@Plugin.Priority(100)
public class JenkinsLogsPlugin extends DefaultTabPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(JenkinsLogsPlugin.class);

    /**
     * Path which will be scanned during plugin execution time. All files located in this directory
     * will be added to plugin inventory file and appear as a list of log files on client side.
     */
    public static final String JENKINS_LOGS_SCAN_PATH = "TE_JENKINS_LOGS_SCAN_PATH";

    /**
     * Directory which will be queried to access log file contents from client side, should be relative to allure report root
     */
    public static final String JENKINS_LOGS_SOURCE_PATH = "TE_JENKINS_LOGS_SOURCE_PATH";
    public static final String JENKINS_LOGS_SOURCE_PATH_DEFAULT = "te-console-logs/";

    private static final String PROPERTY_NOT_SET_ERROR_MSG = "%s environment property is not set. Logs are not available.";
    public static final String PROPERTY_NOT_DIRECTORY_ERROR_MSG = "Scan path %s is not directory. Logs are not available.";
    private static final String DIRECTORY_EMPTY_ERROR_MSG = "Scan path %s has no files inside. No logs available.";

    ConfigProvider configProvider = new ConfigProvider();

    @Override
    public List<PluginData> getPluginData() {

        // supporting overriding logs folder
        String logsRelativePath = firstNonNull(configProvider.getProperty(JENKINS_LOGS_SCAN_PATH), JENKINS_LOGS_SOURCE_PATH_DEFAULT);
        if (logsRelativePath == null) {
            return createPluginDataWithErrorMessage(String.format(PROPERTY_NOT_SET_ERROR_MSG, JENKINS_LOGS_SCAN_PATH));
        }

        String logsPath = getLogsPath(logsRelativePath);

        File logsFolder = new File(logsPath);
        if (!logsFolder.isDirectory()) {
            return createPluginDataWithErrorMessage(String.format(PROPERTY_NOT_DIRECTORY_ERROR_MSG, logsPath));
        }

        File[] logs = logsFolder.listFiles();
        if (logs.length == 0) {
            return createPluginDataWithErrorMessage(String.format(DIRECTORY_EMPTY_ERROR_MSG, logsPath));
        }

        String clientLogsPath = configProvider.getProperty(JENKINS_LOGS_SOURCE_PATH);
        if (clientLogsPath != null) {
            clientLogsPath = addTrailingSlash(clientLogsPath);
        } else {
            clientLogsPath = JENKINS_LOGS_SOURCE_PATH_DEFAULT;
        }
        LOGGER.info("{} will be set to {}", JENKINS_LOGS_SOURCE_PATH, clientLogsPath);

        JenkinsLogsInventory jenkinsLogsInventory = new JenkinsLogsInventory();
        jenkinsLogsInventory.setLogSourcePath(clientLogsPath);
        jenkinsLogsInventory.addLogFiles(logs);
        copyLogsToReportGenerationFolder(logs, clientLogsPath);

        return createPluginDataWithInventory(jenkinsLogsInventory);
    }

    private String getLogsPath(String scanRelativePath) {

        // default result
        String logsPath = scanRelativePath;

        // supporting multiple input folders (XML reports)
        File[] resultDirectories = getInputDirectories();
        if (resultDirectories != null) {
            for (File resultDirectory : resultDirectories) {
                String resultPath = addTrailingSlash(resultDirectory.getAbsolutePath());
                File logsCandidate = new File(resultPath + scanRelativePath);
                if (logsCandidate.isDirectory()) {
                    logsPath = logsCandidate.getAbsolutePath();
                }
            }
        }
        return logsPath;
    }

    private void copyLogsToReportGenerationFolder(File[] logs, String clientLogsPath) {

        // copying Jenkins logs folder to Report target folder
        File outputDirectory = getOutputDirectory();
        // parameter will not be available if executed via Allure CLI
        if (outputDirectory != null) {
            Path targetFolder = get(outputDirectory.toURI()).resolve(clientLogsPath);
            targetFolder.toFile().mkdirs();
            for (File log : logs) {
                Path source = get(log.toURI());
                try {
                    Files.copy(source, targetFolder.resolve(source.getFileName()), REPLACE_EXISTING);
                } catch (IOException e) {
                    Throwables.propagate(e);
                }
            }
        }
    }

    @VisibleForTesting
    protected String addTrailingSlash(String folderPath) {
        String tempFolderPath = folderPath;
        if (!folderPath.endsWith("/")) {
            tempFolderPath = folderPath + "/";
        }
        return tempFolderPath;
    }

    private List<PluginData> createPluginDataWithErrorMessage(String message) {
        LOGGER.error(message);

        JenkinsLogsInventory empty = JenkinsLogsInventory.withErrorMessage(message);
        return createPluginDataWithInventory(empty);
    }

    private List<PluginData> createPluginDataWithInventory(JenkinsLogsInventory inventory) {
        return Lists.newArrayList(new PluginData(PluginName.JENKINS_LOGS + ".json", inventory));
    }

    @Override
    public void process(AllureTestCase allureTestCase) {
        //ignore
    }

}
