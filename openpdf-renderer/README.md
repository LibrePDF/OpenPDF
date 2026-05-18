# OpenPDF-renderer

OpenPDF-renderer is a pure Java library for rendering PDF files as images using Java2D.
It provides a lightweight way to convert PDF pages into BufferedImage objects suitable for
display in Swing applications or saving to image files.
Since 3.0.5 it uses `openpdf-core` (`com.github.librepdf:openpdf`) as its PDF parser.

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

## Migration to openpdf-core parser

Starting in **3.0.5**, `openpdf-renderer` depends on `openpdf-core`
(`com.github.librepdf:openpdf`) and exposes a new bridge entry point,
[`org.openpdf.renderer.core.OpenPdfCoreRenderer`](src/main/java/org/openpdf/renderer/core/OpenPdfCoreRenderer.java),
which uses `org.openpdf.text.pdf.PdfReader` to parse PDF documents.

The legacy in-tree parser is now `@Deprecated`:

- `org.openpdf.renderer.PDFFile`
- `org.openpdf.renderer.PDFPage`
- `org.openpdf.renderer.PDFParser`
- `org.openpdf.renderer.decode.PDFDecoder`

These classes will remain for at least one release to ease migration and are
not yet scheduled for removal.

### Why?

- **Consistency** &mdash; identical PDF interpretation between the core library
  and the renderer.
- **Maintenance** &mdash; one parser to fix bugs and apply security patches in.
- **Reliability** &mdash; leverages the battle-tested parsing engine in
  `openpdf-core`.
- **Focus** &mdash; rendering development can concentrate on the Java2D / Swing
  integration rather than re-implementing PDF parsing.

### Recommended new usage

```java
import org.openpdf.renderer.core.OpenPdfCoreRenderer;
import javax.imageio.ImageIO;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;

try (OpenPdfCoreRenderer renderer = new OpenPdfCoreRenderer(new File("document.pdf"))) {
    int pages          = renderer.getNumPages();
    Rectangle2D size   = renderer.getPageSize(1);     // 1-based
    int rotation       = renderer.getPageRotation(1);
    Map<String,String> metadata = renderer.getMetadata();
    String title       = renderer.getMetadata("Title");
    String pageText    = renderer.getTextFromPage(1);  // openpdf-core text extraction

    BufferedImage img  = renderer.renderPage(1, 150f); // 150 DPI, ARGB
    ImageIO.write(img, "png", new File("page1.png"));
}
```

### Status

`openpdf-renderer` now uses `openpdf-core` for **all** PDF parsing, including
the per-operator content-stream walking that drives Java2D rasterization. The
in-tree legacy parser (`PDFFile`, `PDFPage`, `PDFParser`,
`decode.PDFDecoder`) is no longer used by `OpenPdfCoreRenderer`.

| Capability | Backend |
|---|---|
| Open file / `Path` / `InputStream` / `byte[]` | `openpdf-core` (`PdfReader`) |
| Page count, page size, rotation | `openpdf-core` (`PdfReader`) |
| Document metadata (`getMetadata`) | `openpdf-core` (`PdfReader#getInfo`) |
| Page text extraction (`getTextFromPage`) | `openpdf-core` (`PdfTextExtractor`) |
| Decoded page content stream (`getPageContent`) | `openpdf-core` (`PdfReader#getPageContent`) |
| Content-stream operator listing (`getContentOperators`) | `openpdf-core` (`PdfContentParser`) |
| Page rasterization (`renderPage`) | `openpdf-core` (`PdfContentParser`) → Java2D via `OpenPdfCorePageRenderer` |

The Java2D rasterizer (`OpenPdfCorePageRenderer`) supports a broad subset of
PDF content-stream operators &mdash; sufficient for typical text + vector PDFs:

| Category | Operators |
|---|---|
| Graphics state | `q`, `Q`, `cm`, `gs` (alpha `CA`/`ca`, line styling) |
| Line style | `w`, `J`, `j`, `M`, `d`, `i` |
| Path construction | `m`, `l`, `c`, `v`, `y`, `re`, `h` |
| Path painting | `S`, `s`, `f`, `F`, `f*`, `B`, `B*`, `b`, `b*`, `n` |
| Clipping | `W`, `W*` |
| Colors (DeviceGray / DeviceRGB / DeviceCMYK) | `g`, `G`, `rg`, `RG`, `k`, `K`, `cs`, `CS`, `sc`, `SC`, `scn`, `SCN` |
| Text state | `BT`, `ET`, `Tf`, `Tc`, `Tw`, `TL`, `Tz`, `Td`, `TD`, `Tm`, `T*`, `Ts` |
| Text showing | `Tj`, `TJ`, `'`, `"` |
| XObjects | `Do` (Form XObjects recursively; Image XObjects: JPEG/`DCTDecode`, JPEG2000/`JPXDecode` where `ImageIO` supports it, and uncompressed / Flate-decoded 8-bit DeviceGray / DeviceRGB / DeviceCMYK) |
| Marked content / compatibility (no-op) | `BMC`, `BDC`, `EMC`, `MP`, `DP`, `BX`, `EX` |

Inline images (`BI`/`ID`/`EI`) are stripped from the content stream before
parsing &mdash; they aren't rendered, but they don't derail the rest of the
page either. Shading (`sh`), pattern / shading colors and type 3 font glyph
operators are silently ignored. Pages that rely heavily on those features
may render with missing content. Adding more operators is a localized change
in `OpenPdfCorePageRenderer`.

