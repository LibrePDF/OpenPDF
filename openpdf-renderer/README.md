OpenPDF-renderer
============

OpenPDF-renderer is a pure Java library for rendering PDF files as images using Java2D. It provides a lightweight, dependency-free way to convert PDF pages into BufferedImage objects suitable for display in Swing applications or saving to image files.

## Features

- 🎨 **Pure Java PDF Rendering** - No native dependencies required
- 📄 **PDF 1.4 Support** - Renders subset of PDF 1.4 specification
- 🖼️ **Multiple Image Formats** - Convert to PNG, JPEG, or any ImageIO-supported format
- 🎯 **Swing Integration** - Direct rendering to Swing components
- 🔤 **Font Support** - TrueType, Type1, Type3, and CID fonts
- 🌈 **Color Spaces** - RGB, CMYK, Gray, and ICC-based color spaces
- 📝 **Annotations** - Basic support for PDF annotations
- 🎨 **Patterns & Shaders** - Advanced graphics rendering capabilities

## Maven Dependency

Add OpenPDF Renderer to your project:

```xml
<dependency>
    <groupId>com.github.librepdf</groupId>
    <artifactId>openpdf-renderer</artifactId>
    <version>3.0.1-SNAPSHOT</version>
</dependency>
```

## Quick Start

### Basic PDF to Image Conversion

```java
import org.openpdf.renderer.PDFFile;
import org.openpdf.renderer.PDFPage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class PdfToImageExample {
    
    public static void main(String[] args) throws IOException {
        // Load PDF file
        File pdfFile = new File("document.pdf");
        try (RandomAccessFile raf = new RandomAccessFile(pdfFile, "r");
             FileChannel channel = raf.getChannel()) {
            
            ByteBuffer buf = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
            PDFFile pdf = new PDFFile(buf);
            
            // Get first page (1-based index)
            PDFPage page = pdf.getPage(1);
            
            // Create image from page
            Rectangle rect = new Rectangle(0, 0,
                    (int) page.getBBox().getWidth(),
                    (int) page.getBBox().getHeight());
            
            Image img = page.getImage(
                    rect.width, rect.height,  // image size
                    rect,                      // clip rectangle
                    null,                      // image observer
                    true,                      // fill background
                    true                       // block until done
            );
            
            // Convert to BufferedImage and save
            BufferedImage bufferedImage = new BufferedImage(
                    rect.width, rect.height, 
                    BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = bufferedImage.createGraphics();
            g2.drawImage(img, 0, 0, null);
            g2.dispose();
            
            ImageIO.write(bufferedImage, "png", new File("output.png"));
            System.out.println("PDF page rendered successfully!");
        }
    }
}
```

## Examples

For complete working examples, see:
* [ImageRendererTest.java](https://github.com/LibrePDF/OpenPDF/blob/master/openpdf-renderer/src/test/java/openpdf/renderer/ImageRendererTest.java) - Basic PDF to image conversion
* [PdfRendererGui.java](https://github.com/LibrePDF/OpenPDF/blob/master/openpdf-renderer/src/test/java/openpdf/renderer/PdfRendererGui.java) - Interactive Swing PDF viewer

## Building and Testing

### Build the Project

```bash
mvn clean install
```

### Run Tests

```bash
mvn test
```

### Generate Javadoc

```bash
mvn javadoc:javadoc
```

## Important Notes

- **Package Naming**: Classes have been renamed from `com.sun.pdfview` to `org.openpdf.renderer`
- **Encryption**: PDF decryption features have been removed from this module
- **Page Indexing**: Page numbers are 1-based (first page is index 1, not 0)
- **Thread Safety**: PDFFile and PDFPage objects are not thread-safe; synchronize access if needed

## Supported PDF Features

| Feature | Support Level |
|---------|---------------|
| PDF 1.4 Specification | ✅ Partial |
| Basic Text Rendering | ✅ Full |
| TrueType Fonts | ✅ Full |
| Type1 Fonts | ✅ Full |
| CID Fonts | ✅ Partial |
| RGB Color Space | ✅ Full |
| CMYK Color Space | ✅ Full |
| ICC Profiles | ✅ Full |
| Images (JPEG, PNG) | ✅ Full |
| Transparency | ✅ Partial |
| Annotations | ⚠️ Basic |
| Forms | ⚠️ Limited |
| Encryption | ❌ Removed |

## Troubleshooting

### Common Issues

**Issue**: "ICC profile not found" error
```
Solution: Ensure all required ICC profile resources are in your classpath
```

**Issue**: Font rendering issues
```
Solution: Check that required font files are accessible and properly formatted
```

**Issue**: Out of memory errors with large PDFs
```
Solution: Increase JVM heap size with -Xmx flag (e.g., -Xmx2g)
```

### Getting Help

- Report bugs: [GitHub Issues](https://github.com/LibrePDF/OpenPDF/issues)
- Contribute: See [CONTRIBUTING.md](../CONTRIBUTING.md)
- License: [LGPL 2.1](LICENSE.txt)

## Usage

### Advanced: Rendering with Custom Resolution

```java
import org.openpdf.renderer.PDFFile;
import org.openpdf.renderer.PDFPage;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class HighResolutionRenderingExample {
    
    public static BufferedImage renderPageAtDPI(PDFFile pdf, int pageNum, int dpi) 
            throws IOException {
        PDFPage page = pdf.getPage(pageNum);
        
        // Calculate dimensions at desired DPI (default is 72 DPI)
        double scale = dpi / 72.0;
        int width = (int) (page.getBBox().getWidth() * scale);
        int height = (int) (page.getBBox().getHeight() * scale);
        
        Rectangle rect = new Rectangle(0, 0, width, height);
        Image img = page.getImage(width, height, rect, null, true, true);
        
        // Convert to BufferedImage
        BufferedImage bufferedImage = new BufferedImage(
                width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = bufferedImage.createGraphics();
        
        // Enable antialiasing for better quality
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                            RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        g2.drawImage(img, 0, 0, null);
        g2.dispose();
        
        return bufferedImage;
    }
}
```

## License

This project is licensed under the **GNU Lesser General Public License (LGPL) version 2.1**. 

[![License (LGPL version 2.1)](https://img.shields.io/badge/license-GNU%20LGPL%20version%202.1-blue.svg?style=flat-square)](http://opensource.org/licenses/LGPL-2.1)



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




