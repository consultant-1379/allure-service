package com.ericsson.de.allure.plugin;

import com.google.common.collect.Lists;

import java.io.File;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("unused")
public class JenkinsLogsInventory {

    private List<LogFile> logFiles = Lists.newArrayList();

    private String logSourcePath;

    private String errorMessage;

    public JenkinsLogsInventory() {
        // empty constructor
    }

    public JenkinsLogsInventory(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void addLogFiles(File[] files) {
        for (File logFile : files) {
            if (logFile.isFile()) {
                LogFile file = new LogFile(logFile.getName(), logFile.length());
                logFiles.add(file);
            }
        }
    }

    public List<LogFile> getLogFiles() {
        return Collections.unmodifiableList(logFiles);
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getLogSourcePath() {
        return logSourcePath;
    }

    public void setLogSourcePath(String logSourcePath) {
        this.logSourcePath = logSourcePath;
    }

    public static JenkinsLogsInventory withErrorMessage(String errorMessage) {
        return new JenkinsLogsInventory(errorMessage);
    }

    public static class LogFile {
        private String name;
        private long size;

        public LogFile(String name, long size) {
            this.name = name;
            this.size = size;
        }

        public String getName() {
            return name;
        }

        public long getSize() {
            return size;
        }
    }
}
