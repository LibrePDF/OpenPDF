package org.openpdf.text;

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

    @Test
    void shouldDetectIndexedColorGif() throws Exception {
        // Load H.gif which is an indexed color GIF
        String fileName = "src/test/resources/H.gif";
        Image image = Image.getInstance(fileName);
        
        assertNotNull(image);
        // colorspace should be 1 for indexed images (not 3 for RGB)
        assertThat(image.getColorspace()).isEqualTo(1);
        
        // Verify that additional colorspace info is set for indexed images
        assertThat(image.getAdditional()).isNotNull();
        assertThat(image.getAdditional().get(org.openpdf.text.pdf.PdfName.COLORSPACE)).isNotNull();
    }

    @Test
    void shouldDetectIndexedColorFromBufferedImage() throws Exception {
        // Create an indexed color BufferedImage programmatically
        int width = 10;
        int height = 10;
        
        // Create a simple 4-color palette (red, green, blue, black)
        byte[] reds = {(byte)255, 0, 0, 0};
        byte[] greens = {0, (byte)255, 0, 0};
        byte[] blues = {0, 0, (byte)255, 0};
        
        java.awt.image.IndexColorModel colorModel = new java.awt.image.IndexColorModel(
            2, // 2 bits per pixel (4 colors)
            4, // 4 colors in palette
            reds, greens, blues
        );
        
        java.awt.image.BufferedImage bufferedImage = new java.awt.image.BufferedImage(
            width, height, java.awt.image.BufferedImage.TYPE_BYTE_INDEXED, colorModel
        );
        
        // Fill with some pattern
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                bufferedImage.getRaster().setSample(x, y, 0, (x + y) % 4);
            }
        }
        
        // Convert to Image
        Image image = Image.getInstance(bufferedImage, null);
        
        assertNotNull(image);
        // Should be indexed (colorspace = 1), not RGB (colorspace = 3)
        assertThat(image.getColorspace()).isEqualTo(1);
        
        // Verify that additional colorspace info is set
        assertThat(image.getAdditional()).isNotNull();
        assertThat(image.getAdditional().get(org.openpdf.text.pdf.PdfName.COLORSPACE)).isNotNull();
    }

}
