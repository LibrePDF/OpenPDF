package com.lowagie.text.pdf;

import com.lowagie.text.Document;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class BarcodeMacroPDF417Test {

    private static final Path OUTPUT_DIR = Paths.get(".", "target", "test-classes");
    private static final Path COMP_DIR = Paths.get(".", "src", "test", "resources");
    private static final String FILENAME = "barcode_macro_pdf_417.pdf";
    private static final int SEGMENT_COUNT = 2;

    @BeforeAll
    static void setup() {
        OUTPUT_DIR.toFile().mkdirs();
        COMP_DIR.toFile().mkdirs();
    }

    @Test
    public void testBarcode() throws IOException {
        generatePdf();
        Assertions.assertTrue(comparePdf());
    }

    private void generatePdf() throws IOException {
        Document document = new Document();
        OutputStream out = new FileOutputStream(OUTPUT_DIR.resolve(FILENAME).toFile());
        PdfWriter.getInstance(document, out);
        document.open();

        String[] testTexts = {"Test PDF417 Segment 0", "Test PDF417 Segment 1"};

        document.add(new Paragraph(testTexts[0]));
        document.add(getBarcode(testTexts[0], 0));

        for (int i = 0; i < 10; i++) {
            document.add(new Paragraph(String.format("Test paragraph #%d", i)));
        }

        document.add(new Paragraph(testTexts[1]));
        document.add(getBarcode(testTexts[1], 1));

        document.close();
    }

    private boolean comparePdf() throws IOException {
        PdfReader outReader = new PdfReader(OUTPUT_DIR.resolve(FILENAME).toString());
        PdfReader cmpReader = new PdfReader(COMP_DIR.resolve(FILENAME).toString());
        PdfDictionary outDict = outReader.getPageN(1);
        PdfDictionary cmpDict = cmpReader.getPageN(1);
        if (!outDict.getKeys().equals(cmpDict.getKeys())) {
            return false;
        }
        for (PdfName name : outDict.getKeys()) {
            if (!outDict.get(name).toString().equals(cmpDict.get(name).toString())) {
                return false;
            }
        }
        return true;
    }

    private Image getBarcode(String text, int segId) {
        BarcodePDF417 bp = new BarcodePDF417();
        bp.setOptions(BarcodePDF417.PDF417_USE_MACRO);
        bp.setMacroFileId("12");
        bp.setMacroSegmentCount(SEGMENT_COUNT);
        bp.setMacroSegmentId(segId);
        bp.setText(text);
        return bp.getImage();
    }
}
