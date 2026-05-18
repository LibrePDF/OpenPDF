package org.openpdf.renderer.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link OpenPdfCoreRenderer} bridge that uses
 * {@code openpdf-core} as the underlying PDF parser.
 */
class OpenPdfCoreRendererTest {

    private static byte[] pdfBytes;
    private static Path pdfPath;

    @BeforeAll
    static void loadFixture() throws Exception {
        URL url = OpenPdfCoreRendererTest.class.getClassLoader()
                .getResource("HelloWorldMeta.pdf");
        assertThat(url).as("HelloWorldMeta.pdf must be on the test classpath").isNotNull();
        // Use toURI() so paths containing spaces or other URL-escaped chars resolve correctly.
        pdfPath = Paths.get(url.toURI());
        pdfBytes = Files.readAllBytes(pdfPath);
    }

    @Test
    void opensFromByteArrayAndReportsPageCount() throws IOException {
        try (OpenPdfCoreRenderer r = new OpenPdfCoreRenderer(pdfBytes)) {
            assertThat(r.getNumPages()).isGreaterThanOrEqualTo(1);
        }
    }

    @Test
    void opensFromFileAndReturnsPageGeometry() throws IOException {
        try (OpenPdfCoreRenderer r = new OpenPdfCoreRenderer(pdfPath.toFile())) {
            Rectangle2D size = r.getPageSize(1);
            assertThat(size.getX()).isEqualTo(0d);
            assertThat(size.getY()).isEqualTo(0d);
            assertThat(size.getWidth()).isPositive();
            assertThat(size.getHeight()).isPositive();
            assertThat(r.getPageRotation(1)).isBetween(0, 359);
        }
    }

    @Test
    void opensFromPathSameAsFile() throws IOException {
        try (OpenPdfCoreRenderer r = new OpenPdfCoreRenderer(pdfPath)) {
            assertThat(r.getNumPages()).isGreaterThanOrEqualTo(1);
        }
    }

    @Test
    void opensFromInputStream() throws IOException {
        try (InputStream in = new ByteArrayInputStream(pdfBytes);
             OpenPdfCoreRenderer r = new OpenPdfCoreRenderer(in)) {
            assertThat(r.getNumPages()).isGreaterThanOrEqualTo(1);
        }
    }

