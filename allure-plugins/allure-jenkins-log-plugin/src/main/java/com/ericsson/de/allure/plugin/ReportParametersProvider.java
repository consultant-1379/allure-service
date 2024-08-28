package com.ericsson.de.allure.plugin;

import java.io.File;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 * Date: 21.06.2017
 */
public class ReportParametersProvider {

    private static ThreadLocal<File[]> inputDirectories = new ThreadLocal<>();

    private static ThreadLocal<File> outputDirectory = new ThreadLocal<>();

    private ReportParametersProvider() {
    }

    static File[] getInputDirectories() {
        return inputDirectories.get();
    }

    static File getOutputDirectory() {
        return outputDirectory.get();
    }

    public static void setInputDirectories(File[] inputDirectories) {
        ReportParametersProvider.inputDirectories.set(inputDirectories);
    }

    public static void setInputDirectories(ThreadLocal<File[]> inputDirectories) {
        ReportParametersProvider.inputDirectories = inputDirectories;
    }

    public static void setOutputDirectory(File outputDirectory) {
        ReportParametersProvider.outputDirectory.set(outputDirectory);
    }
}