For pages that need features outside this supported subset and you want
pixel-perfect output today, the deprecated `PDFFile` / `PDFPage.getImage(...)`
API still works.

## Quick Start

### Basic PDF to Image Conversion

`org.openpdf.renderer.core.OpenPdfCoreRenderer` is the recommended entry point.
It uses `openpdf-core` (`PdfReader`) for parsing and the existing Java2D engine
for rasterization.

```java
import org.openpdf.renderer.core.OpenPdfCoreRenderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class PdfToImageExample {

    public static void main(String[] args) throws IOException {
        try (OpenPdfCoreRenderer renderer = new OpenPdfCoreRenderer(new File("document.pdf"))) {
            // Render the first page (1-based) at 150 DPI as TYPE_INT_ARGB.
            BufferedImage page = renderer.renderPage(1, 150f);
            ImageIO.write(page, "png", new File("output.png"));
            System.out.println("PDF page rendered successfully!");
        }
    }
}
```

### Document inspection

```java
try (OpenPdfCoreRenderer renderer = new OpenPdfCoreRenderer(new File("document.pdf"))) {
    int pages         = renderer.getNumPages();
    var size          = renderer.getPageSize(1);     // 1-based
    int rotation      = renderer.getPageRotation(1);
    var metadata      = renderer.getMetadata();      // unmodifiable Map<String,String>
    String title      = renderer.getMetadata("Title");
    String pageText   = renderer.getTextFromPage(1); // openpdf-core text extraction
    // List<String>, e.g. [q, BT, Tf, Td, Tj, ET, Q]
    var operators     = renderer.getContentOperators(1);
}
```

### Rendering directly to a `Graphics2D`

Avoid the intermediate `BufferedImage` when the caller already has a target
surface (Swing component, printer, SVG-backed graphics, ...):

```java
try (OpenPdfCoreRenderer renderer = new OpenPdfCoreRenderer(new File("document.pdf"))) {
    BufferedImage out = new BufferedImage(800, 1000, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = out.createGraphics();
    try {
        renderer.renderPage(1, g2, 800, 1000); // fit page to the box, preserve aspect
    } finally {
        g2.dispose();
    }
}
```

### Batch rendering

```java
try (OpenPdfCoreRenderer renderer = new OpenPdfCoreRenderer(new File("document.pdf"))) {
    List<BufferedImage> pages = renderer.renderAllPages(150f);
    for (int i = 0; i < pages.size(); i++) {
        ImageIO.write(pages.get(i), "png", new File("page-" + (i + 1) + ".png"));
    }
}
```

## Using the legacy `PDFFile` / `PDFPage` API (deprecated)

The pre-3.0.5 entry point still works but is now `@Deprecated`. New code should
prefer `OpenPdfCoreRenderer` (see above). The legacy API is kept for one or
more releases to ease migration, and may still be useful for pages that
exercise PDF features outside the supported subset of the new
`OpenPdfCorePageRenderer` (extended graphics state `gs`, CMYK / pattern /
shading colors, XObject `Do`, inline images, marked content, clipping, ...),
since the legacy renderer has broader operator coverage.

The legacy classes live in `org.openpdf.renderer`:

- `org.openpdf.renderer.PDFFile`
- `org.openpdf.renderer.PDFPage`
- `org.openpdf.renderer.PDFParser`
- `org.openpdf.renderer.decode.PDFDecoder`

> ⚠️ Expect deprecation warnings at compile time. These classes are scheduled
> to be removed in a future major release.

### Legacy: Basic PDF to Image Conversion

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

public class LegacyPdfToImageExample {

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

### Legacy: Rendering with Custom Resolution

```java
import org.openpdf.renderer.PDFFile;
import org.openpdf.renderer.PDFPage;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class LegacyHighResolutionRenderingExample {

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

### Legacy: Migration tips

| Legacy API | Replacement on `OpenPdfCoreRenderer` |
|---|---|
| `new PDFFile(ByteBuffer)` | `new OpenPdfCoreRenderer(File \| Path \| InputStream \| byte[])` |
| `pdf.getNumPages()` | `renderer.getNumPages()` |
| `pdf.getPage(n).getBBox()` | `renderer.getPageSize(n)` |
| `page.getImage(w, h, rect, ...)` | `renderer.renderPage(n, dpi)` |
| Manual DPI scaling via `getBBox()` | Pass `dpi` directly to `renderPage` |
| (no equivalent) | `renderer.getMetadata()`, `getTextFromPage(n)`, `getContentOperators(n)` |

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
- **Deprecated APIs**: `PDFFile`, `PDFPage`, `PDFParser`, and `org.openpdf.renderer.decode.PDFDecoder`
  are deprecated in favor of `org.openpdf.renderer.core.OpenPdfCoreRenderer`.
  Existing code continues to work for now &mdash; see the "Migration to openpdf-core parser" section above.

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
import org.openpdf.renderer.core.OpenPdfCoreRenderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

try (OpenPdfCoreRenderer renderer = new OpenPdfCoreRenderer(new File("document.pdf"))) {
    for (int i = 1; i <= renderer.getNumPages(); i++) {
        BufferedImage img = renderer.renderPage(i, 300f); // 300 DPI
        ImageIO.write(img, "png", new File("page-" + i + ".png"));
    }
}
```

The returned image is a `BufferedImage` of type `TYPE_INT_ARGB` whose dimensions
are the page size (in PDF user space units, i.e. 1/72 inch) scaled by
`dpi / 72`. Antialiasing for shapes and text is enabled by default.

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




