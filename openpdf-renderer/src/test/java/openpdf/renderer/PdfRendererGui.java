package openpdf.renderer;

import javax.swing.JFrame;
import java.io.File;
import java.net.URL;

/**
 * Render PDF file as a Swing GUI.
 */
public class PdfRendererGui {

    public static void main(String[] args) {
        int pageIndex = 1;

        // Load the PDF from classpath (src/test/resources)
        URL pdfUrl = PdfRendererGui.class.getClassLoader().getResource("HelloWorldMeta.pdf");
        if (pdfUrl == null) {
            System.err.println("[PdfRendererGui] PDF not found in classpath: HelloWorldMeta.pdf");
            return;
        }

        String fileName = new File(pdfUrl.getFile()).getAbsolutePath();
        System.out.println("[PdfRendererGui] Loaded PDF: " + fileName);

        JFrame myFrame = new JFrame("Openpdf-renderer");
        myFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // ← Important!
        PDFDisplay pdfDisplay = new PDFDisplay(fileName, pageIndex);
        myFrame.add(pdfDisplay);
        myFrame.setSize(700, 1000);
        myFrame.setVisible(true);
    }
}
