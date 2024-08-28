package com.ericsson.de.allure.service.application.util;

import org.junit.Test;

import java.io.IOException;
import java.io.UncheckedIOException;

import static com.ericsson.de.allure.service.application.util.UnsafeIOConsumer.safely;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 07.07.2017
 */
public class UnsafeIOConsumerTest {

    @Test
    public void safely_callable_success() {
        assertThat(safely(this::returnValue)).isEqualTo("value");
    }

    @Test
    public void safely_callable_io_exception() {
        try {
            safely(this::throwIoException);
        } catch (Exception e) {
            assertThat(e).isExactlyInstanceOf(UncheckedIOException.class);
            assertThat(e.getCause()).isExactlyInstanceOf(IOException.class);
        }
    }

    @Test
    public void safely_callable_checked_exception() {
        try {
            safely(this::throwCheckedException);
        } catch (Exception e) {
            assertThat(e).isExactlyInstanceOf(RuntimeException.class);
            assertThat(e.getCause()).isExactlyInstanceOf(InterruptedException.class);
        }
    }

    @Test
    public void safely_callable_runtime_exception() {
        try {
            safely(this::throwRuntimeException);
        } catch (Exception e) {
            assertThat(e).isExactlyInstanceOf(RuntimeException.class);
            assertThat(e.getCause()).isNull();
        }
    }

    private String returnValue() {
        return "value";
    }

    private String throwIoException() throws IOException {
        throw new IOException();
    }

    private String throwCheckedException() throws InterruptedException {
        throw new InterruptedException();
    }

    private String throwRuntimeException() {
        throw new RuntimeException();
    }

}