package com.lowagie.text.pdf;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * @author  SE_SUSTech, group: Lanrand
 * test issue #620
 * <p>This file is to test the code for fix the bugs in the issue #620
 * and there are 2 test cases
 */

public class PdfDocument620Test {

    /**
     * According to the issue, there will be different when you use setKeepTogether
     * and not use it in the leading.
     * <p>First, we will use three paragraph to write a document and see the pdf file.
     * We can see that after fix the issue, the leading will be same whenever you use
     * setKeepTogether or not.
     * When we see the pdf file, the two should be same.
     */

    @Test
    public void test1() throws IOException {
        File file1 = new File("E://t1output1.pdf");
        File file2 = new File("E://t1output2.pdf");
        FileOutputStream stream1 = new FileOutputStream(file1);
        FileOutputStream stream2 = new FileOutputStream(file2);
        Document document1 = new Document();
        Document document2 = new Document();
        try {
            PdfWriter.getInstance(document1, stream1);
            PdfWriter.getInstance(document2, stream2);
            document1.open();
            document2.open();

            /* First, we try to write a document with a paragraph using setKeepTogether and set true. */
            Paragraph par1 = new Paragraph("Zeile1", FontFactory.getFont(BaseFont.COURIER, 10));
            Paragraph par2 = new Paragraph("Zeile2", FontFactory.getFont(BaseFont.COURIER, 10));
            par2.setLeading(24);
            par2.setKeepTogether(true);
            Paragraph par3 = new Paragraph("Zeile3", FontFactory.getFont(BaseFont.COURIER, 10));
            par3.setLeading(12);

            document1.add(par1);
            document1.add(par2);
            document1.add(par3);

            /* Then, we try to write a document without a paragraph using setKeepTogether*/
            Paragraph par11 = new Paragraph("Zeile1", FontFactory.getFont(BaseFont.COURIER, 10));
            Paragraph par22 = new Paragraph("Zeile2", FontFactory.getFont(BaseFont.COURIER, 10));
            par22.setLeading(24);
            Paragraph par33 = new Paragraph("Zeile3", FontFactory.getFont(BaseFont.COURIER, 10));
            par33.setLeading(12);

            document2.add(par11);
            document2.add(par22);
            document2.add(par33);

        }
        catch (DocumentException de) {
            System.out.println(de.getMessage());
        }
        document1.close();
        document2.close();
    }

    @Test
    public void test2() throws IOException {
        File file1 = new File("E://t2output1.pdf");
        File file2 = new File("E://t2output2.pdf");
        FileOutputStream stream1 = new FileOutputStream(file1);
        FileOutputStream stream2 = new FileOutputStream(file2);
        Document document1 = new Document();
        Document document2 = new Document();
        try {
            PdfWriter.getInstance(document1, stream1);
            PdfWriter.getInstance(document2, stream2);
            document1.open();
            document2.open();

            /* First, we try to write a document with a long paragraph using setKeepTogether and
            set true. */
            String s = "sagdageafeddddddddddddddddddddddddddddddddddddddddddddddddddddddddd" +
                    "sdaffffffffffffffffffffffffffffffffffffffffffffff" +
                    "dsafffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                    "dsafffffffffffffffffffffffffffffff";
            Paragraph par1 = new Paragraph("Zeile1", FontFactory.getFont(BaseFont.COURIER, 10));
            Paragraph par2 = new Paragraph(s, FontFactory.getFont(BaseFont.COURIER, 10));
            par2.setLeading(24);
            par2.setKeepTogether(true);
            Paragraph par3 = new Paragraph("Zeile3", FontFactory.getFont(BaseFont.COURIER, 10));
            par3.setLeading(12);

            document1.add(par1);
            document1.add(par2);
            document1.add(par3);

            /* Then, we try to write a document without a paragraph using setKeepTogether*/
            Paragraph par11 = new Paragraph("Zeile1", FontFactory.getFont(BaseFont.COURIER, 10));
            Paragraph par22 = new Paragraph(s, FontFactory.getFont(BaseFont.COURIER, 10));
            par22.setLeading(24);
            Paragraph par33 = new Paragraph("Zeile3", FontFactory.getFont(BaseFont.COURIER, 10));
            par33.setLeading(12);

            document2.add(par11);
            document2.add(par22);
            document2.add(par33);

        }
        catch (DocumentException de) {
            System.out.println(de.getMessage());
        }
        document1.close();
        document2.close();
    }
}
