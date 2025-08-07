package org.openpdf.examples.footer;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import org.openpdf.text.Document;
import org.openpdf.text.Element;
import org.openpdf.text.HeaderFooter;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.pdf.PdfWriter;

public class Footer {

    /**
     * Create a document with 2 empty pages and custom numbered footer without before part.
     *
     * @param args
     */

    public static void main(String[] args) {
        Document document = new Document(PageSize.A4, 10f, 10f, 10f, 10f);

        try {
            PdfWriter.getInstance(document,
                    new FileOutputStream("footer.pdf"));
            HeaderFooter footer = new HeaderFooter(true, new Phrase(" page"));
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setBorderWidthBottom(0);
            document.setFooter(footer);

            document.open();
            document.add(new Paragraph(" "));
            document.newPage();
            document.add(new Paragraph(" "));
            document.newPage();
            document.add(new Paragraph(" "));
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
        }
        document.close();
    }
}
