package com.lowagie.text.pdf;

import com.lowagie.text.Font;
import org.junit.jupiter.api.Test;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import java.awt.*;
import java.util.ArrayList;

public class DifferentiatingWords571Test {
    /**
     * The following creates a new document.
     * From there, fonts are initialized and placed in fontArrayList.
     * For testing purposes, add new text  into textArrayList
     *
     * Using the paragraph constructor created for this issue, use both fontArrayList and textArrayList
     * parameters.This will return the matching font for the string.
     * From there a user may choose to align using column text, and finish by adding the paragraph into the doc.
     */
    @Test
    void testFontsInDiffWords() throws IOException {

        final Document document = new Document();
        final PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("./src/test/resources/TestDiffFonts.pdf"));

        document.open();
        LayoutProcessor.enable(java.awt.Font.LAYOUT_RIGHT_TO_LEFT);

        Font timesNormal = new Font(Font.TIMES_ROMAN, Font.DEFAULTSIZE, Font.NORMAL);
        Font customFont = new Font(Font.COURIER, 12, Font.BOLD, Color.BLUE);
        Font customFont2 = new Font(Font.COURIER, 19, Font.BOLD, Color.BLUE);
        ArrayList<Font> fontArrayList = new ArrayList<>();

        fontArrayList.add(timesNormal);
        fontArrayList.add(customFont);
        fontArrayList.add(customFont2);
        ArrayList<String> textArrayList = new ArrayList<>();
        textArrayList.add("Testing string1");
        textArrayList.add("Testing string2");
        textArrayList.add("Testing string3");
        textArrayList.add("Testing string4");

        Paragraph paragraph = new Paragraph(textArrayList,fontArrayList);
        paragraph.setAlignment(Element.ALIGN_JUSTIFIED);

        PdfContentByte canvas = writer.getDirectContent();
        ColumnText ct = new ColumnText(canvas);
        ct.setSimpleColumn(100, 500, 200, 750);
        ct.addElement(paragraph); // add the paragraph as element
        ct.go();

        document.add(paragraph);
        document.close();


    }


}
