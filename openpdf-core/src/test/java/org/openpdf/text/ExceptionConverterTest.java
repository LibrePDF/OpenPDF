package org.openpdf.text;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import org.junit.jupiter.api.Test;

class ExceptionConverterTest {

    @Test
    void shouldCopyStackTraceFromConvertedException() {
        // Given
        IOException cause = new IOException("test");

        // When
        ExceptionConverter converter = new ExceptionConverter(cause);

        // Then
        assertThat(converter.getStackTrace())
                .isNotEmpty()
                .containsExactly(cause.getStackTrace());
    }
}
