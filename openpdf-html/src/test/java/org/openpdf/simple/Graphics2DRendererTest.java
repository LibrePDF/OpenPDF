package org.openpdf.simple;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Percentage.withPercentage;


public class Graphics2DRendererTest {
    private static final Logger log = LoggerFactory.getLogger(Graphics2DRendererTest.class);

    @Test
    public void rendersHtmlToImage() throws IOException {
        URL source = requireNonNull(Thread.currentThread().getContextClassLoader().getResource("hello.html"));
        File output = File.createTempFile("flying-saucer-" + getClass().getSimpleName(), ".png");

        BufferedImage image = Graphics2DRenderer.renderToImage(source.toExternalForm(), 600, 200);

        assertThat(ImageIO.write(image, "png", output)).isTrue();
        log.info("Rendered {} to image: {}", source, output.toURI());
        assertThat(image.getWidth()).isEqualTo(600);
        assertThat(image.getHeight()).isEqualTo(200);
    }

    @Test
    public void rendersHtmlToImage_hamlet() throws IOException {
        URL source = requireNonNull(Thread.currentThread().getContextClassLoader().getResource("hamlet.xhtml"));
        File output = File.createTempFile("flying-saucer-" + getClass().getSimpleName(), ".png");

        BufferedImage image = Graphics2DRenderer.renderToImage(source.toExternalForm(), 700, 7700);

        assertThat(ImageIO.write(image, "png", output)).isTrue();
        log.info("Rendered {} to image: {}", source, output.toURI());
        assertThat(image.getWidth()).isEqualTo(700);
        assertThat(image.getHeight()).isEqualTo(7700);
    }

    @Test
    public void renderToImageAutoSize() throws IOException {
        URL source = requireNonNull(Thread.currentThread().getContextClassLoader().getResource("hello.html"));
        File output = File.createTempFile("flying-saucer-" + getClass().getSimpleName(), ".png");

        BufferedImage image = Graphics2DRenderer.renderToImageAutoSize(source.toExternalForm(), 600, TYPE_INT_ARGB);

        assertThat(ImageIO.write(image, "png", output)).isTrue();
        log.info("Rendered {} to image: {}", source, output.toURI());
        assertThat(image.getWidth()).isEqualTo(600);
        assertThat(image.getHeight()).isCloseTo(80, withPercentage(10));
    }
}
