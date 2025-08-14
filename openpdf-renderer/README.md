OpenPDF-renderer
============

OpenPDF-renderer is a Java library for rendering PDF files as images.

## License

This project is licensed under the **GNU Lesser General Public License (LGPL)**. 

[![License (LGPL version 2.1)](https://img.shields.io/badge/license-GNU%20LGPL%20version%202.1-blue.svg?style=flat-square)](http://opensource.org/licenses/LGPL-2.1)

Examples:
========
* [Render PDF as image](https://github.com/LibrePDF/OpenPDF/blob/master/openpdf-renderer/src/test/java/openpdf/renderer/ImageRendererTest.java)
* [Render PDF in Swing GUI](https://github.com/LibrePDF/OpenPDF/blob/master/openpdf-renderer/src/test/java/openpdf/renderer/PdfRendererGui.java)


Note
=====
* Package names renamed from com.sun.pdfview to org.openpdf.renderer.
* PDF decryption is removed from openpdf-renderer.

Usage
=====

```java

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

            
            System.out.println("PDF page rendered and saved to: " + outputImageFile.getAbsolutePath());
        }
    }
}
```

## History

OpenPDF-renderer is a fork from https://github.com/katjas/PDFrenderer (forked from http://java.net/projects/pdf-renderer) forked in june 2025, and is used in accordance with the license: https://github.com/katjas/PDFrenderer/blob/master/LICENSE.txt

### PDFRenderer – Background & Origin

- **Background – Sun Labs**  
  - Researchers at **Sun Labs** created the **all-Java PDF Renderer** to drive a lightweight PDF viewer for OpenOffice, rendering content via Java2D without needing full browser or native libraries.  


- **Open-Sourcing of the Project**  
  - When the internal needs waned, Sun Labs offered the project for open-source release. It was adopted and championed by **Josh Marinacci** and **Richard Bair** (of SwingLabs fame).  
  - **Tom Oke** took lead in further development, and the first open-source release appeared publicly in **December 2007**, under the **LGPL license**.  

- **Key Motivations**  
  - Java lacked a built-in way to render PDFs. This project enabled:
    - Previewing PDFs in Swing applications,
    - Rendering pages as images for printing or embedding,
    - Using pure Java (Java2D) without external dependencies.  

- **Legacy & Influence**  
  - **katjas/PDFrenderer** is a modern GitHub-hosted fork of the java.net version. Its goal: fix bugs, improve functionality, and modernize the rendering pipeline.  
  - Enhancements include:
    - Better colour space handling,
    - Support for more PDF features (annotations, fonts, JBIG2 decoding, etc.).  




