package com.lowagie.text.pdf;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.Utilities;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class PdfSignatureAppearanceTest {

    @Test
    void invisibleExternalSignature() throws DocumentException, IOException, NoSuchAlgorithmException {
        byte[] expectedDigestPreClose = null;
        byte[] expectedDigestClose = null;

        // These fields are provided to be able to generate the same content more than
        // once
        Calendar signDate = Calendar.getInstance();

        byte[] originalDocId = null;
        PdfObject overrideFileId = new PdfLiteral("<123><123>".getBytes());

        byte[] resultDocument = null;

        for (int i = 0; i < 10; i++) {
            try (InputStream is = getClass().getResourceAsStream("/EmptyPage.pdf");
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    PdfReader reader = new PdfReader(is)) {
                originalDocId = reader.getDocumentId();

                PdfStamper stp = PdfStamper.createSignature(reader, baos, '\0', null, true);
                stp.setEnforcedModificationDate(signDate);
                stp.setOverrideFileId(overrideFileId);

                PdfSignatureAppearance sap = stp.getSignatureAppearance();

                PdfDictionary dic = new PdfDictionary();
                dic.put(PdfName.FILTER, PdfName.ADOBE_PPKLITE);
                dic.put(PdfName.M, new PdfDate(signDate));

                sap.setCryptoDictionary(dic);
                sap.setSignDate(signDate);
                sap.setCertificationLevel(2);
                sap.setReason("Test");

                Map<PdfName, Integer> exc = new HashMap<>();
                exc.put(PdfName.CONTENTS, 10);
                sap.preClose(exc);

                byte[] result = Utilities.toByteArray(sap.getRangeStream());
                byte[] sha256 = getSHA256(result);
                if (expectedDigestPreClose == null) {
                    expectedDigestPreClose = sha256;
                } else {
                    assertArrayEquals(expectedDigestPreClose, sha256);
                }

                PdfDictionary update = new PdfDictionary();
                update.put(PdfName.CONTENTS, new PdfString("aaaa").setHexWriting(true));
                sap.close(update);

                byte[] resultClose = Utilities.toByteArray(sap.getRangeStream());
                byte[] sha256Close = getSHA256(resultClose);
                if (expectedDigestClose == null) {
                    expectedDigestClose = sha256Close;
                } else {
                    assertArrayEquals(expectedDigestClose, sha256Close);
                }

                resultDocument = baos.toByteArray();
            }

            try (InputStream resultIS = new ByteArrayInputStream(
                    resultDocument); PdfReader resultReader = new PdfReader(resultIS)) {
                byte[] documentId = resultReader.getDocumentId();
                assertNotNull(documentId);
                assertArrayEquals(originalDocId, documentId);

                PdfArray idArray = resultReader.getTrailer().getAsArray(PdfName.ID);
                assertEquals(2, idArray.size());
                assertArrayEquals(documentId,
                        com.lowagie.text.DocWriter.getISOBytes(idArray.getPdfObject(0).toString()));
                assertEquals("123", idArray.getPdfObject(1).toString());
            }
        }
    }

    @Test
    void visibleExternalSignature() throws DocumentException, IOException, NoSuchAlgorithmException {
        byte[] expectedDigestPreClose = null;
        byte[] expectedDigestClose = null;

        Calendar signDate = Calendar.getInstance();
        Image sigImg = Image.getInstance(getClass().getClassLoader().getResource("GitHub-Mark-32px.png"));
        PdfReader sigPdf = new PdfReader(getClass().getClassLoader().getResource("SimulatedBoldAndStrokeWidth.pdf"));

        byte[] originalDocId = null;
        PdfObject overrideFileId = new PdfLiteral("<123><123>".getBytes());

        byte[] resultDocument = null;

        for (int i = 0; i < 10; i++) {
            try (InputStream is = getClass().getResourceAsStream("/EmptyPage.pdf");
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    PdfReader reader = new PdfReader(is)) {
                originalDocId = reader.getDocumentId();

                PdfStamper stp = PdfStamper.createSignature(reader, baos, '\0', null, true);
                stp.setEnforcedModificationDate(signDate);
                stp.setOverrideFileId(overrideFileId);

                PdfSignatureAppearance sap = stp.getSignatureAppearance();

                PdfDictionary dic = new PdfDictionary();
                dic.put(PdfName.FILTER, PdfName.ADOBE_PPKLITE);
                dic.put(PdfName.M, new PdfDate(signDate));

                sap.setCryptoDictionary(dic);
                sap.setSignDate(signDate);
                sap.setVisibleSignature(new Rectangle(100, 100), 1);
                sap.setLayer2Text("Hello world");
                if (i < 5) {
                    // Test image signature in the first half of the tests
                    sap.setSignatureGraphic(sigImg);
                } else {
                    // Test PDF signature in the second half of the tests
                    if (i == 5) {
                        expectedDigestPreClose = null;
                        expectedDigestClose = null;
                    }
                    sap.setSignaturePDF(sigPdf, 1);
                }

                Map<PdfName, Integer> exc = new HashMap<>();
                exc.put(PdfName.CONTENTS, 10);
                sap.preClose(exc);

                byte[] result = Utilities.toByteArray(sap.getRangeStream());
                byte[] sha256 = getSHA256(result);
                if (expectedDigestPreClose == null) {
                    expectedDigestPreClose = sha256;
                } else {
                    assertArrayEquals(expectedDigestPreClose, sha256);
                }

                PdfDictionary update = new PdfDictionary();
                update.put(PdfName.CONTENTS, new PdfString("aaaa").setHexWriting(true));
                sap.close(update);

                byte[] resultClose = Utilities.toByteArray(sap.getRangeStream());
                byte[] sha256Close = getSHA256(resultClose);
                if (expectedDigestClose == null) {
                    expectedDigestClose = sha256Close;
                } else {
                    assertArrayEquals(expectedDigestClose, sha256Close);
                }

                resultDocument = baos.toByteArray();
            }

            try (InputStream resultIS = new ByteArrayInputStream(
                    resultDocument); PdfReader resultReader = new PdfReader(resultIS)) {
                byte[] documentId = resultReader.getDocumentId();
                assertNotNull(documentId);
                assertArrayEquals(originalDocId, documentId);

                PdfArray idArray = resultReader.getTrailer().getAsArray(PdfName.ID);
                assertEquals(2, idArray.size());
                assertArrayEquals(documentId,
                        com.lowagie.text.DocWriter.getISOBytes(idArray.getPdfObject(0).toString()));
                assertEquals("123", idArray.getPdfObject(1).toString());
            }
        }
    }

    private byte[] getSHA256(byte[] bytes) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return md.digest(bytes);
    }

}
