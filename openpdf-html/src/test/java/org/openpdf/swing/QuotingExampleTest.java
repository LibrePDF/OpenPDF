package org.openpdf.swing;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.openpdf.swing.Java2DRenderer.htmlAsImage;

public class QuotingExampleTest {
    private static final Logger log = LoggerFactory.getLogger(QuotingExampleTest.class);

    @Test
    public void exampleWithQuotes() throws Exception {
        BufferedImage image = htmlAsImage(requireNonNull(getClass().getResourceAsStream("/quotes.xhtml")), 600);
        assertNotNull(image, "Rendered image should not be null");

        File result = new File("target/%s.png".formatted(getClass().getSimpleName()));
        ImageIO.write(image, "png", result);
        log.info("Generated image from html: {}", result.getAbsolutePath());
    }
}
