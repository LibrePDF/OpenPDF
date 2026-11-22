package org.openpdf.text.pdf;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Font;
import org.openpdf.text.Rectangle;
import org.openpdf.text.Utilities;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

                PdfStamper stp = PdfStamper.createSignature(reader, baos, null, null, true);
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
                        org.openpdf.text.DocWriter.getISOBytes(idArray.getPdfObject(0).toString()));
                assertEquals("123", idArray.getPdfObject(1).toString());
            }
        }
    }

    @Test
    void visibleExternalSignature() throws DocumentException, IOException, NoSuchAlgorithmException {
        byte[] expectedDigestPreClose = null;
        byte[] expectedDigestClose = null;

        Calendar signDate = Calendar.getInstance();

        byte[] originalDocId = null;
        PdfObject overrideFileId = new PdfLiteral("<123><123>".getBytes());

        byte[] resultDocument = null;

        for (int i = 0; i < 10; i++) {
            try (InputStream is = getClass().getResourceAsStream("/EmptyPage.pdf");
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    PdfReader reader = new PdfReader(is)) {
                originalDocId = reader.getDocumentId();

                PdfStamper stp = PdfStamper.createSignature(reader, baos, null, null, true);
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
                        org.openpdf.text.DocWriter.getISOBytes(idArray.getPdfObject(0).toString()));
                assertEquals("123", idArray.getPdfObject(1).toString());
            }
        }
    }

    @Test
    void nameAndDescriptionKeepsLayer2FontSizeForDescription() throws Exception {
        try (InputStream is = getClass().getResourceAsStream("/EmptyPage.pdf");
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PdfReader reader = new PdfReader(is)) {
            PdfStamper stp = PdfStamper.createSignature(reader, baos, null, null, true);
            PdfSignatureAppearance sap = stp.getSignatureAppearance();
            sap.setCrypto(null, createCertificate("Signer Name"), null, PdfSignatureAppearance.SELF_SIGNED);
            sap.setVisibleSignature(new Rectangle(50, 600, 350, 680), 1);
            sap.setRender(PdfSignatureAppearance.SignatureRenderNameAndDescription);
            sap.setLayer2Font(new Font(Font.HELVETICA, 8));
            sap.setLayer2Text("Digitally signed by Signer Name\nDate: today");

            sap.getAppearance();

            List<Float> sizes = new ArrayList<>();
            Matcher m = Pattern.compile("([0-9.]+) Tf").matcher(sap.getLayer(2).toString());
            while (m.find()) {
                sizes.add(Float.parseFloat(m.group(1)));
            }
            assertEquals(2, sizes.size(), "font sizes in n2: " + sizes);
            assertTrue(sizes.get(0) > 8f, "signer name should be fitted to its column: " + sizes);
            assertEquals(8f, sizes.get(1), "description should keep the layer 2 font size: " + sizes);
        }
    }

    private static X509Certificate createCertificate(String cn) throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        X500Name name = new X500Name("CN=" + cn);
        Date now = new Date();
        X509CertificateHolder holder = new JcaX509v3CertificateBuilder(name, BigInteger.ONE, now,
                new Date(now.getTime() + 86_400_000L), name, kp.getPublic())
                .build(new JcaContentSignerBuilder("SHA256withRSA").build(kp.getPrivate()));
        return new JcaX509CertificateConverter().getCertificate(holder);
    }

    private byte[] getSHA256(byte[] bytes) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return md.digest(bytes);
    }

}
