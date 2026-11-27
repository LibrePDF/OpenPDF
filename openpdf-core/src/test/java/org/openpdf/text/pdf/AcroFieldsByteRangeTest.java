package org.openpdf.text.pdf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Date;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openpdf.text.Document;
import org.openpdf.text.Paragraph;

/**
 * The byte range of a signature decides which bytes of the file are signed. A signature is only accepted when the
 * byte range covers the file from its start up to some position, leaving out nothing but the signature value.
 */
class AcroFieldsByteRangeTest {

    private static final String FIELD = "Signature1";

    private static byte[] signed;

    @BeforeAll
    static void signDocument() throws Exception {
        KeyPair keys = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        X500Name name = new X500Name("CN=Test Signer");
        Date from = new Date(System.currentTimeMillis() - 86_400_000L);
        Date to = new Date(System.currentTimeMillis() + 86_400_000L);
        X509Certificate certificate = new JcaX509CertificateConverter().getCertificate(
                new JcaX509v3CertificateBuilder(name, BigInteger.ONE, from, to, name, keys.getPublic())
                        .build(new JcaContentSignerBuilder("SHA256withRSA").build(keys.getPrivate())));

        ByteArrayOutputStream plain = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, plain);
        document.open();
        document.add(new Paragraph("The signed text."));
        document.close();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfStamper stamper = PdfStamper.createSignature(new PdfReader(plain.toByteArray()), out, (String) null);
        stamper.getSignatureAppearance().setCrypto(keys.getPrivate(), new Certificate[]{certificate}, null,
                PdfSignatureAppearance.WINCER_SIGNED);
        stamper.close();
        signed = out.toByteArray();
    }

    @Test
    void signedDocumentIsVerifiedAndCovered() throws Exception {
        try (PdfReader reader = new PdfReader(signed)) {
            AcroFields fields = reader.getAcroFields();
            assertEquals(java.util.List.of(FIELD), fields.getSignedFieldNames());
            assertTrue(fields.verifySignature(FIELD).verify());
            assertTrue(fields.signatureCoversWholeDocument(FIELD));
            assertEquals(1, fields.getRevision(FIELD));
            assertEquals(1, fields.getTotalRevisions());
            try (InputStream revision = fields.extractRevision(FIELD)) {
                assertEquals(signed.length, revision.readAllBytes().length);
            }
        }
    }

    @Test
    void contentAddedAfterTheSignatureIsNotCovered() throws Exception {
        byte[] appended = appendUpdate(signed, null);
        try (PdfReader reader = new PdfReader(appended)) {
            AcroFields fields = reader.getAcroFields();
            assertTrue(fields.verifySignature(FIELD).verify(), "the signed revision itself is unchanged");
            assertFalse(fields.signatureCoversWholeDocument(FIELD));
            assertEquals(1, fields.getRevision(FIELD));
            assertEquals(2, fields.getTotalRevisions());
            try (InputStream revision = fields.extractRevision(FIELD)) {
                assertEquals(signed.length, revision.readAllBytes().length);
            }
        }
    }

    @Test
    void additionalRangeAtTheEndOfTheFileIsRejected() throws Exception {
        // the additional pair covers no bytes, its length makes up for the appended update
        int start = signed.length;
        int length = 0;
        byte[] modified = null;
        for (int k = 0; k < 10; ++k) {
            modified = appendUpdate(signed, new long[]{start, length});
            if (start == modified.length && length == modified.length - signed.length) {
                break;
            }
            start = modified.length;
            length = modified.length - signed.length;
        }
        assertEquals(modified.length, start);
        assertInvalidByteRange(modified);
    }

    @Test
    void negativeLengthIsRejected() throws Exception {
        assertInvalidByteRange(appendUpdate(signed, new long[]{0, -100}));
    }

    @Test
    void rangeThatDoesNotStartAtTheBeginningIsRejected() throws Exception {
        assertInvalidByteRange(replaceRange(signed, 0, 10));
    }

    @Test
    void gapThatIsLargerThanTheSignatureValueIsRejected() throws Exception {
        assertInvalidByteRange(replaceRange(signed, 1, -10));
    }

    @Test
    void rangeBeyondTheEndOfTheFileIsRejected() throws Exception {
        assertInvalidByteRange(replaceRange(signed, 3, 1_000_000));
    }

    @Test
    void signatureWhoseByteRangeLeavesOutOtherContentIsRejected() throws Exception {
        // in this file, the gap between the signed ranges holds a cross-reference table and other objects
        try (InputStream in = AcroFieldsByteRangeTest.class.getResourceAsStream("/siwa.pdf");
                PdfReader reader = new PdfReader(in)) {
            AcroFields fields = reader.getAcroFields();
            assertFalse(fields.signatureCoversWholeDocument(FIELD));
            assertThrows(RuntimeException.class, () -> fields.verifySignature(FIELD));
        }
    }

    private static void assertInvalidByteRange(byte[] pdf) throws Exception {
        try (PdfReader reader = new PdfReader(pdf)) {
            AcroFields fields = reader.getAcroFields();
            assertEquals(java.util.List.of(FIELD), fields.getSignedFieldNames());
            assertFalse(fields.signatureCoversWholeDocument(FIELD));
            assertNull(fields.extractRevision(FIELD));
            assertThrows(RuntimeException.class, () -> fields.verifySignature(FIELD));
        }
    }

    /**
     * Appends an incremental update that adds text to the first page.
     *
     * @param additionalRange a pair to add to the byte range of the signature, or null to leave it as it is
     */
    private static byte[] appendUpdate(byte[] pdf, long[] additionalRange) throws Exception {
        return update(pdf, range -> {
            if (additionalRange == null) {
                return null;
            }
            range.add(new PdfNumber(additionalRange[0]));
            range.add(new PdfNumber(additionalRange[1]));
            return range;
        });
    }

    /**
     * Appends an incremental update that changes one value of the byte range of the signature.
     */
    private static byte[] replaceRange(byte[] pdf, int index, long delta) throws Exception {
        return update(pdf, range -> {
            long value = range.getAsNumber(index).longValue() + delta;
            range.set(index, new PdfNumber(value));
            return range;
        });
    }

    private static byte[] update(byte[] pdf, java.util.function.UnaryOperator<PdfArray> change) throws Exception {
        PdfReader reader = new PdfReader(pdf);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfStamper stamper = new PdfStamper(reader, out, (String) null, true);
        PdfContentByte over = stamper.getOverContent(1);
        over.beginText();
        over.setFontAndSize(BaseFont.createFont(), 14);
        over.showTextAligned(PdfContentByte.ALIGN_LEFT, "Text added after signing.", 72, 700, 0);
        over.endText();
        PdfDictionary signature = stamper.getAcroFields().getSignatureDictionary(FIELD);
        assertNotNull(signature);
        PdfArray range = change.apply(new PdfArray(signature.getAsArray(PdfName.BYTERANGE)));
        if (range != null) {
            signature.put(PdfName.BYTERANGE, range);
            stamper.stamper.markUsed(signature);
        }
        stamper.close();
        return out.toByteArray();
    }
}