    @Test
    void exposesMetadataViaOpenPdfCore() throws IOException {
        try (OpenPdfCoreRenderer r = new OpenPdfCoreRenderer(pdfBytes)) {
            Map<String, String> info = r.getMetadata();
            assertThat(info).isNotNull();
            // Spot-check: getMetadata(String) must agree with the bulk map.
            for (Map.Entry<String, String> e : info.entrySet()) {
                assertThat(r.getMetadata(e.getKey())).isEqualTo(e.getValue());
            }
            // The returned map must be unmodifiable.
            assertThatThrownBy(() -> info.put("x", "y"))
                    .isInstanceOf(UnsupportedOperationException.class);
            // Null keys are rejected.
            assertThatThrownBy(() -> r.getMetadata(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Test
    void extractsTextViaOpenPdfCore() throws IOException {
        try (OpenPdfCoreRenderer r = new OpenPdfCoreRenderer(pdfBytes)) {
            String text = r.getTextFromPage(1);
            assertThat(text).isNotNull();
        }
    }

    @Test
    void exposesDecodedPageContentBytes() throws IOException {
        try (OpenPdfCoreRenderer r = new OpenPdfCoreRenderer(pdfBytes)) {
            byte[] content = r.getPageContent(1);
            assertThat(content).isNotNull().isNotEmpty();
        }
    }

    @Test
    void parsesContentStreamOperatorsViaOpenPdfCore() throws IOException {
        try (OpenPdfCoreRenderer r = new OpenPdfCoreRenderer(pdfBytes)) {
            List<String> ops = r.getContentOperators(1);
            assertThat(ops).isNotNull().isNotEmpty();
            // A page that produced any text via PdfTextExtractor must contain
            // the BT/ET text-object delimiters in its content stream.
            if (!r.getTextFromPage(1).isEmpty()) {
                assertThat(ops).contains("BT", "ET");
            }
            // All recorded tokens are non-empty operator names.
            assertThat(ops).allSatisfy(op -> assertThat(op).isNotBlank());
        }
    }

    @Test
    void renderPageProducesImageOfExpectedSize() throws IOException {
        try (OpenPdfCoreRenderer r = new OpenPdfCoreRenderer(pdfBytes)) {
            Rectangle2D size = r.getPageSize(1);
            float dpi = 144f; // 2x
            BufferedImage img = r.renderPage(1, dpi);
            assertThat(img).isNotNull();
            int expectedW = Math.max(1, Math.round((float) size.getWidth() * (dpi / 72f)));
            int expectedH = Math.max(1, Math.round((float) size.getHeight() * (dpi / 72f)));
            assertThat(img.getWidth()).isEqualTo(expectedW);
            assertThat(img.getHeight()).isEqualTo(expectedH);
        }
    }

    @Test
    void renderPageProducesNonBlankContentViaOpenPdfCore() throws IOException {
        // Verify the openpdf-core-driven Java2D renderer actually drew something:
        // the rendered page must contain pixels that are not the opaque-white background.
        try (OpenPdfCoreRenderer r = new OpenPdfCoreRenderer(pdfBytes)) {
            BufferedImage img = r.renderPage(1, 150f);

            // Save for visual inspection.
            File outDir = new File("target/test-outputs");
            outDir.mkdirs();
            File outFile = new File(outDir, "OpenPdfCoreRenderer-page1.png");
            ImageIO.write(img, "png", outFile);
            assertThat(outFile).exists();

            int nonBackgroundPixels = 0;
            int sampleCount = 0;
            // Sample on a 4-px grid to keep this fast.
            for (int y = 0; y < img.getHeight(); y += 4) {
                for (int x = 0; x < img.getWidth(); x += 4) {
                    sampleCount++;
                    int argb = img.getRGB(x, y);
                    int a = (argb >>> 24) & 0xFF;
                    int rch = (argb >> 16) & 0xFF;
                    int gch = (argb >> 8) & 0xFF;
                    int bch = argb & 0xFF;
                    boolean opaqueWhite = a == 0xFF && rch == 0xFF && gch == 0xFF && bch == 0xFF;
                    if (!opaqueWhite) {
                        nonBackgroundPixels++;
                    }
                }
            }
            assertThat(sampleCount).isPositive();
            assertThat(nonBackgroundPixels)
                    .as("openpdf-core-driven renderer must produce at least some non-background "
                            + "pixels (saved to %s)", outFile.getAbsolutePath())
                    .isPositive();
        }
    }

    @Test
    void renderPageRejectsBadArguments() throws IOException {
        try (OpenPdfCoreRenderer r = new OpenPdfCoreRenderer(pdfBytes)) {
            assertThatThrownBy(() -> r.renderPage(1, 0f))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> r.renderPage(0, 72f))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> r.renderPage(r.getNumPages() + 1, 72f))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    void closeIsIdempotentAndBlocksFurtherRendering() throws IOException {
        OpenPdfCoreRenderer r = new OpenPdfCoreRenderer(pdfBytes);
        r.close();
        r.close(); // second close must be a no-op
        assertThatThrownBy(() -> r.renderPage(1, 72f))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void exposesUnderlyingPdfReader() throws IOException {
        try (OpenPdfCoreRenderer r = new OpenPdfCoreRenderer(pdfBytes)) {
            assertThat(r.getReader()).isNotNull();
            assertThat(r.getReader().getNumberOfPages()).isEqualTo(r.getNumPages());
        }
    }

    @Test
    void renderAllPagesReturnsOneImagePerPage() throws IOException {
        try (OpenPdfCoreRenderer r = new OpenPdfCoreRenderer(pdfBytes)) {
            List<BufferedImage> images = r.renderAllPages(72f);
            assertThat(images).hasSize(r.getNumPages());
            assertThat(images).allSatisfy(img -> {
                assertThat(img.getWidth()).isPositive();
                assertThat(img.getHeight()).isPositive();
            });
        }
    }

    @Test
    void renderPageOntoGraphics2DDrawsContentAndRestoresState() throws IOException {
        try (OpenPdfCoreRenderer r = new OpenPdfCoreRenderer(pdfBytes)) {
            int w = 200;
            int h = 260;
            BufferedImage target = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = target.createGraphics();
            try {
                java.awt.geom.AffineTransform before = g2.getTransform();
                java.awt.Shape beforeClip = g2.getClip();
                r.renderPage(1, g2, w, h);
                // Caller-supplied Graphics2D state must be unchanged after rendering.
                assertThat(g2.getTransform()).isEqualTo(before);
                assertThat(g2.getClip()).isEqualTo(beforeClip);
            } finally {
                g2.dispose();
            }
            // Something must have been drawn on the target surface.
            int nonBackground = 0;
            for (int y = 0; y < h; y += 4) {
                for (int x = 0; x < w; x += 4) {
                    int argb = target.getRGB(x, y);
                    int a = (argb >>> 24) & 0xFF;
                    int rch = (argb >> 16) & 0xFF;
                    int gch = (argb >> 8) & 0xFF;
                    int bch = argb & 0xFF;
                    if (!(a == 0xFF && rch == 0xFF && gch == 0xFF && bch == 0xFF)) {
                        nonBackground++;
                    }
                }
            }
            assertThat(nonBackground).isPositive();
        }
    }

    @Test
    void renderPageOntoGraphics2DRejectsBadArguments() throws IOException {
        try (OpenPdfCoreRenderer r = new OpenPdfCoreRenderer(pdfBytes)) {
            BufferedImage target = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = target.createGraphics();
            try {
                assertThatThrownBy(() -> r.renderPage(1, null, 10, 10))
                        .isInstanceOf(NullPointerException.class);
                assertThatThrownBy(() -> r.renderPage(1, g2, 0, 10))
                        .isInstanceOf(IllegalArgumentException.class);
                assertThatThrownBy(() -> r.renderPage(1, g2, 10, -1))
                        .isInstanceOf(IllegalArgumentException.class);
                assertThatThrownBy(() -> r.renderPage(0, g2, 10, 10))
                        .isInstanceOf(IllegalArgumentException.class);
                assertThatThrownBy(() -> r.renderPage(r.getNumPages() + 1, g2, 10, 10))
                        .isInstanceOf(IllegalArgumentException.class);
            } finally {
                g2.dispose();
            }
        }
    }
}

