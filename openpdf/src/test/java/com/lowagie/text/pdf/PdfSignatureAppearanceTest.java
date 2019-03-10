package com.lowagie.text.pdf;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.HashMap;

import com.lowagie.text.Utilities;

import com.lowagie.text.DocumentException;
import com.lowagie.text.Rectangle;
import org.junit.jupiter.api.Test;

public class PdfSignatureAppearanceTest {

    @Test
    void invisibleExternalSignature() throws DocumentException, IOException, NoSuchAlgorithmException {
        byte[] expectedDigestPreClose = null;
        byte[] expectedDigestClose = null;

        // These fields are provided to be able to generate the same content more than
        // once
        Calendar signDate = Calendar.getInstance();
        PdfObject overrideFileId = new PdfLiteral("<123><123>".getBytes());

        for (int i = 0; i < 10; i++) {
            try (InputStream is = getClass().getResourceAsStream("/EmptyPage.pdf"); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                PdfReader reader = new PdfReader(is);
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

                HashMap exc = new HashMap();
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
            }
        }
    }

    @Test
    void visibleExternalSignature() throws DocumentException, IOException, NoSuchAlgorithmException {
        byte[] expectedDigestPreClose = null;
        byte[] expectedDigestClose = null;

        Calendar signDate = Calendar.getInstance();
        PdfObject overrideFileId = new PdfLiteral("<123><123>".getBytes());

        for (int i = 0; i < 10; i++) {
            try (InputStream is = getClass().getResourceAsStream("/EmptyPage.pdf"); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                PdfReader reader = new PdfReader(is);
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

                HashMap exc = new HashMap();
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
            }
        }
    }

    private byte[] getSHA256(byte[] bytes) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return md.digest(bytes);
    }

}
