package com.lowagie.text.pdf;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.lowagie.text.Document;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PrinterJob;
import java.io.ByteArrayOutputStream;
import org.junit.jupiter.api.Test;

class PdfPrinterGraphics2DTest {

    @Test
    void testCreate() {
        try (Document document = new Document()) {
            PdfWriter writer = PdfWriter.getInstance(document, new ByteArrayOutputStream());
            document.open();
            Graphics2D graphics1 = writer.getDirectContent().createPrinterGraphics(10, 10, PrinterJob.getPrinterJob());
            Graphics graphics2 = graphics1.create();
            assertEquals(PdfPrinterGraphics2D.class, graphics1.getClass());
            assertEquals(PdfPrinterGraphics2D.class, graphics2.getClass());
            graphics2.dispose();
            graphics1.dispose();
        }
    }
}
