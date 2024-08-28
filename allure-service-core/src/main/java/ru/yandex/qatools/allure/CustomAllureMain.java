package ru.yandex.qatools.allure;

import com.google.common.io.Files;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import ru.yandex.qatools.allure.data.AllureReportGenerator;
import ru.yandex.qatools.allure.data.plugins.Plugin;
import ru.yandex.qatools.allure.data.plugins.WithResources;
import ru.yandex.qatools.allure.data.utils.ServiceLoaderUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.ericsson.de.allure.plugin.ReportParametersProvider.setInputDirectories;
import static com.ericsson.de.allure.plugin.ReportParametersProvider.setOutputDirectory;
import static java.util.stream.Collectors.toList;
import static ru.yandex.qatools.allure.data.DummyReportGenerator.getFiles;
import static ru.yandex.qatools.allure.data.io.ReportWriter.PLUGINS_DIRECTORY_NAME;

/**
 * This class is a workaround to fix "allure.report.face" static files and custom allure plugin extensions lookup
 * within classpath.
 *
 * The {@link ru.yandex.qatools.allure.AllureMain#REPORT_RESOURCE_PATTERN} pattern does not match allure
 * "allure.report.face" static files lookup within executable jar file classpath.
 *
 * @see ru.yandex.qatools.allure.AllureMain
 */
public final class CustomAllureMain {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomAllureMain.class);

    private static final String STATIC_FILES_LOCATION_PATTERN = "classpath*:allure.report.face/**/*.*";
    private static final String CUSTOM_ALLURE_PLUGIN_PKG = "ericsson";
    static final Pattern REPORT_RESOURCE_PATTERN = Pattern.compile("allure\\.report\\.face/(.+)(?<!/)$");

    /**
     * There is no need to instance this class.
     */
    private CustomAllureMain() {
    }

    public static void run(String[] args) throws IOException {

        if (args.length < 2) { // NOSONAR
            LOGGER.error("There must be at least two arguments");
            return;
        }
        int lastIndex = args.length - 1;
        File[] inputDirectories = getFiles(Arrays.copyOf(args, lastIndex));
        setInputDirectories(inputDirectories);

        File outputDirectory = new File(args[lastIndex]);
        setOutputDirectory(outputDirectory);

        AllureReportGenerator reportGenerator = new AllureReportGenerator(inputDirectories);
        reportGenerator.generate(outputDirectory);

        unpackFace(outputDirectory);
        unpackCustomPlugins(outputDirectory);
    }

    /**
     * Unpack report face to given directory.
     *`
     * @param outputDirectory the output directory to unpack face.
     * @throws IOException if any occurs.
     */
    static void unpackFace(File outputDirectory) throws IOException {
        Resource[] resources = new PathMatchingResourcePatternResolver().getResources(STATIC_FILES_LOCATION_PATTERN);
        for (Resource res : resources) {
            Matcher matcher = REPORT_RESOURCE_PATTERN.matcher(res.getURL().toString());
            if (matcher.find()) {
                String resourcePath = matcher.group(1);
                copyResource(outputDirectory, resourcePath, res);
            }
        }
    }

    static void unpackCustomPlugins(File outputDirectory) {
        findPluginsByType(Plugin.class).stream()
            .filter(plugin -> plugin.getClass().getPackage().getName().contains(CUSTOM_ALLURE_PLUGIN_PKG))
            .filter(WithResources.class::isInstance)
            .map(WithResources.class::cast)
            .forEach(resource -> copyPluginResources(resource, outputDirectory));
    }

    private static <T> List<T> findPluginsByType(Class<T> type) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return ServiceLoaderUtils.load(classLoader, type);
    }

    private static void copyPluginResources(WithResources plugin, File outputDirectory) {
        String resourcePath = plugin.getClass().getCanonicalName().replace('.', '/');
        try {
            LOGGER.debug("try to find resources at {}", resourcePath);
            List<Resource> resources = Stream.of(new PathMatchingResourcePatternResolver()
                .getResources("classpath*:" + resourcePath + "**/*.*"))
                .collect(toList());

            String destPath = Paths.get(PLUGINS_DIRECTORY_NAME, plugin.getName()).toString();
            File pluginDir = createParentDirs(outputDirectory, destPath);

            for (Resource res : resources) {
                LOGGER.debug("found custom plugin: {}", res.getURL());
                copyResource(pluginDir, res.getFilename(), res);
            }
        } catch (IOException io) {
            throw new RuntimeException(io);
        }
    }

    private static void copyResource(File srcDir, String destPath, Resource data) throws IOException {
        File dest = createParentDirs(srcDir, destPath);
        try (FileOutputStream output = new FileOutputStream(dest)) {
            IOUtils.copy(data.getInputStream(), output);
            LOGGER.debug("successfully copied at {}", dest);
        }
    }

    private static File createParentDirs(File parent, String childName) throws IOException {
        File newFile = new File(parent, childName);
        Files.createParentDirs(newFile);
        return newFile;
    }
}
