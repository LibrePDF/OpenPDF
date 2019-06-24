package com.lowagie.text.pdf.sign;

import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.PdfPKCS7;
import com.lowagie.text.pdf.PdfReader;

public class ExtractCertificatesTest {

    public ExtractCertificatesTest() {
        super();
    }


    @Test
    public void testSha1() throws Exception {
        extract("src/test/resources/sample_signed-sha1.pdf");
    }

    @Test
    public void testSha512() throws Exception {
        extract("src/test/resources/sample_signed-sha512.pdf");
    }

    private void extract(String pdf) throws Exception {

        List<X509Certificate> certificates = new ArrayList<X509Certificate>();
        System.out.println("pdf name: " + pdf);

        KeyStore kall = PdfPKCS7.loadCacertsKeyStore();

        try (PdfReader reader = new PdfReader(pdf)) {
            AcroFields fields = reader.getAcroFields();

            ArrayList<String> signatures = fields.getSignatureNames();
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
