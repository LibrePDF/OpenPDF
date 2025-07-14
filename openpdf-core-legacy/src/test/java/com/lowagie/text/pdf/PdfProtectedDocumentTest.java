package com.lowagie.text.pdf;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.lowagie.text.DocWriter;
import com.lowagie.text.Utilities;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class PdfProtectedDocumentTest {

    @Test
    void signPasswordProtected() throws Exception {
        Calendar signDate = Calendar.getInstance();

        byte[] documentBytes;
        byte[] expectedDigestPreClose = null;
        byte[] expectedDigestClose = null;

        byte[] originalDocId = null;
        byte[] changingId = null;

        // Sign and compare the generated range
        for (int i = 0; i < 10; i++) {
            try (InputStream is = getClass().getResourceAsStream("/open_protected.pdf");
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    PdfReader reader = new PdfReader(is, new byte[]{' '})) {
                originalDocId = reader.getDocumentId();

                PdfStamper stp = PdfStamper.createSignature(reader, baos, '\0', null, true);
                stp.setEnforcedModificationDate(signDate);

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
                    assertFalse(Arrays.equals(expectedDigestPreClose, sha256));
                }

                PdfDictionary update = new PdfDictionary();
                update.put(PdfName.CONTENTS, new PdfString("aaaa").setHexWriting(true));
                sap.close(update);

                byte[] resultClose = Utilities.toByteArray(sap.getRangeStream());
                assertArrayEquals(result, resultClose);

                byte[] sha256Close = getSHA256(resultClose);
                if (expectedDigestClose == null) {
                    expectedDigestClose = sha256Close;
                } else {
                    assertFalse(Arrays.equals(expectedDigestClose, sha256Close));
                }

                documentBytes = baos.toByteArray();
            }

            // Ensure document is readable
            try (InputStream is = new ByteArrayInputStream(documentBytes);
                    PdfReader reader = new PdfReader(is, new byte[]{' '})) {
                assertNotNull(reader);

                byte[] documentId = reader.getDocumentId();
                assertNotNull(documentId);
                assertArrayEquals(originalDocId, documentId);

                PdfArray idArray = reader.getTrailer().getAsArray(PdfName.ID);
                assertEquals(2, idArray.size());
                assertArrayEquals(documentId,
                        com.lowagie.text.DocWriter.getISOBytes(idArray.getPdfObject(0).toString()));

                byte[] currentChangingId = DocWriter.getISOBytes(idArray.getPdfObject(1).toString());
                if (changingId != null) {
                    assertFalse(Arrays.equals(changingId, currentChangingId));
                }
                changingId = currentChangingId;
            }
        }

    }

    @Test
    void signPasswordProtectedOverrideFileId() throws Exception {
        Calendar signDate = Calendar.getInstance();

        // override with custom FileID to ensure deterministic behaviour
        byte[] originalDocId = null;
        PdfObject overrideFileId = new PdfLiteral("<123><123>".getBytes());

        byte[] documentBytes;
        byte[] expectedDigestPreClose = null;
        byte[] expectedDigestClose = null;

        // Sign and compare the generated range
        for (int i = 0; i < 10; i++) {
            try (InputStream is = getClass().getResourceAsStream("/open_protected.pdf");
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    PdfReader reader = new PdfReader(is, new byte[]{' '})) {
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
                assertArrayEquals(result, resultClose);

                byte[] sha256Close = getSHA256(resultClose);
                if (expectedDigestClose == null) {
                    expectedDigestClose = sha256Close;
                } else {
                    assertArrayEquals(expectedDigestClose, sha256Close);
                }

                documentBytes = baos.toByteArray();
            }

            // Ensure document is readable
            try (InputStream is = new ByteArrayInputStream(documentBytes);
                    PdfReader reader = new PdfReader(is, new byte[]{' '})) {
                assertNotNull(reader);

                byte[] documentId = reader.getDocumentId();
                assertNotNull(documentId);
                assertArrayEquals(originalDocId, documentId);

                PdfArray idArray = reader.getTrailer().getAsArray(PdfName.ID);
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
