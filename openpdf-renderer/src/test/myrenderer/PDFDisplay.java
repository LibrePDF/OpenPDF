package test.myrenderer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.nio.ByteBuffer;

import javax.swing.JComponent;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;
import com.sun.pdfview.PDFRenderer;

public class PDFDisplay extends JComponent {
	private static final long serialVersionUID = 1L;
	// byte array containing the PDF file content
	private byte[] bytes = null;
	private String fileName;
	private int pageIndex;
	
	public PDFDisplay(String fileName, int pageIndex) {
		super();
		this.fileName = fileName;
		this.pageIndex = pageIndex;
		readFile();
	}

	private void readFile() {
		try(BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(fileName));){
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			int b;
			while ((b = inputStream.read()) != -1) {
				outputStream.write(b);
			}
			this.bytes = outputStream.toByteArray();	
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void paintComponent(Graphics g) {
		try {
			PDFFile pdfFile = new PDFFile(ByteBuffer.wrap(this.bytes));		
			PDFPage page = pdfFile.getPage(this.pageIndex);
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
				page.waitForFinish();
			}
			catch (InterruptedException e) {
				// some exception handling
			}
			renderer.run();
		}catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
