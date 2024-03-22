package com.lowagie.text;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;

class ImageTest {

    // For performance testing, set this to something > 100
    private static final int PERFORMANCE_ITERATIONS = 1;

    @Test
    void shouldReturnImageWithUrlForUrl() throws Exception {
        final Image image = Image.getInstance(ClassLoader.getSystemResource("H.gif"));
        assertNotNull(image.getUrl());
    }

    @Test
    void shouldReturnImageWithUrlForPath() throws Exception {
        String fileName = "src/test/resources/H.gif";
        final Image image = Image.getInstance(fileName);
        assertNotNull(image.getUrl());
    }

    @Test
    void shouldReturnImageWithUrlFromClasspath() throws Exception {
        String fileName = "H.gif";
        final Image image = Image.getInstanceFromClasspath(fileName);
        assertNotNull(image.getUrl());
    }

    @Test
    void shouldReturnImageWithoutUrl() throws IOException {
        byte[] imageBytes = readFileBytes();
        Image image = Image.getInstance(imageBytes);
        assertNotNull(image);
        assertNull(image.getUrl());
        assertThat(image.getRawData()).isNotEmpty();
    }

    @Test
    void performanceTestPngFilename() throws IOException {
        long start = System.nanoTime();
        Image image = null;
        for (int i = 0; i < PERFORMANCE_ITERATIONS; i++) {
            String fileName = "src/test/resources/imageTest/ImageTest.png";
            image = Image.getInstance(fileName);
        }
        long deltaMillis = (System.nanoTime() - start) / 1_000_000 / PERFORMANCE_ITERATIONS;
        if (PERFORMANCE_ITERATIONS > 1) {
            System.out.format("Load PNG ~time after %d iterations %d ms%n", PERFORMANCE_ITERATIONS, deltaMillis);
        }
        assertNotNull(image);
        assertThat(image.getRawData()).isNotEmpty();
    }

    @Test
    void performanceTestJpgWithFilename() throws IOException {
        long start = System.nanoTime();
        Image image = null;
        for (int i = 0; i < PERFORMANCE_ITERATIONS; i++) {
            String fileName = "src/test/resources/imageTest/ImageTest.jpg";
            image = Image.getInstance(fileName);
        }
        long deltaMillis = (System.nanoTime() - start) / 1_000_000 / PERFORMANCE_ITERATIONS;
        if (PERFORMANCE_ITERATIONS > 1) {
            System.out.format("Load JPG ~time after %d iterations %d ms%n", PERFORMANCE_ITERATIONS, deltaMillis);
        }
        assertNotNull(image.getUrl());
        assertThat(image.getRawData()).isNotEmpty();
    }

    @Test
    void performanceTestGifWithFilename() throws IOException {
        long start = System.nanoTime();
        Image image = null;
        for (int i = 0; i < PERFORMANCE_ITERATIONS; i++) {
            String fileName = "src/test/resources/imageTest/ImageTest.gif";
            image = Image.getInstance(fileName);
        }
        long deltaMillis = (System.nanoTime() - start) / 1_000_000 / PERFORMANCE_ITERATIONS;
        if (PERFORMANCE_ITERATIONS > 1) {
            System.out.format("Load GIF ~time after %d iterations %d ms%n", PERFORMANCE_ITERATIONS, deltaMillis);
        }
        assertThat(deltaMillis).isLessThan(200);
        assertNotNull(image.getUrl());
    }

    private byte[] readFileBytes() throws IOException {
        byte[] bytes = null;
        try (InputStream stream = this.getClass().getResourceAsStream("/imageTest/ImageTest.png")) {
            if (stream != null) {
                bytes = stream.readAllBytes();
            }
        }
        return bytes;
    }

}
