package com.ericsson.de.allure.service.application;

import com.ericsson.de.allure.service.application.storage.StorageService;
import com.ericsson.de.allure.service.presentation.controllers.exceptions.ServiceException;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;
import com.google.common.io.ByteStreams;
import com.google.common.net.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zeroturnaround.zip.ZipUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.Files.createFile;
import static java.nio.file.Files.deleteIfExists;
import static java.util.UUID.randomUUID;
import static org.apache.tika.io.IOUtils.closeQuietly;

@Service
public class ResourceUploadService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceUploadService.class);

    @Autowired
    private ReportGenerationService reportService;

    @Autowired
    private StorageService uploadStorage;

    @Autowired
    private FileTypeDetector fileTypeDetector;

    public void upload(String reportId, InputStream data) {
        BufferedOutputStream outputStream = null;
        InputStream dataBuffer = new BufferedInputStream(data);
        Path resourcePath = null;
        FileOutputStream fileOutputStream = null;
        try {
            validate(reportId, dataBuffer);

            Path resourceDirPath = uploadStorage.getOrCreateDirectory(reportId);
            String resourceName = randomUUID().toString();
            resourcePath = Paths.get(resourceDirPath.toString(), resourceName);
            createFile(resourcePath);
            LOGGER.info("created resource file at {}", resourcePath);
            fileOutputStream = new FileOutputStream(resourcePath.toFile());
            outputStream = new BufferedOutputStream(fileOutputStream);
            ByteStreams.copy(dataBuffer, outputStream);
            outputStream.flush();

            unpacking(resourcePath);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new ServiceException("Error to upload resource " + reportId);
        } finally {
            closeQuietly(fileOutputStream);
            closeQuietly(outputStream);
            closeQuietly(data);
            closeQuietly(dataBuffer);
            delete(resourcePath);
        }
    }

    @VisibleForTesting
    void validate(String reportId, InputStream inputStream) throws IOException {
        if (!fileTypeDetector.hasFileContentType(inputStream, MediaType.ZIP)) {
            throw new ServiceException("Invalid upload content type");
        }
        if (reportService.isUploadCompleted(reportId)) {
            throw new ServiceException("Unable to upload resource as report (" + reportId
                + ") has been already generated");
        }
    }

    private void unpacking(Path resourcePath) {
        LOGGER.info("archive entries in {}:", resourcePath);
        ZipUtil.iterate(resourcePath.toFile(), zipEntry -> LOGGER.info("- {}", zipEntry.getName()));
        ZipUtil.unpack(resourcePath.toFile(), resourcePath.getParent().toFile());
    }

    private static void delete(Path resourcePath) {
        try {
            if (resourcePath != null) {
                LOGGER.info("deleting archive {}", resourcePath);
                deleteIfExists(resourcePath);
            }
        } catch (IOException e) {
            Throwables.propagate(e);
        }
    }
}
