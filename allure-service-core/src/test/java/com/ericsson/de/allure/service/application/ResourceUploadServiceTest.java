package com.ericsson.de.allure.service.application;

import com.ericsson.de.allure.service.application.storage.StorageService;
import com.ericsson.de.allure.service.presentation.controllers.exceptions.ServiceException;
import com.google.common.io.Resources;
import com.google.common.net.MediaType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static java.nio.file.Files.createDirectory;
import static java.nio.file.Files.newInputStream;
import static java.util.Arrays.stream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ResourceUploadServiceTest {

    private static final String RESOURCE_ID = "dfa73a26-1e25-11e7-93ae-92361f002671";

    @InjectMocks
    private ResourceUploadService service;

    @Mock
    private FileTypeDetector detector;

    @Mock
    private StorageService storage;

    @Mock
    private ReportGenerationService reportService;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setUp() throws IOException {
        Path resourceDirPath = Paths.get(folder.getRoot().getAbsolutePath(), RESOURCE_ID);
        doReturn(resourceDirPath).when(storage).getOrCreateDirectory(eq(RESOURCE_ID));
        createDirectory(resourceDirPath);
    }

    @Test
    public void throw_service_exception_on_complete_upload()
        throws IOException, URISyntaxException {
        InputStream zipInputStream = mock(InputStream.class);
        doReturn(true).when(detector).hasFileContentType(any(InputStream.class), eq(MediaType.ZIP));
        doReturn(true).when(reportService).isUploadCompleted(eq(RESOURCE_ID));
        assertThatThrownBy(() -> service.validate(RESOURCE_ID, zipInputStream))
            .isInstanceOf(ServiceException.class)
            .hasMessage("Unable to upload resource as report (" + RESOURCE_ID + ") has been already generated");
    }

    @Test
    public void throw_service_exception_on_unsupported_resource()
        throws IOException, URISyntaxException {
        InputStream jsonInputStream = createInputStream("xunit_json");
        assertThatThrownBy(() -> service.upload(RESOURCE_ID, jsonInputStream))
            .isInstanceOf(ServiceException.class)
            .hasMessage("Invalid upload content type");
    }

    @Test
    public void throw_illegal_argument_exception_on_empty_resource()
        throws IOException, URISyntaxException {
        doThrow(IOException.class).when(storage).getOrCreateDirectory(eq(RESOURCE_ID));
        doReturn(true).when(detector).hasFileContentType(any(InputStream.class), eq(MediaType.ZIP));
        InputStream zipInputStream = createInputStream("allure_xml_results_zip");
        assertThatThrownBy(() -> service.upload(RESOURCE_ID, zipInputStream))
            .isInstanceOf(ServiceException.class)
            .hasMessage("Error to upload resource " + RESOURCE_ID);
    }

    @Test
    public void upload_resource_and_verify_its_existence() throws IOException, URISyntaxException {
        doReturn(true).when(detector).hasFileContentType(any(InputStream.class), eq(MediaType.ZIP));

        InputStream zipInputStream = createInputStream("allure_xml_results_zip");

        service.upload(RESOURCE_ID, zipInputStream);

        verify(detector, atLeastOnce()).hasFileContentType(any(InputStream.class), eq(MediaType.ZIP));
        verify(storage, atLeastOnce()).getOrCreateDirectory(eq(RESOURCE_ID));
        verify(reportService, atLeastOnce()).isUploadCompleted(anyString());

        File[] rootDirectory = folder.getRoot().listFiles();
        assertThat(rootDirectory)
            .as("List of root files/directories")
            .hasSize(1)
            .extracting(File::getName)
            .contains(RESOURCE_ID);

        Optional<File> resourceDirectory = stream(rootDirectory)
            .filter(File::isDirectory)
            .findFirst();
        assertThat(resourceDirectory)
            .isPresent();
        assertThat(resourceDirectory.get().getName())
            .isEqualTo(RESOURCE_ID);
        assertThat(resourceDirectory.get().listFiles())
            .hasSize(6)
            .extracting(File::getName)
            .contains(
                "te-console-logs",
                "e64255b3-2d21-4cf5-b25f-4582cffc5647-attachment.txt",
                "d808a9f0-82c1-4295-9828-fcde7fda5e8b-attachment.txt",
                "b29b579d-8b28-477d-9d84-0b2a233f8ca6-testsuite.xml",
                "7bb7fd79-5bd8-486b-a818-6972bbdf8872-testsuite.xml",
                "2c9e9f19-e2a5-45c8-946d-f89629ef9a71-attachment.txt"
                );
    }

    private static InputStream createInputStream(String fileName) throws URISyntaxException, IOException {
        return newInputStream(Paths.get(Resources.getResource(fileName).toURI()));
    }
}
