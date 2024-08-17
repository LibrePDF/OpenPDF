openpdf-pdfrenderer
===================

PDF to Image renderer for OpenPDF. 

Because every fine PDF library should be able to create a PNG image from a PDF page.

License: GNU Lesser General Public License

---

This is a fork of https://github.com/katjas/PDFrenderer which is based on [pdf-renderer](http://java.net/projects/pdf-renderer) (covered by the LGPL-2.1 license) for improvement purposes.

The principal objective of the fork is to improve the original PDF renderer. The original version is able to handle most of the PDF 1.4 features, but has several bugs and missing functionality.



To do:
------
* some colours are displayed incorrect, there seem to be open issues regarding colour space handling
* some fonts can't be rendered and are replaced with built in fonts 
* embedded Type0 font with a CIDType0 font is not supported correctly. Currently there is a hack in the code to fall back to the built in fonts in this case.
* try to improve support of auto adjust stroke and overprint mode - the data is read but not really handled correctly.

Done:
-----
* support for widget annotation containing digital signature
* support function type 4 rendering (subset of the PostScript language, specification taken from http://www.adobe.com/devnet/acrobat/pdfs/adobe_supplement_iso32000.pdf)
* support link annotations for being able to render links
* rough support of stamp and freetext annotations
* handle alternate colour spaces (colour space plus a function to be applied on the colour values)
* fixes transparency issues / transparent masked images (even though transparency is still not completely supported)
* corrected handling of overlapping shapes
* better support Type0 fonts that use embedded CID fonts
* jbig2 image format decoded with (improved) "jpedal" API
* DeviceCMY / DeviceRGB colour spaces are working now, but some PDFs are still displayed in wrong format.
* Improved reading of CMYK images. Some colours are still displayed wrong. (using the ch.randelshofer.media.jpeg.JPEGImageIO API)
* Improved run length decoding (corrected reading of buffer) 
* fixed compression issues
* fixed size of outline fonts 
* fixed several exceptions
* Fixed various font encoding problems (Flex in Type 1, wrong stemhints in Type 1C and inverted presentation of Type 3)
* fixed rotation of text (http://java.net/jira/browse/PDF_RENDERER-91)
* JPEG decoding with imageio
* Work-around lack of YCCK decoding support in standard JRE image readers and thus allow CMYK jpeg images without using 3rd party image readers (e.g., JAI)
* Employ local TTF files if available instead of using the built-ins as substitutes. Scanning of available TTFs will take some time on the first request for an unavailable TTF. This behaviour can be disabled by setting the system property PDFRenderer.avoidExternalTtf to true. The PDFRenderer.fontSearchPath system property can be used to alter the search path, though Windows and Mac OS X defaults should hopefully be sensible. 
* Added TIFF Type 2 Predictor for decoding
* use built in font as workaround for MMType1 fonts instead of throwing an exception
* introduced configuration options for improving the memory usage when rendering PDFs with large (e.g. scanned) images
* improved parsing of SMask images
* modified parsing of paths, some closures were missing
* added some debugging
* Add option to inject exception handling - e.g. for redirecting the stack trace to a log file
* Add SymbolSetEncoding

Usage / Example
-------

// example class for displaying a PDF file
```java
public class PDFDisplay extends JComponent{

	// byte array containing the PDF file content
	private byte[] bytes = null;
	
	
	// some more code
	
	@Override
	public void paintComponent(Graphics g) {
		int pageindex = 1;
		PDFFile pdfFile = new PDFFile(ByteBuffer.wrap(this.bytes));		
		PDFPage page = pdfFile.getPage(pageIndex);
		Paper paper = new Paper();
		int formatOrientation = page.getAspectRatio() > 1 ? PageFormat.LANDSCAPE
							: PageFormat.PORTRAIT;
		if(formatOrientation == PageFormat.LANDSCAPE) {
			paper.setSize(page.getHeight(), page.getWidth());
		}else {
			paper.setSize(page.getWidth(), page.getHeight());
		}				
		PageFormat pageFormat = new PageFormat();
		pageFormat.setPaper(paper);
		pageFormat.setOrientation(formatOrientation);

		Graphics2D g2d = (Graphics2D)g.create();
		Rectangle imgbounds = new Rectangle(0, 0, (int)pageFormat.getWidth(),
						(int)pageFormat.getHeight());
		PDFRenderer renderer = new PDFRenderer(page, g2d, imgbounds, null, Color.WHITE);
		try {
			this.page.waitForFinish();
		}
		catch (InterruptedException e) {
			// some exception handling
		}
		renderer.run();
	}
}
```
