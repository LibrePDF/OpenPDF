package com.lowagie.text.pdf;

import com.lowagie.text.*;

import java.io.FileOutputStream;
import java.io.IOException;

public class IssueTest613 {

    public static void main(String[] args) {

        // Register the font which we want to supports nirmalaui
        FontFactory.register("C:\\Windows\\Fonts\\NIRMALA.TTF");
        Document document = new Document();
        try {
            PdfWriter.getInstance(document,
                    new FileOutputStream("D:\\workspace\\TMP\\out\\HelloWorld.pdf"));
            document.open();
            document.add(new Chunk(
                    "नमस्ते",
                    FontFactory.getFont("nirmalaui-bold", "Identity-H", false, 10F, 0, null)));
        } catch (IOException ioexcep) {
            System.err.println(ioexcep.getMessage());
        } catch (DocumentException docexcep) {
            System.err.println(docexcep.getMessage());
        }finally {
            document.close();
        }
    }
}
