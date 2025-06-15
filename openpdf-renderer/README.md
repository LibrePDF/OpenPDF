OpenPDF-renderer
============

OpenPDF-renderer is a Java library for rendering PDF files.


Examples:
========
* [Render PDF as image](https://github.com/LibrePDF/OpenPDF/blob/master/openpdf-renderer/src/test/java/openpdf/renderer/ImageRendererTest.java)
* [Render PDF in Swing GUI](https://github.com/LibrePDF/OpenPDF/blob/master/openpdf-renderer/src/test/java/openpdf/renderer/PdfRendererGui.java)


Note
=====
* Package names renamed from com.sun.pdfview to org.openpdf.renderer.

Usage
=====

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
