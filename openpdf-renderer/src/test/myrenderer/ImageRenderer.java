package test.myrenderer;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;

/**
 * Render PDF file to image.
 */
public class ImageRenderer {

    public static void main(String[] args) {
        String fileName = Paths.get(System.getProperty("user.dir"),
                "openpdf-renderer/src/test/myrenderer/HelloWorldMeta.pdf").toString();
        int pageIndex = 1; // 1-based index
        String outputImagePath = Paths.get(System.getProperty("user.dir"),
                "openpdf-renderer/src/test/myrenderer/page_" + pageIndex + ".png").toString();

        try {
            File file = new File(fileName);
            FileInputStream fis = new FileInputStream(file);
            FileChannel fc = fis.getChannel();
            ByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());

            PDFFile pdfFile = new PDFFile(bb);
            PDFPage page = pdfFile.getPage(pageIndex);

            Rectangle rect = new Rectangle(0, 0,
                    (int) page.getBBox().getWidth(),
                    (int) page.getBBox().getHeight());

            Image img = page.getImage(rect.width, rect.height, rect, null, true, true);

            // Convert Image to BufferedImage
            BufferedImage bufferedImage = new BufferedImage(rect.width, rect.height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = bufferedImage.createGraphics();
            g2.drawImage(img, 0, 0, null);
            g2.dispose();

            // Save to disk as PNG
            File outputImageFile = new File(outputImagePath);
            ImageIO.write(bufferedImage, "png", outputImageFile);

            System.out.println("Saved image to: " + outputImageFile.getAbsolutePath());

            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
