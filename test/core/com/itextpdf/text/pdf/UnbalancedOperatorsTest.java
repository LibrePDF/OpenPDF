package com.itextpdf.text.pdf;

import java.io.ByteArrayOutputStream;

import org.junit.Test;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.exceptions.IllegalPdfSyntaxException;

public class UnbalancedOperatorsTest {

    private Document document;
    private PdfWriter writer;
    
    private void initializeDocument() throws DocumentException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        document = new Document();
        writer = PdfWriter.getInstance(document, baos);
        document.open();        
    }
    
    @Test
    public void testBasicDocument() throws Exception {
        initializeDocument();
        writer.getDirectContent().saveState();
        document.add(new Paragraph("Hello World"));
        writer.getDirectContent().restoreState();
        document.close();
    }

    @Test
    public void testNewPage() throws Exception {
        initializeDocument();
        writer.getDirectContent().saveState();
        document.add(new Paragraph("Hello World"));
        writer.getDirectContent().restoreState();
        document.newPage();
        document.add(new Paragraph("Hello World"));
        document.close();
    }
    
    @Test(expected=IllegalPdfSyntaxException.class)
    public void testUnbalancedRestoreState() throws Exception {
        initializeDocument();
        document.add(new Paragraph("Hello World"));
        writer.getDirectContent().restoreState();
        document.close();
    }

    @Test(expected=IllegalPdfSyntaxException.class)
    public void testUnbalancedSaveStateOnClose() throws Exception {
        initializeDocument();
        writer.getDirectContent().saveState();
        document.add(new Paragraph("Hello World"));
        document.close();
    }

    @Test(expected=IllegalPdfSyntaxException.class)
    public void testUnbalancedSaveStateOnNewPage() throws Exception {
        initializeDocument();
        writer.getDirectContent().saveState();
        document.add(new Paragraph("Hello World"));
        document.newPage();
        document.add(new Paragraph("Hello World"));
        document.close();
    }

}
