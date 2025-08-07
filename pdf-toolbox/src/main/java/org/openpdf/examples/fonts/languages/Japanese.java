package org.openpdf.examples.fonts.languages;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.openpdf.text.Chunk;
import org.openpdf.text.Document;
import org.openpdf.text.Font;
import org.openpdf.text.pdf.BaseFont;
import org.openpdf.text.pdf.PdfWriter;

public class Japanese {

    public static void main(String[] args) throws IOException {
        // step 0: prepare font with chinese symbols
        BaseFont baseFont = BaseFont.createFont(
                Japanese.class.getClassLoader().getResource("fonts/GenShinGothic-Normal.ttf").getFile(),
                BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        Font font = new Font(baseFont, 12, Font.NORMAL);

        // step 1: Prepare document for japanese text
        Document document = new Document();
        ByteArrayOutputStream pdfOutput = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, pdfOutput);
        document.open();

        // step 2: we add content to the document
        // http://en.glyphwiki.org/wiki/u20bb7
        document.add(new Chunk("\uD842\uDFB7", font));

        // step 3: we close the document
        document.close();

        Files.write(Paths.get(Japanese.class.getSimpleName() + ".pdf"), pdfOutput.toByteArray());
    }
}
