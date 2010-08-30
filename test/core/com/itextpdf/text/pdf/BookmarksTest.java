package com.itextpdf.text.pdf;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;

public class BookmarksTest {

    private static final String TITLE = "1.\u00a0Paragraph 1";

    @Test
    public void testNoBreakSpace() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document();

        PdfWriter writer = PdfWriter.getInstance(document, baos);

        document.open();
        writer.setPageEvent(new PdfPageEventHelper() {
            public void onParagraph(PdfWriter writer, Document document, float position) {
                PdfContentByte cb = writer.getDirectContent();
                PdfDestination destination = new PdfDestination(PdfDestination.FITH, position);
                new PdfOutline(cb.getRootOutline(), destination, TITLE);
            }            
        });
        document.add(new Paragraph("Hello World"));
        document.close();
        
        // read bookmark back
        PdfReader r = new PdfReader(baos.toByteArray());

        List bookmarks = SimpleBookmark.getBookmark(r);
        assertEquals("bookmark size", 1, bookmarks.size()); 
        HashMap<String, Object> b = (HashMap<String, Object>)bookmarks.get(0);
        String title = (String) b.get("Title");
        assertEquals("bookmark title", TITLE, title); 
    }

}