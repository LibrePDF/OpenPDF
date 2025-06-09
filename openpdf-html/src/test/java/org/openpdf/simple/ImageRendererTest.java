package org.openpdf.simple;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.File;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

class ImageRendererTest {
    private static final Logger log = LoggerFactory.getLogger(ImageRendererTest.class);

    @Test
    public void backgroundImageWithRelativePath() throws Exception {
        File result = new File("target/%s.png".formatted(getClass().getSimpleName()));
        BufferedImage image = ImageRenderer.renderToImage(requireNonNull(getClass().getResource("/text-with-background-image.xhtml")), result.getAbsolutePath(), 600);
        assertThat(image.getWidth()).isEqualTo(600);
        assertThat(image.getHeight()).isBetween(100, 200);
        log.info("Generated image from html: {}", result.getAbsolutePath());
    }
}