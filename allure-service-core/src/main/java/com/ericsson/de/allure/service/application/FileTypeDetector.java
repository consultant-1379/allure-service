package com.ericsson.de.allure.service.application;

import com.google.common.net.MediaType;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Objects;

@Component
public class FileTypeDetector {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileTypeDetector.class);

    /**
     * A file type detector for probing a file to guess its file type.
     *
     * The initial bytes in a file examines to guess its file type.
     *
     * @param inputStream file input stream.
     * @param contentType expected content type.
     *
     * @return
     *      returns <code>true</code> if file has given #contentType, otherwise <code>false</code>
     */
    public boolean hasFileContentType(InputStream inputStream, MediaType contentType) {
        try {
            String type = new Tika().detect(inputStream);
            return Objects.equals(type, contentType.toString());
        } catch (IOException e) {
            LOGGER.error("It was not possible to determine the file type but will continue", e);
            return false;
        }
    }

    /**
     * A file type detector for probing a file to guess its file type.
     *
     * The initial bytes in a file examines to guess its file type.
     *
     * @param path path
     * @param contentType expected content type.
     *
     * @return
     *      returns <code>true</code> if file has given #contentType, otherwise <code>false</code>
     */
    public boolean hasFileContentType(Path path, MediaType contentType) {
        try (InputStream inputStream = new FileInputStream(path.toFile())) {
            return hasFileContentType(inputStream, contentType);
        } catch (IOException e) {
            LOGGER.error("It was not possible to determine the file type but will continue", e);
            return false;
        }
    }
}
