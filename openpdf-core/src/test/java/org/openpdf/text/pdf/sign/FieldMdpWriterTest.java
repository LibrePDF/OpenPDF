package org.openpdf.text.pdf.sign;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayOutputStream;
import org.junit.jupiter.api.Test;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.AcroFields;
import org.openpdf.text.pdf.LockPermission;
import org.openpdf.text.pdf.PdfArray;
import org.openpdf.text.pdf.PdfDictionary;
import org.openpdf.text.pdf.PdfName;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.PdfSignatureAppearance;
import org.openpdf.text.pdf.PdfStamper;
import org.openpdf.text.pdf.PdfWriter;
import org.openpdf.text.pdf.SignatureFieldLock;
import org.openpdf.text.pdf.SignatureType;

class FieldMdpWriterTest {

    private static final SignatureFieldLock LOCK_NOME = SignatureFieldLock.include(FormFixture.NOME);

    @Test
    void signingLockedVisibleFieldAddsFieldMdp() throws Exception {
        byte[] pdf = FormFixture.builder().lock(FormFixture.FIRMA_A, LOCK_NOME).build();
        pdf = TestSigner.sign(pdf, TestSigner.existingField(FormFixture.FIRMA_A).signer("A"));
        DocumentEditor.save("fieldmdp-visible.pdf", pdf);

        try (PdfReader reader = new PdfReader(pdf)) {
            AcroFields fields = reader.getAcroFields();
            PdfArray references = fields.getSignatureDictionary(FormFixture.FIRMA_A).getAsArray(PdfName.REFERENCE);
            assertThat(references.size()).isEqualTo(1);
            PdfDictionary reference = references.getAsDict(0);
            assertThat(reference.getAsName(PdfName.TRANSFORMMETHOD)).isEqualTo(PdfName.FIELDMDP);
            assertThat(reference.getAsName(PdfName.TYPE)).isEqualTo(PdfName.SIGREF);
            assertThat(reference.getAsIndirectObject(PdfName.DATA)).isNotNull();
            assertThat(fields.getSignatureFieldMdp(FormFixture.FIRMA_A)).isEqualTo(LOCK_NOME);
            assertThat(fields.getSignatureTypes().get(FormFixture.FIRMA_A)).isEqualTo(SignatureType.APPROVAL);
            assertThat(fields.verifySignature(FormFixture.FIRMA_A).verify()).isTrue();
        }
    }

    @Test
    void signingLockedInvisibleFieldReusesTheField() throws Exception {
        SignatureFieldLock lock = SignatureFieldLock.all();
        byte[] pdf = FormFixture.builder().lock(FormFixture.FIRMA_INV, lock).build();
        int fieldCount = topLevelFieldCount(pdf);
        pdf = TestSigner.sign(pdf, TestSigner.existingField(FormFixture.FIRMA_INV).signer("A"));
        DocumentEditor.save("fieldmdp-invisible.pdf", pdf);

        try (PdfReader reader = new PdfReader(pdf)) {
            AcroFields fields = reader.getAcroFields();
            assertThat(fields.getFieldItem(FormFixture.FIRMA_INV).size()).isEqualTo(1);
            assertThat(topLevelFieldCount(pdf)).isEqualTo(fieldCount);
            assertThat(fields.getSignedFieldNames()).containsExactly(FormFixture.FIRMA_INV);
            assertThat(fields.getSignatureFieldMdp(FormFixture.FIRMA_INV)).isEqualTo(lock);
            assertThat(fields.verifySignature(FormFixture.FIRMA_INV).verify()).isTrue();
        }
    }

    @Test
    void newFieldWithLockGetsLockAndFieldMdp() throws Exception {
        SignatureFieldLock lock = SignatureFieldLock.exclude(FormFixture.NOTE);
        byte[] pdf = TestSigner.sign(FormFixture.create(), TestSigner
                .newVisibleField("FirmaNuova", new Rectangle(300, 350, 500, 420), 1).signer("A").lock(lock));

        try (PdfReader reader = new PdfReader(pdf)) {
            AcroFields fields = reader.getAcroFields();
            assertThat(fields.getSignatureFieldLock("FirmaNuova")).isEqualTo(lock);
            assertThat(fields.getSignatureFieldMdp("FirmaNuova")).isEqualTo(lock);
            assertThat(fields.verifySignature("FirmaNuova").verify()).isTrue();
        }
    }

