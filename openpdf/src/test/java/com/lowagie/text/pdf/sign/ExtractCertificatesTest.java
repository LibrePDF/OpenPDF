package com.lowagie.text.pdf.sign;

import static org.assertj.core.api.Assertions.assertThat;

import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.PdfPKCS7;
import com.lowagie.text.pdf.PdfPKCS7.X509Name;
import com.lowagie.text.pdf.PdfReader;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.List;
import org.junit.jupiter.api.Test;

class ExtractCertificatesTest {

    ExtractCertificatesTest() {
        super();
    }


    @Test
    void testSha1() throws Exception {
        extract("src/test/resources/sample_signed-sha1.pdf", false);
    }

    @Test
    void testSha512() throws Exception {
        extract("src/test/resources/sample_signed-sha512.pdf", false);
    }

    /**
     * Extract certificates and validate timestamp Sample file taken from <a
     * href="https://www.tecxoft.com/samples.php">...</a> (it has signature with timestamp with SHA-256) NB: Signature
     * will only be valid till 2022/01/02 (Hence only Sout is used not assert)
     */
    @Test
    void testSha256TimeStamp() throws Exception {
        extract("src/test/resources/pdf_digital_signature_timestamp.pdf", true);
    }

    private void extract(String pdf, boolean isExpectedValidTimeStamp) throws Exception {

        System.out.println("pdf name: " + pdf);

        KeyStore kall = PdfPKCS7.loadCacertsKeyStore();

        try (PdfReader reader = new PdfReader(pdf)) {
            AcroFields fields = reader.getAcroFields();

            List<String> signatures = fields.getSignedFieldNames();
            assertThat(signatures).isNotEmpty().hasSize(1);
            for (String signature : signatures) {

                assertThat(signature).isNotEmpty();
                boolean isWholeDocumentCovered = fields.signatureCoversWholeDocument(signature);
                assertThat(isWholeDocumentCovered).isTrue();
                assertThat(fields.getRevision(signature)).isEqualTo(1);
                assertThat(fields.getTotalRevisions()).isEqualTo(1);

                PdfPKCS7 pk = fields.verifySignature(signature);
                Calendar cal = pk.getSignDate();
                Certificate[] pkc = pk.getCertificates();
                X509Certificate certificate = pk.getSigningCertificate();
                assertThat(cal).isLessThan(Calendar.getInstance());
                X509Name subjectFields = PdfPKCS7.getSubjectFields(certificate);
                assertThat(subjectFields).isNotNull();
                assertThat(subjectFields.getAllFields()).isNotEmpty()
                        .containsKey("C");
                assertThat(pk.verify()).isTrue();
                assertThat(pk.verifyTimestampImprint()).isEqualTo(isExpectedValidTimeStamp);

                Object[] fails = PdfPKCS7.verifyCertificates(pkc, kall, null, cal);
                if (fails == null) {
                    System.out.println("Certificates verified against the KeyStore");
                } else {
                    System.err.println("Certificate failed: " + fails[1]);
                }

            }
        }

    }
}
