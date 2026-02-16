package org.openpdf.renderer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive unit tests for PDF rendering functionality.
 * Tests various rendering scenarios including different sizes, clips, and page properties.
 */
class PDFRenderingTest {

    @Test
    void testRenderPdfPage_BasicRendering() throws Exception {
        // given
        URL resourceUrl = getClass().getClassLoader().getResource("HelloWorldMeta.pdf");
        assertThat(resourceUrl).isNotNull();
        
        File file = new File(resourceUrl.getFile());
        
        // when
        try (FileInputStream fis = new FileInputStream(file);
             FileChannel fc = fis.getChannel()) {
            
            ByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            PDFFile pdfFile = new PDFFile(bb);
            PDFPage page = pdfFile.getPage(1);
            
            Rectangle2D bbox = page.getBBox();
            Image img = page.getImage((int) bbox.getWidth(), (int) bbox.getHeight(), 
                                     bbox, null, true, true);
            
            // then
            assertThat(img).isNotNull();
            assertThat(img.getWidth(null)).isGreaterThan(0);
            assertThat(img.getHeight(null)).isGreaterThan(0);
        }
    }

    @Test
    void testRenderPdfPage_CustomDimensions() throws Exception {
        // given
        URL resourceUrl = getClass().getClassLoader().getResource("HelloWorldMeta.pdf");
        assertThat(resourceUrl).isNotNull();
        
        File file = new File(resourceUrl.getFile());
        
        // when - render at different size (200x300)
        try (FileInputStream fis = new FileInputStream(file);
             FileChannel fc = fis.getChannel()) {
            
            ByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            PDFFile pdfFile = new PDFFile(bb);
            PDFPage page = pdfFile.getPage(1);
            
            Rectangle2D bbox = page.getBBox();
            Image img = page.getImage(200, 300, bbox, null, true, true);
            
            // then
            assertThat(img).isNotNull();
            assertThat(img.getWidth(null)).isEqualTo(200);
            assertThat(img.getHeight(null)).isEqualTo(300);
        }
    }

    @Test
    void testRenderPdfPage_WithoutBackground() throws Exception {
        // given
        URL resourceUrl = getClass().getClassLoader().getResource("HelloWorldMeta.pdf");
        assertThat(resourceUrl).isNotNull();
        
        File file = new File(resourceUrl.getFile());
        
        // when - render without background
        try (FileInputStream fis = new FileInputStream(file);
             FileChannel fc = fis.getChannel()) {
            
            ByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            PDFFile pdfFile = new PDFFile(bb);
            PDFPage page = pdfFile.getPage(1);
            
            Rectangle2D bbox = page.getBBox();
            Image img = page.getImage((int) bbox.getWidth(), (int) bbox.getHeight(), 
                                     bbox, null, false, true);
            
            // then
            assertThat(img).isNotNull();
        }
    }

    @Test
    void testRenderPdfPage_ClippedRegion() throws Exception {
        // given
        URL resourceUrl = getClass().getClassLoader().getResource("HelloWorldMeta.pdf");
        assertThat(resourceUrl).isNotNull();
        
        File file = new File(resourceUrl.getFile());
        
        // when - render only a portion of the page (top-left quadrant)
        try (FileInputStream fis = new FileInputStream(file);
             FileChannel fc = fis.getChannel()) {
            
            ByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            PDFFile pdfFile = new PDFFile(bb);
            PDFPage page = pdfFile.getPage(1);
            
            Rectangle2D bbox = page.getBBox();
            Rectangle2D clip = new Rectangle2D.Double(0, 0, 
                bbox.getWidth() / 2, bbox.getHeight() / 2);
            
            Image img = page.getImage(100, 100, clip, null, true, true);
            
            // then
            assertThat(img).isNotNull();
            assertThat(img.getWidth(null)).isEqualTo(100);
            assertThat(img.getHeight(null)).isEqualTo(100);
        }
    }

    @Test
    void testGetPdfPageProperties() throws Exception {
        // given
        URL resourceUrl = getClass().getClassLoader().getResource("HelloWorldMeta.pdf");
        assertThat(resourceUrl).isNotNull();
        
        File file = new File(resourceUrl.getFile());
        
        // when
        try (FileInputStream fis = new FileInputStream(file);
             FileChannel fc = fis.getChannel()) {
            
            ByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            PDFFile pdfFile = new PDFFile(bb);
            PDFPage page = pdfFile.getPage(1);
            
            // then
            assertThat(page.getPageNumber()).isEqualTo(1);
            assertThat(page.getWidth()).isGreaterThan(0);
            assertThat(page.getHeight()).isGreaterThan(0);
            assertThat(page.getBBox()).isNotNull();
            assertThat(page.getAspectRatio()).isGreaterThan(0);
        }
    }

    @Test
    void testGetPdfFileProperties() throws Exception {
        // given
        URL resourceUrl = getClass().getClassLoader().getResource("HelloWorldMeta.pdf");
        assertThat(resourceUrl).isNotNull();
        
        File file = new File(resourceUrl.getFile());
        
        // when
        try (FileInputStream fis = new FileInputStream(file);
             FileChannel fc = fis.getChannel()) {
            
            ByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            PDFFile pdfFile = new PDFFile(bb);
            
            // then
            assertThat(pdfFile.getNumPages()).isGreaterThan(0);
            assertThat(pdfFile.isPrintable()).isTrue();
            assertThat(pdfFile.isSaveable()).isTrue();
        }
    }

