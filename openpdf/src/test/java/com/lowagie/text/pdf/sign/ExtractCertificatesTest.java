package com.lowagie.text.pdf.sign;

import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.PdfPKCS7;
import com.lowagie.text.pdf.PdfReader;
import org.junit.jupiter.api.Test;

class ExtractCertificatesTest {

    ExtractCertificatesTest() {
        super();
    }


    @Test
    void testSha1() throws Exception {
        extract("src/test/resources/sample_signed-sha1.pdf");
    }

    @Test
    void testSha512() throws Exception {
        extract("src/test/resources/sample_signed-sha512.pdf");
    }

    /**
     * Extract certificates and validate timestamp
     * Sample file taken from https://www.tecxoft.com/samples.php (it has signature with timestamp with SHA-256)
     * NB: Signature will only be valid till 2022/01/02 (Hence only Sout is used not assert)
     *
     * @throws Exception
     */
    @Test
    void testSha256TimeStamp() throws Exception {
        extract("src/test/resources/pdf_digital_signature_timestamp.pdf");
    }

    private void extract(String pdf) throws Exception {

        List<X509Certificate> certificates = new ArrayList<>();
        System.out.println("pdf name: " + pdf);

        KeyStore kall = PdfPKCS7.loadCacertsKeyStore();

        try (PdfReader reader = new PdfReader(pdf)) {
            AcroFields fields = reader.getAcroFields();

            List<String> signatures = fields.getSignedFieldNames();
            System.out.println("Signs: " + signatures.size());
            for (String signature : signatures) {

                System.out.println("Signature name: " + signature);
                System.out.println("Signature covers whole document: " + fields.signatureCoversWholeDocument(signature));
                System.out.println(
                        "Document revision: " + fields.getRevision(signature) + " of " + fields.getTotalRevisions());

                PdfPKCS7 pk = fields.verifySignature(signature);
                Calendar cal = pk.getSignDate();
                Certificate[] pkc = pk.getCertificates();
                X509Certificate certificate = pk.getSigningCertificate();
                certificates.add(certificate);
                System.out.println("sign date:" + cal.getTime());
                System.out.println("Subject: " + PdfPKCS7.getSubjectFields(certificate));
                System.out.println("Document modified: " + !pk.verify());
                System.out.println("Timestamp valid: " + pk.verifyTimestampImprint());

                Object[] fails = PdfPKCS7.verifyCertificates(pkc, kall, null, cal);
                if (fails == null) {
                    System.out.println("Certificates verified against the KeyStore");
                } else {
                    System.out.println("Certificate failed: " + fails[1]);
                }

            }
        }

    }
}
