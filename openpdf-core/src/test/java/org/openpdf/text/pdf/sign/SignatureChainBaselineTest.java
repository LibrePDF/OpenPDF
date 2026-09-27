package org.openpdf.text.pdf.sign;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.openpdf.text.pdf.AcroFields;
import org.openpdf.text.pdf.PdfPKCS7;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.PdfSignatureAppearance;

/**
 * Baseline of a serial signature chain: A certifies with {@code /P 2}, a field is filled in append mode and B signs.
 */
class SignatureChainBaselineTest {

    @Test
    void certifiedChainWithFormFillingKeepsEverySignatureIntact() throws Exception {
        byte[] pdf = FormFixture.create();
        pdf = TestSigner.sign(pdf, TestSigner.existingField(FormFixture.FIRMA_A).signer("A")
                .certify(PdfSignatureAppearance.CERTIFIED_FORM_FILLING));
        pdf = DocumentEditor.fillField(pdf, FormFixture.NOTE, "Filled after the certification");
        pdf = TestSigner.sign(pdf, TestSigner.existingField(FormFixture.FIRMA_B).signer("B"));
        DocumentEditor.save("baseline-chain.pdf", pdf);

        try (PdfReader reader = new PdfReader(pdf)) {
            AcroFields fields = reader.getAcroFields();
            assertThat(fields.getSignedFieldNamesInRevisionOrder())
                    .containsExactly(FormFixture.FIRMA_A, FormFixture.FIRMA_B);
            assertThat(fields.getTotalRevisions()).isEqualTo(2);
            assertThat(fields.getRevision(FormFixture.FIRMA_A)).isEqualTo(1);
            assertThat(fields.getRevision(FormFixture.FIRMA_B)).isEqualTo(2);
            assertThat(fields.signatureCoversWholeDocument(FormFixture.FIRMA_A)).isFalse();
            assertThat(fields.signatureCoversWholeDocument(FormFixture.FIRMA_B)).isTrue();
            assertThat(reader.getCertificationLevel()).isEqualTo(PdfSignatureAppearance.CERTIFIED_FORM_FILLING);
            assertThat(fields.getField(FormFixture.NOTE)).isEqualTo("Filled after the certification");
            for (String name : List.of(FormFixture.FIRMA_A, FormFixture.FIRMA_B)) {
                PdfPKCS7 pkcs7 = fields.verifySignature(name);
                assertThat(pkcs7.verify()).as("signature %s", name).isTrue();
            }
        }
    }
}