    @Test
    void testRenderMultiplePages() throws Exception {
        // given
        URL resourceUrl = getClass().getClassLoader().getResource("HelloWorldMeta.pdf");
        assertThat(resourceUrl).isNotNull();
        
        File file = new File(resourceUrl.getFile());
        
        // when
        try (FileInputStream fis = new FileInputStream(file);
             FileChannel fc = fis.getChannel()) {
            
            ByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            PDFFile pdfFile = new PDFFile(bb);
            
            int numPages = pdfFile.getNumPages();
            assertThat(numPages).isGreaterThan(0);
            
            // Render each page
            for (int i = 1; i <= numPages; i++) {
                PDFPage page = pdfFile.getPage(i);
                assertThat(page).isNotNull();
                
                Rectangle2D bbox = page.getBBox();
                Image img = page.getImage(100, 100, bbox, null, true, true);
                
                assertThat(img).isNotNull();
            }
        }
    }

    @Test
    void testRenderPdfToBufferedImageAndSaveToFile(@TempDir File tempDir) throws Exception {
        // given
        URL resourceUrl = getClass().getClassLoader().getResource("HelloWorldMeta.pdf");
        assertThat(resourceUrl).isNotNull();
        
        File file = new File(resourceUrl.getFile());
        
        // when
        try (FileInputStream fis = new FileInputStream(file);
             FileChannel fc = fis.getChannel()) {
            
            ByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            PDFFile pdfFile = new PDFFile(bb);
            PDFPage page = pdfFile.getPage(1);
            
            Rectangle2D bbox = page.getBBox();
            int width = (int) bbox.getWidth();
            int height = (int) bbox.getHeight();
            
            Image img = page.getImage(width, height, bbox, null, true, true);
            
            // Convert to BufferedImage
            BufferedImage bufferedImage = new BufferedImage(width, height, 
                                                           BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = bufferedImage.createGraphics();
            g2.drawImage(img, 0, 0, null);
            g2.dispose();
            
            // Save to file
            File outputFile = new File(tempDir, "rendered_page.png");
            ImageIO.write(bufferedImage, "png", outputFile);
            
            // then
            assertThat(outputFile).exists();
            assertThat(outputFile.length()).isGreaterThan(0);
            
            // Verify the image can be read back
            BufferedImage readImage = ImageIO.read(outputFile);
            assertThat(readImage).isNotNull();
            assertThat(readImage.getWidth()).isEqualTo(width);
            assertThat(readImage.getHeight()).isEqualTo(height);
        }
    }

    @Test
    void testRenderPdfAtDifferentScales() throws Exception {
        // given
        URL resourceUrl = getClass().getClassLoader().getResource("HelloWorldMeta.pdf");
        assertThat(resourceUrl).isNotNull();
        
        File file = new File(resourceUrl.getFile());
        
        // when - render at different scales
        try (FileInputStream fis = new FileInputStream(file);
             FileChannel fc = fis.getChannel()) {
            
            ByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            PDFFile pdfFile = new PDFFile(bb);
            PDFPage page = pdfFile.getPage(1);
            
            Rectangle2D bbox = page.getBBox();
            int originalWidth = (int) bbox.getWidth();
            int originalHeight = (int) bbox.getHeight();
            
            // Test at 50% scale
            Image img50 = page.getImage(originalWidth / 2, originalHeight / 2, 
                                       bbox, null, true, true);
            assertThat(img50.getWidth(null)).isEqualTo(originalWidth / 2);
            assertThat(img50.getHeight(null)).isEqualTo(originalHeight / 2);
            
            // Test at 200% scale
            Image img200 = page.getImage(originalWidth * 2, originalHeight * 2, 
                                        bbox, null, true, true);
            assertThat(img200.getWidth(null)).isEqualTo(originalWidth * 2);
            assertThat(img200.getHeight(null)).isEqualTo(originalHeight * 2);
        }
    }

    @Test
    void testPageRotation() throws Exception {
        // given
        URL resourceUrl = getClass().getClassLoader().getResource("HelloWorldMeta.pdf");
        assertThat(resourceUrl).isNotNull();
        
        File file = new File(resourceUrl.getFile());
        
        // when
        try (FileInputStream fis = new FileInputStream(file);
             FileChannel fc = fis.getChannel()) {
            
            ByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            PDFFile pdfFile = new PDFFile(bb);
            PDFPage page = pdfFile.getPage(1);
            
            // then - verify rotation property exists
            assertThat(page.getRotation()).isIn(0, 90, 180, 270);
        }
    }

    @Test
    void testGetMetadata() throws Exception {
        // given
        URL resourceUrl = getClass().getClassLoader().getResource("HelloWorldMeta.pdf");
        assertThat(resourceUrl).isNotNull();
        
        File file = new File(resourceUrl.getFile());
        
        // when
        try (FileInputStream fis = new FileInputStream(file);
             FileChannel fc = fis.getChannel()) {
            
            ByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            PDFFile pdfFile = new PDFFile(bb);
            
            // then - check if metadata can be retrieved (may be null for some PDFs)
            String title = pdfFile.getStringMetadata("Title");
            String author = pdfFile.getStringMetadata("Author");
            String creator = pdfFile.getStringMetadata("Creator");
            
            // Metadata might be null, but the method should work without errors
            // Just verify it doesn't throw an exception
            assertThat(pdfFile).isNotNull();
        }
    }
}
