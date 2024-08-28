package com.ericsson.de.allure.service.application.util;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

@FunctionalInterface
public interface UnsafeIOConsumer<T> {

    void accept(T t) throws IOException;

    /**
     * Special wrapper for being able to use method
     * references for methods throwing IOException:
     * <pre>
     *     Stream.of(1, 2, 3).forEach(this::foo); // compiles
     *     Stream.of(1, 2, 3).forEach(this::bar); // does not compile
     *     Stream.of(1, 2, 3).forEach(safely(this::bar)); // compiles
     *     ...
     *     void foo(Integer foo) { ... }
     *     ...
     *     void bar(Integer bar) throws IOException { ... }
     * </pre>
     */
    static <T> Consumer<T> safely(UnsafeIOConsumer<T> unsafe) {
        return path -> {
            try {
                unsafe.accept(path);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };
    }

    /**
     * Wraps your code throwing checked exceptions into one throwing unchecked exceptions.
     */
    static <T> T safely(Callable<T> unsafe) {
        try {
            return unsafe.call();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (Exception e) {
            if (RuntimeException.class.isInstance(e)) {
                throw RuntimeException.class.cast(e);
            }
            throw new RuntimeException(e);
        }
    }

}
