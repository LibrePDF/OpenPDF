package org.openpdf.swing;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;

import static java.awt.Color.RED;
import static java.awt.Color.WHITE;
import static java.awt.image.BufferedImage.TYPE_4BYTE_ABGR;
import static java.awt.image.BufferedImage.TYPE_4BYTE_ABGR_PRE;
import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static java.awt.image.BufferedImage.TYPE_INT_ARGB_PRE;
import static org.junit.jupiter.api.Assertions.assertEquals;

class Java2DRendererTest {
  private static final Logger log = LoggerFactory.getLogger(Java2DRendererTest.class);

  @ParameterizedTest
  @ValueSource(ints = {TYPE_INT_ARGB, TYPE_INT_ARGB_PRE, TYPE_4BYTE_ABGR, TYPE_4BYTE_ABGR_PRE})
  void convertHtmlToImage_withResizedEmbeddedABGRImage(int imageType) throws Exception {
    BufferedImage smallImg = create2PixelImage(imageType);
      String html = """
            <html><body>
            <img width="100" src="data:image/png;base64,%s"/>
            <h1>Hello</h1></body></html>""".formatted(imageBase64(smallImg));
    BufferedImage htmlAsImage = Java2DRenderer.htmlAsImage(html, 100);

    File result = new File("target/%s.%s.png".formatted(getClass().getSimpleName(), imageType));
    ImageIO.write(htmlAsImage, "png", result);
    log.info("Generated image from html: {}", result.getAbsolutePath());

    assertEquals(RED.getRGB(), htmlAsImage.getRGB(75, 25));
    assertEquals(WHITE.getRGB(), htmlAsImage.getRGB(25, 25));
  }

  private static BufferedImage create2PixelImage(int imageType) {
    BufferedImage img = new BufferedImage(2, 1, imageType);
    img.setRGB(0, 0, 0);
    img.setRGB(1, 0, 0xFFFF0000);
    return img;
  }

  private static String imageBase64(BufferedImage img) throws IOException {
    try (ByteArrayOutputStream bytes = new ByteArrayOutputStream(96)) {
      ImageIO.write(img, "png", bytes);
      return Base64.getEncoder().encodeToString(bytes.toByteArray());
    }
  }
}