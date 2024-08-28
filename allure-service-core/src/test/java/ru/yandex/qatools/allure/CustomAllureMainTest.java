package ru.yandex.qatools.allure;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

public class CustomAllureMainTest {

    @Rule
    public TemporaryFolder folderRule = new TemporaryFolder();

    @Test
    public void report_resources_match_only_files() {
        String filePath = "lib/allure-bundle-1.4.23.HOTFIX1.jar!/allure.report.face/webjars/bar.css";
        assertThat(CustomAllureMain.REPORT_RESOURCE_PATTERN.matcher(filePath).find())
            .as("pattern should accept '%s' file", filePath)
            .isTrue();
    }

    @Test
    public void report_resources_not_match_only_directories() {
        String dirPath = "lib/allure-bundle-1.4.23.HOTFIX1.jar!/allure.report.face/webjars/angular-loading-bar/";
        assertThat(CustomAllureMain.REPORT_RESOURCE_PATTERN.matcher(dirPath).find())
            .as("pattern should not accept '%s' directory", dirPath)
            .isFalse();
    }

    @Test
    public void allure_face_resources_are_unpacked_from_classpath() throws IOException {
        File outputDir = folderRule.newFolder();
        CustomAllureMain.unpackFace(outputDir);

        List<String> facesPath = Files.list(Paths.get(outputDir.toURI()))
            .map(Path::getFileName)
            .map(Path::toString)
            .collect(toList());
        assertThat(facesPath)
            .containsOnly(
                "css", "flash", "img", "index.html", "js", "plugins", "templates",
                "translations", "vendor", "WEB-INF", "webjars");
    }

    @Test
    public void allure_custom_plugin_resources_are_unpacked_from_classpath() throws IOException {
        File outputDir = folderRule.newFolder();
        CustomAllureMain.unpackCustomPlugins(outputDir);

        List<String> pluginsPathList = Files.list(Paths.get(outputDir.toURI()))
            .flatMap(this::walk)
            .map(Path::getFileName)
            .map(Path::toString)
            .collect(toList());

        assertThat(pluginsPathList)
            .containsOnly(
                "plugins",
                "csvExport",
                "script-min.js",
                "script.js",
                "styles-min.css",
                "styles.css",
                "tab.tpl.html",
                "jenkins-logs",
                "detailed.html",
                "overview.html",
                "tab.tpl.html",
                "priorities",
                "en.json",
                "ptbr.json",
                "ru.json",
                "nodeType",
                "nodeName",
                "kpi",
                "teamName");
    }

    private Stream<Path> walk(Path dir) {
        try {
            return Files.walk(dir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
