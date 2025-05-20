package com.lowagie.text;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import org.junit.jupiter.api.Test;

class ImageTest {

    // For performance testing, set this to something > 100
    private static final int PERFORMANCE_ITERATIONS = 1;


    @Test
    void shouldReturnImageForBase64DataPNG() throws Exception {
        final Image image = Image.getInstance(
                "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAUAAAAFCAYAAACNbyblAAAAHElEQVQI12P4//8/w38GIAXDIBKE0DHxgljNBAAO9TXL0Y4OHwAAAABJRU5ErkJggg==");
        assertNotNull(image);
    }

    @Test
    void shouldReturnImageForBase64DataJPEG() throws Exception {
        final Image image = Image.getInstance(
                "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAZABkAAD/2wCEABQQEBkSGScXFycyJh8mMi4mJiYmLj41NTU1NT5EQUFBQUFBRERE"
                        + "REREREREREREREREREREREREREREREREREQBFRkZIBwgJhgYJjYmICY2RDYrKzZERERCNUJERERERERERERERERERERERERERERERERERE"
                        + "RERERERERERERERP/AABEIAAEAAQMBIgACEQEDEQH/xABMAAEBAAAAAAAAAAAAAAAAAAAABQEBAQAAAAAAAAAAAAAAAAAABQYQAQAAAAAA"
                        + "AAAAAAAAAAAAAAARAQAAAAAAAAAAAAAAAAAAAAD/2gAMAwEAAhEDEQA/AJQA9Yv/2Q==");
        assertNotNull(image);
    }


    @Test
    void performanceTestPngFilename() throws IOException {
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
    
}
