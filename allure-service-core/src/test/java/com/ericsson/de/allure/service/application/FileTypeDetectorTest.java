package com.ericsson.de.allure.service.application;

import com.google.common.io.Resources;
import com.google.common.net.MediaType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import static java.nio.file.Files.newInputStream;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class FileTypeDetectorTest {

    @InjectMocks
    private FileTypeDetector detector;

    @Test
    public void detectMatchedZipContentType() throws URISyntaxException, IOException {
        InputStream inputStream = newInputStream(Paths.get(Resources.getResource("allure_xml_results_zip").toURI()));
        assertThat(detector.hasFileContentType(inputStream, MediaType.ZIP)).isTrue();
    }

    @Test
    public void detectUnmatchedContentType() throws IOException, URISyntaxException {
        InputStream inputStream = newInputStream(Paths.get(Resources.getResource("xunit_json").toURI()));
        assertThat(detector.hasFileContentType(inputStream, MediaType.ZIP)).isFalse();
    }
}
