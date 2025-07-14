package com.lowagie.text.pdf;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lowagie.text.DocumentException;
import com.lowagie.text.Utilities;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class PdfSignatureRangeTest {

    private static void checkSignature(byte[] pdf) throws IOException {
        byte[] produced = fakeSignature(pdf);
        try (PdfReader r = new PdfReader(produced)) {
            assertTrue(r.getAcroFields().signatureCoversWholeDocument("Signature1"), "file size: " + pdf.length);
        }
    }

    private static byte[] fakeSignature(byte[] pdf) throws IOException {
        try (PdfReader reader = new PdfReader(pdf); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfStamper stp = PdfStamper.createSignature(reader, baos, '\0', null, true);

            PdfSignatureAppearance sap = stp.getSignatureAppearance();

            PdfDictionary dic = new PdfDictionary();
            dic.put(PdfName.FILTER, PdfName.ADOBE_PPKLITE);

            sap.setCryptoDictionary(dic);
            sap.setCertificationLevel(2);
            sap.setReason("Test");

            Map<PdfName, Integer> exc = new HashMap<>();
            exc.put(PdfName.CONTENTS, 10);
            sap.preClose(exc);

            PdfDictionary update = new PdfDictionary();
            update.put(PdfName.CONTENTS, new PdfString("aaaa").setHexWriting(true));
            sap.close(update);

            return baos.toByteArray();
        }
    }

    private static byte[] enlarge(byte[] pdf, int size) {
        int trailerSize = 23;
        byte[] out = new byte[size];
        Arrays.fill(out, (byte) '\n'); // fill with newlines
        System.arraycopy(pdf, 0, out, 0, pdf.length - trailerSize);
        System.arraycopy(pdf, pdf.length - trailerSize, out, out.length - trailerSize, trailerSize);
        return out;
    }

    @Test
    void bigFileSignature() throws DocumentException, IOException {
        byte[] pdf = Utilities.toByteArray(getClass().getResourceAsStream("/EmptyPage.pdf"));
        checkSignature(pdf);
        checkSignature(enlarge(pdf, 100001));
        checkSignature(enlarge(pdf, 16777217)); // must be odd, as only the last bit is lost
    }

    @Test
    void objectXrefDocumentSignature() throws DocumentException, IOException {
        byte[] pdf = Utilities.toByteArray(getClass().getResourceAsStream("/objectXref.pdf"));
        checkSignature(pdf);
        checkSignature(enlarge(pdf, 100001));
        checkSignature(enlarge(pdf, 16777217)); // must be odd, as only the last bit is lost
    }

}