    @Test
    void certificationAndLockShareTheReferenceArray() throws Exception {
        byte[] pdf = FormFixture.builder().lock(FormFixture.FIRMA_A, LOCK_NOME).build();
        pdf = TestSigner.sign(pdf, TestSigner.existingField(FormFixture.FIRMA_A).signer("A")
                .certify(PdfSignatureAppearance.CERTIFIED_FORM_FILLING));
        DocumentEditor.save("fieldmdp-certified.pdf", pdf);

        try (PdfReader reader = new PdfReader(pdf)) {
            AcroFields fields = reader.getAcroFields();
            PdfArray references = fields.getSignatureDictionary(FormFixture.FIRMA_A).getAsArray(PdfName.REFERENCE);
            assertThat(references.size()).isEqualTo(2);
            assertThat(references.getAsDict(0).getAsName(PdfName.TRANSFORMMETHOD)).isEqualTo(PdfName.DOCMDP);
            assertThat(references.getAsDict(1).getAsName(PdfName.TRANSFORMMETHOD)).isEqualTo(PdfName.FIELDMDP);
            assertThat(fields.getSignatureTypes().get(FormFixture.FIRMA_A))
                    .isEqualTo(SignatureType.CERTIFICATION_FILLINGFORMS_SIGNING);
            assertThat(reader.getCertificationLevel()).isEqualTo(PdfSignatureAppearance.CERTIFIED_FORM_FILLING);
            assertThat(fields.verifySignature(FormFixture.FIRMA_A).verify()).isTrue();
        }
    }

    @Test
    void certificationKeepsUsageRights() throws Exception {
        byte[] pdf = withUsageRights(FormFixture.create());
        pdf = TestSigner.sign(pdf, TestSigner.existingField(FormFixture.FIRMA_A).signer("A")
                .certify(PdfSignatureAppearance.CERTIFIED_FORM_FILLING));

        try (PdfReader reader = new PdfReader(pdf)) {
            PdfDictionary perms = reader.getCatalog().getAsDict(PdfName.PERMS);
            assertThat(perms.get(PdfName.UR3)).isNotNull();
            assertThat(perms.get(PdfName.DOCMDP)).isNotNull();
            assertThat(reader.getCertificationLevel()).isEqualTo(PdfSignatureAppearance.CERTIFIED_FORM_FILLING);
        }
    }

    @Test
    void certificationMustBeTheFirstSignature() throws Exception {
        byte[] pdf = TestSigner.sign(FormFixture.create(), TestSigner.existingField(FormFixture.FIRMA_A));

        assertThatThrownBy(() -> TestSigner.sign(pdf, TestSigner.existingField(FormFixture.FIRMA_B)
                .certify(PdfSignatureAppearance.CERTIFIED_FORM_FILLING)))
                .isInstanceOf(DocumentException.class)
                .hasMessageContaining("first signature");
    }

    @Test
    void aFieldLockedByAnEarlierSignatureCannotBeSigned() throws Exception {
        byte[] original = FormFixture.builder()
                .lock(FormFixture.FIRMA_A, SignatureFieldLock.include(FormFixture.FIRMA_B)).build();
        byte[] pdf = TestSigner.sign(original, TestSigner.existingField(FormFixture.FIRMA_A).signer("A"));

        assertThatThrownBy(() -> TestSigner.sign(pdf, TestSigner.existingField(FormFixture.FIRMA_B).signer("B")))
                .isInstanceOf(DocumentException.class)
                .hasMessageContaining("locked by the signature " + FormFixture.FIRMA_A);
        byte[] signedTwice = TestSigner.sign(pdf, TestSigner.existingField(FormFixture.FIRMA_C).signer("C"));
        try (PdfReader reader = new PdfReader(signedTwice)) {
            assertThat(reader.getAcroFields().getSignedFieldNames()).hasSize(2);
        }
    }

