package com.lowagie.text.pdf;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class TabTest {
    public static void main(String[] args) throws FileNotFoundException, DocumentException {
        Document document = new Document(PageSize.A4.rotate(), 10, 10, 10, 10);
        Document.compress = false;
        try {
            PdfWriter.getInstance(document,
                    new FileOutputStream("TabsTable.pdf"));
            document.open();
            document.add(new Chunk("data\ttable"));
        } catch (Exception de) {
            de.printStackTrace();
        }
        document.close();
    }
}