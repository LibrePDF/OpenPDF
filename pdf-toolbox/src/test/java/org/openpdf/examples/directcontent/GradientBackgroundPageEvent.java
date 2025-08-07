package org.openpdf.examples.directcontent;

import java.awt.Color;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.openpdf.text.Document;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.PdfContentByte;
import org.openpdf.text.pdf.PdfPageEventHelper;
import org.openpdf.text.pdf.PdfShading;
import org.openpdf.text.pdf.PdfWriter;

public class GradientBackgroundPageEvent {

    public static void main(String[] args) throws IOException {
        try (Document document = new Document()) {
            PdfWriter writer = PdfWriter.getInstance(document,
                    Files.newOutputStream(Path.of("GradientBackgroundPageEvent.pdf")));
            writer.setPageEvent(new GradientBackground());
            document.open();
            for (int i = 0; i < 20; i++) {
                document.add(new Paragraph("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam "
                        + "nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. "
                        + "At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea "
                        + "takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur "
                        + "sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam "
                        + "erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita "
                        + "kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet."));
            }
        }
    }

    static class GradientBackground extends PdfPageEventHelper {

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            Rectangle pageSize = document.getPageSize();
            PdfShading axial = PdfShading.simpleAxial(writer,
                    pageSize.getLeft(pageSize.getWidth() / 10), pageSize.getBottom(),
                    pageSize.getRight(pageSize.getWidth() / 10), pageSize.getBottom(),
                    new Color(50, 55, 190), new Color(0, 180, 250), true, true);
            PdfContentByte canvas = writer.getDirectContentUnder();
            canvas.paintShading(axial);
        }
    }

}