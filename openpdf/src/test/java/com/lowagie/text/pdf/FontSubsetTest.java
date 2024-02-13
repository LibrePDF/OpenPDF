package com.lowagie.text.pdf;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.crypto.prng.FixedSecureRandom;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FontSubsetTest {

    /*
     * See : https://github.com/LibrePDF/OpenPDF/issues/623
     */
    @Test
    public void createSubsetPrefixTest() throws Exception {
        BaseFont font = BaseFont.createFont("LiberationSerif-Regular.ttf", BaseFont.IDENTITY_H,
                BaseFont.EMBEDDED,true, getFontByte("fonts/liberation-serif/LiberationSerif-Regular.ttf"), null);
        assertNotEquals(font.createSubsetPrefix(), font.createSubsetPrefix());

        byte[] baseSeed = new SecureRandom().generateSeed(512);
        // init deterministic SecureRandom with a custom base seed
        SecureRandom secureRandom = new FixedSecureRandom(baseSeed);
        font.setSecureRandom(secureRandom);
        assertNotEquals(font.createSubsetPrefix(), font.createSubsetPrefix()); // still different, as FixedSecureRandom generates a new random on each step

        SecureRandom secureRandomOne = new FixedSecureRandom(baseSeed);
        font.setSecureRandom(secureRandomOne);

        String subsetPrefixOne = font.createSubsetPrefix();

        // re-init FixedSecureRandom for deterministic generation
        SecureRandom secureRandomTwo = new FixedSecureRandom(baseSeed);
        font.setSecureRandom(secureRandomTwo);

        String subsetPrefixTwo = font.createSubsetPrefix();
        assertEquals(subsetPrefixOne, subsetPrefixTwo); // the desired deterministic behavior
    }

    private byte[] getFontByte(String fileName) throws IOException {
        try (InputStream stream = BaseFont.getResourceStream(fileName, null)) {
            return IOUtils.toByteArray(stream);
        }
    }

    /*
     * This test is to ensure creation of CIDSet dictionary according to the includeCidSet flag
     */
    @Test
    public void includeCidSetTest() throws Exception {
        checkCidSetPresence(true);
        checkCidSetPresence(false);
    }

    private void checkCidSetPresence(boolean includeCidSet) throws Exception {
        byte[] documentBytes;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, baos);
            document.open();

            BaseFont font = BaseFont.createFont("LiberationSerif-Regular.ttf", BaseFont.IDENTITY_H,
                    BaseFont.EMBEDDED,true, getFontByte("fonts/liberation-serif/LiberationSerif-Regular.ttf"), null);
            font.setIncludeCidSet(includeCidSet);
            String text = "This is the test string.";
            document.add(new Paragraph(text, new Font(font, 12)));
            document.close();

            documentBytes = baos.toByteArray();
        }

        boolean fontFound = false;
        try (PdfReader reader = new PdfReader(documentBytes)) {
            for (int k = 1; k < reader.getXrefSize(); ++k) {
                PdfObject obj = reader.getPdfObjectRelease(k);
                if (obj == null || !obj.isDictionary())
                    continue;
                PdfDictionary dic = (PdfDictionary) obj;
                PdfObject type = PdfReader.getPdfObjectRelease(dic.get(PdfName.TYPE));
                if (type == null || !type.isName())
                    continue;
                PdfDictionary fd = dic.getAsDict(PdfName.FONTDESCRIPTOR);
                if (PdfName.FONT.equals(type) && fd != null) {
                    PdfIndirectReference cidset = fd.getAsIndirectObject(PdfName.CIDSET);
                    assertEquals(includeCidSet, cidset != null);
                    fontFound = true;
                    break;
                }
            }
        }
        assertTrue(fontFound);
    }

}
