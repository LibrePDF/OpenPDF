/**
 * The restoreStateTest program implements an application that
 * reproduce the issue that make the red color to wanted blue color of box
 *
 * @author  Jiachun
 * @version 1.0
 * @since   2021-11-21
 */

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.*;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class restoreStateTest {
    public static void main(String[] args) throws IOException {
        try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream("/tmp/output.pdf"))) {
            Document document = new Document();
            PdfWriter writer = PdfWriter.getInstance(document, outputStream);
            document.open();
            PdfContentByte cb = writer.getDirectContent();

            document.setPageSize(PageSize.A4);
            document.newPage();

            //draw first rectangle
            cb.saveState();
            cb.setColorFill(new Color(255, 0, 0, 50));   //alpha value = 50
            cb.setColorStroke(Color.RED);
            cb.rectangle(200, 400, 30, 30);
            cb.fillStroke();
            cb.restoreState();

            //draw second rectangle
            cb.saveState();
            cb.setColorFill(new Color(0, 0, 255, 50));   //same alpha value (50)
            cb.setColorStroke(Color.BLUE);
            cb.rectangle(300, 400, 30, 30);
            cb.fillStroke();
            cb.restoreState();

            document.close();
        }
    }
}
