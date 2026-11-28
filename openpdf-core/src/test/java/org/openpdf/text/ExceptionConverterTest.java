package org.openpdf.text;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.junit.jupiter.api.Test;

/**
 * Tests for issue #1296: {@code ExceptionConverter.getStackTrace()} always returned an empty
 * array because {@code fillInStackTrace()} was suppressed, and {@code getCause()} returned null
 * because the wrapped exception was kept in a private field. Logging frameworks therefore printed
 * no stack trace at all when an ExceptionConverter propagated to user code.
 */
class ExceptionConverterTest {

    @Test
    void stackTraceIsNotEmpty() {
        ExceptionConverter converter = new ExceptionConverter(new IOException("test"));
        assertTrue(converter.getStackTrace().length > 0, "getStackTrace() must not be empty");
    }

    @Test
    void wrappedExceptionIsTheCause() {
        IOException wrapped = new IOException("test");
        ExceptionConverter converter = new ExceptionConverter(wrapped);
        assertSame(wrapped, converter.getCause(), "The wrapped exception must be the cause");
        assertSame(wrapped, converter.getException(), "getException() must return the wrapped exception");
    }

    @Test
    void messageDelegatesToWrappedException() {
        ExceptionConverter converter = new ExceptionConverter(new IOException("test"));
        assertEquals("test", converter.getMessage());
        assertEquals("ExceptionConverter: java.io.IOException: test", converter.toString());
        // no prefix when the wrapped exception is already unchecked
        assertEquals("java.lang.IllegalStateException: oops",
                new ExceptionConverter(new IllegalStateException("oops")).toString());
    }

    @Test
    void printStackTraceContainsWrappedExceptionTrace() {
        ExceptionConverter converter = new ExceptionConverter(new IOException("test"));
        StringWriter out = new StringWriter();
        converter.printStackTrace(new PrintWriter(out));
        String trace = out.toString();
        assertTrue(trace.contains("java.io.IOException: test"),
                "The stack trace must contain the wrapped exception");
        assertTrue(trace.contains("printStackTraceContainsWrappedExceptionTrace"),
                "The stack trace must contain the frame where the exception was converted");
    }

    @Test
    void convertExceptionReturnsRuntimeExceptionsUnwrapped() {
        IllegalStateException unchecked = new IllegalStateException("oops");
        assertSame(unchecked, ExceptionConverter.convertException(unchecked));
        IOException checked = new IOException("test");
        RuntimeException converted = ExceptionConverter.convertException(checked);
        assertSame(checked, converted.getCause());
    }
}
