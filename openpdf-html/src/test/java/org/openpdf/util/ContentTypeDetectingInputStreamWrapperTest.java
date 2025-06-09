package org.openpdf.util;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

class ContentTypeDetectingInputStreamWrapperTest {
    @Test
    void isPdf() throws IOException {
        try (var stream = new ContentTypeDetectingInputStreamWrapper(new ByteArrayInputStream("%PDF1234567890".getBytes(UTF_8)))) {
            assertThat(stream.isPdf()).isTrue();
        }
    }

    @Test
    void isNotPdf() throws IOException {
        try (var stream = new ContentTypeDetectingInputStreamWrapper(new ByteArrayInputStream("Hello, world".getBytes(UTF_8)))) {
            assertThat(stream.isPdf()).isFalse();
        }
    }
}