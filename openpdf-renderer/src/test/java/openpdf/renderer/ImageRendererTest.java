package openpdf.renderer;

import org.openpdf.renderer.PDFFile;
import org.openpdf.renderer.PDFPage;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import static org.junit.jupiter.api.Assertions.*;

public class ImageRendererTest {

    @Test
    public void testRenderPdfPageToImage() throws Exception {
        int pageIndex = 1; // 1-based index

        // Load PDF file from test resources
        URL resourceUrl = getClass().getClassLoader().getResource("HelloWorldMeta.pdf");
        assertNotNull(resourceUrl, "PDF resource not found in classpath");

        File file = new File(resourceUrl.getFile());
        assertTrue(file.exists(), "PDF file does not exist");

        try (FileInputStream fis = new FileInputStream(file);
                FileChannel fc = fis.getChannel()) {

            ByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            PDFFile pdfFile = new PDFFile(bb);
            PDFPage page = pdfFile.getPage(pageIndex);

            Rectangle rect = new Rectangle(0, 0,
                    (int) page.getBBox().getWidth(),
                    (int) page.getBBox().getHeight());

            Image img = page.getImage(rect.width, rect.height, rect, null, true, true);

            // Convert to BufferedImage
            BufferedImage bufferedImage = new BufferedImage(rect.width, rect.height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = bufferedImage.createGraphics();
            g2.drawImage(img, 0, 0, null);
            g2.dispose();

            // Save output image to target/test-output
            File outputDir = new File("target/test-output");
            outputDir.mkdirs();
            File outputImageFile = new File(outputDir, "page_" + pageIndex + ".png");
            ImageIO.write(bufferedImage, "png", outputImageFile);

            assertTrue(outputImageFile.exists(), "Output image file was not created");
            assertTrue(outputImageFile.length() > 0, "Output image file is empty");

            System.out.println("PDF page rendered and saved to: " + outputImageFile.getAbsolutePath());
        }
    }
}
