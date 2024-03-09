package com.lowagie.text.pdf;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.Security;
import java.util.List;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AcroFieldsTest {

    /**
     * This test fails, because signatureCoversWholeDocument does only check the last signed block.
     *
     * @throws Exception a ClassCastException
     */
    @Test
    public void testGetSignatures() throws Exception {
        // for algorithm SHA256 (without dash)
        Security.addProvider(new BouncyCastleProvider());
        InputStream moddedFile = AcroFieldsTest.class.getResourceAsStream("/siwa.pdf");
        PdfReader reader = new PdfReader(moddedFile);
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter writer = PdfWriter.getInstance(document, out);

        AcroFields fields = new AcroFields(reader, writer);
        List<String> names = fields.getSignedFieldNames();
        Assertions.assertEquals(1, names.size());

        for (String signName : names) {
            Assertions.assertFalse(fields.signatureCoversWholeDocument(signName));
            // TODO need other PR to fix java.lang.ClassCastException:
            // org.bouncycastle.asn1.BERTaggedObject cannot be cast to
            // org.bouncycastle.asn1.DERTaggedObject
            // PdfPKCS7 pdfPkcs7 = fields.verifySignature(signName, "BC");
            // Assertions.assertTrue(pdfPkcs7.verify());
        }

    }

    @Test
    public void infiniteLoopTest() throws Exception {
        try (InputStream is = AcroFieldsTest.class.getResourceAsStream("/pades_infinite_loop.pdf");
                PdfReader reader = new PdfReader(is)) {
            assertTimeoutPreemptively(ofMillis(500), () -> {
                reader.getAcroFields();
            });
        }

    }

}
