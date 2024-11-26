package com.lowagie.text;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;

class ImageTest {

    private static final int PERFORMANCE_ITERATIONS = 1;

    @Test
    void performanceTestPngFilename() throws IOException {
        long start = System.nanoTime();
        Image image = null;
        for (int i = 0; i < PERFORMANCE_ITERATIONS; i++) {
            String fileName = "imageTest/ImageTest.png";
            image = Image.getInstance(ClassLoader.getSystemResource(fileName));
        }
        assertNotNull(image, "Image should not be null after performance test for PNG");
        assertThat(image.getRawData()).isNotNull().hasSizeGreaterThan(0);
    }

    @Test
    void performanceTestJpgWithFilename() throws IOException {
        long start = System.nanoTime();
        Image image = null;
        for (int i = 0; i < PERFORMANCE_ITERATIONS; i++) {
            String fileName = "imageTest/ImageTest.jpg";
            image = Image.getInstance(ClassLoader.getSystemResource(fileName));
        }
        assertNotNull(image, "Image should not be null after performance test for JPG");
        assertThat(image.getRawData()).isNotNull().hasSizeGreaterThan(0);
    }

    @Test
    void performanceTestGifWithFilename() throws IOException {
        long start = System.nanoTime();
        Image image = null;
        for (int i = 0; i < PERFORMANCE_ITERATIONS; i++) {
            String fileName = "imageTest/ImageTest.gif";
            image = Image.getInstance(ClassLoader.getSystemResource(fileName));
        }
        assertNotNull(image, "Image should not be null after performance test for GIF");
        assertThat(image.getUrl().toString()).isNotEmpty();
    }

    @Test
    void shouldReturnImageWithUrlForPath() throws Exception {
        String fileName = "H.gif";
        final Image image = Image.getInstance(ClassLoader.getSystemResource(fileName));
        assertNotNull(image.getUrl(), "Image URL should not be null when loaded from a file path");
    }

    private byte[] readFileBytes(String filePath) throws IOException {
        InputStream stream = ClassLoader.getSystemResourceAsStream(filePath);
        if (stream == null) {
            throw new IOException("File not found: " + filePath);
        }
        return stream.readAllBytes();
    }
}