    @Test
    void lockNamesMustExistWhenSigning() {
        assertThatThrownBy(() -> TestSigner.sign(FormFixture.create(),
                TestSigner.newInvisibleField("FirmaNuova").lock(SignatureFieldLock.include("Missing"))))
                .isInstanceOf(DocumentException.class)
                .hasMessageContaining("Missing");
    }

    @Test
    void lockPermissionCannotWidenThePermissionInEffect() throws Exception {
        byte[] pdf = TestSigner.sign(FormFixture.builder().pdfVersion(PdfWriter.VERSION_2_0).build(),
                TestSigner.existingField(FormFixture.FIRMA_A).signer("A")
                        .certify(PdfSignatureAppearance.CERTIFIED_FORM_FILLING));

        assertThatThrownBy(() -> TestSigner.sign(pdf, TestSigner.newInvisibleField("FirmaNuova").signer("B")
                .lock(SignatureFieldLock.all().withPermission(LockPermission.FORM_FILLING_AND_ANNOTATIONS))))
                .isInstanceOf(DocumentException.class)
                .hasMessageContaining("wider");
        byte[] restricted = TestSigner.sign(pdf, TestSigner.newInvisibleField("FirmaNuova").signer("B")
                .lock(SignatureFieldLock.include(FormFixture.NOME).withPermission(LockPermission.NO_CHANGES)));
        try (PdfReader reader = new PdfReader(restricted)) {
            assertThat(reader.getAcroFields().getSignatureFieldMdp("FirmaNuova").getPermission())
                    .isEqualTo(LockPermission.NO_CHANGES);
        }
    }

    @Test
    void lockPermissionOnNewFieldRequiresPdf20() throws Exception {
        SignatureFieldLock lock = SignatureFieldLock.include(FormFixture.NOME)
                .withPermission(LockPermission.FORM_FILLING);

        assertThatThrownBy(() -> TestSigner.sign(FormFixture.create(),
                TestSigner.newInvisibleField("FirmaNuova").lock(lock)))
                .isInstanceOf(DocumentException.class)
                .hasMessageContaining("PDF 2.0");
        byte[] pdf = TestSigner.sign(FormFixture.create(), TestSigner.newInvisibleField("FirmaNuova").lock(lock)
                .pdf20());
        try (PdfReader reader = new PdfReader(pdf)) {
            assertThat(reader.getAcroFields().getSignatureFieldMdp("FirmaNuova")).isEqualTo(lock);
        }
    }

    @Test
    void theLockOfAnExistingFieldCannotBeReplaced() {
        assertThatThrownBy(() -> TestSigner.sign(FormFixture.create(),
                TestSigner.existingField(FormFixture.FIRMA_A).lock(LOCK_NOME)))
                .isInstanceOf(DocumentException.class)
                .hasMessageContaining("cannot be replaced");
    }

    @Test
    void aSignedFieldCannotBeSignedAgain() throws Exception {
        byte[] pdf = TestSigner.sign(FormFixture.create(), TestSigner.existingField(FormFixture.FIRMA_A));

        assertThatThrownBy(() -> TestSigner.sign(pdf, TestSigner.existingField(FormFixture.FIRMA_A)))
                .isInstanceOf(DocumentException.class)
                .hasMessageContaining("already signed");
    }

    private static int topLevelFieldCount(byte[] pdf) throws Exception {
        try (PdfReader reader = new PdfReader(pdf)) {
            PdfDictionary acroForm = reader.getCatalog().getAsDict(PdfName.ACROFORM);
            return acroForm.getAsArray(PdfName.FIELDS).size();
        }
    }

    private static byte[] withUsageRights(byte[] pdf) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (PdfReader reader = new PdfReader(pdf)) {
            PdfStamper stamper = new PdfStamper(reader, out, null, true);
            PdfDictionary perms = new PdfDictionary();
            perms.put(PdfName.UR3, new PdfDictionary(PdfName.SIG));
            PdfDictionary catalog = reader.getCatalog();
            catalog.put(PdfName.PERMS, perms);
            stamper.markUsed(catalog);
            stamper.close();
        }
        return out.toByteArray();
    }
}
