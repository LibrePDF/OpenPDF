/*
 * Author: alesky78 <alessandro.dottavio@gmail.com>
 */

package com.lowagie.text.pdf;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.draw.LineSeparator;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import org.junit.jupiter.api.Test;

public class ColumnTextSeparator {

    public static final float[][] COLUMNS = {{36, 36, 296, 806}, {299, 36, 559, 806}};
    private String filePath;

    @Test
    public void test_columnTextSeparator() throws Exception {
        filePath = System.getProperty("user.dir") + "/src/test/resources";

        File RESULT = new File(filePath + "/columnTextSeparator.pdf");
        // step 1
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter pdfWriter = PdfWriter.getInstance(document, baos);

        document.open();
        PdfContentByte wrote = pdfWriter.getDirectContent();

        ColumnText ct = new ColumnText(wrote);
        Phrase p = null;

        for (int i = 0; i < 3; i++) {
            p = new Phrase();
            p.add(new LineSeparator(0.3f, 100, null, Element.ALIGN_CENTER, -2));
            p.add("test");
            ct.addText(p);
            ct.addText(Chunk.NEWLINE);
        }

        ct.setAlignment(Element.ALIGN_JUSTIFIED);
        ct.setExtraParagraphSpace(6);
        ct.setLeading(0, 1.2f);
        ct.setFollowingIndent(27);
        int linesWritten = 0;
        int column = 0;
        int status = ColumnText.START_COLUMN;
        while (ColumnText.hasMoreText(status)) {
            ct.setSimpleColumn(COLUMNS[column][0], COLUMNS[column][1], COLUMNS[column][2], COLUMNS[column][3]);
            ct.setYLine(COLUMNS[column][3]);
            status = ct.go();
            linesWritten += ct.getLinesWritten();
            column = Math.abs(column - 1);
            if (column == 0) {
                document.newPage();
            }
        }

        document.close();

        FileOutputStream fos = new FileOutputStream(RESULT);
        fos.write(baos.toByteArray());
        fos.close();
    }


}
