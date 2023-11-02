package com.lowagie.text;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
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
        final Image image = Image.getInstance(ClassLoader.getSystemResource("H.gif").getPath());
        assertNotNull(image.getUrl());
    }

    @Test
    void performanceTestPng() throws IOException {
        long start = System.nanoTime();
        Image image = null;
        for (int i = 0; i < PERFORMANCE_ITERATIONS; i++) {
            image = Image.getInstance(ClassLoader.getSystemResource("imageTest/ImageTest.png").getPath());
        }
        long deltaMillis = (System.nanoTime() - start) / 1_000_000 / PERFORMANCE_ITERATIONS;
        if (PERFORMANCE_ITERATIONS > 1) {
            System.out.format("Load PNG ~time after %d iterations %d ms%n", PERFORMANCE_ITERATIONS, deltaMillis);
        }
        assertNotNull(image.getUrl());
    }

    @Test
    void performanceTestJpg() throws IOException {
        long start = System.nanoTime();
        Image image = null;
        for (int i = 0; i < PERFORMANCE_ITERATIONS; i++) {
            image = Image.getInstance(ClassLoader.getSystemResource("imageTest/ImageTest.jpg").getPath());
        }
        long deltaMillis = (System.nanoTime() - start) / 1_000_000 / PERFORMANCE_ITERATIONS;
        if (PERFORMANCE_ITERATIONS > 1) {
            System.out.format("Load JPG ~time after %d iterations %d ms%n", PERFORMANCE_ITERATIONS, deltaMillis);
        }
        assertNotNull(image.getUrl());
    }

    @Test
    void performanceTestGif() throws IOException {
        long start = System.nanoTime();
        Image image = null;
        for (int i = 0; i < PERFORMANCE_ITERATIONS; i++) {
            image = Image.getInstance(ClassLoader.getSystemResource("imageTest/ImageTest.gif").getPath());
        }
        long deltaMillis = (System.nanoTime() - start) / 1_000_000 / PERFORMANCE_ITERATIONS;
        if (PERFORMANCE_ITERATIONS > 1) {
            System.out.format("Load GIF ~time after %d iterations %d ms%n", PERFORMANCE_ITERATIONS, deltaMillis);
        }
        assertThat(deltaMillis).isLessThan(200);
        assertNotNull(image.getUrl());
    }
}
