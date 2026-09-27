package org.openpdf.text.pdf.mdp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayOutputStream;
import org.junit.jupiter.api.Test;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.LockPermission;
import org.openpdf.text.pdf.PdfArray;
import org.openpdf.text.pdf.PdfDictionary;
import org.openpdf.text.pdf.PdfName;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.PdfSignatureAppearance;
import org.openpdf.text.pdf.PdfStamper;
import org.openpdf.text.pdf.PdfString;
import org.openpdf.text.pdf.PdfWriter;
import org.openpdf.text.pdf.SignatureFieldLock;
import org.openpdf.text.pdf.sign.DocumentEditor;
import org.openpdf.text.pdf.sign.FormFixture;
import org.openpdf.text.pdf.sign.TestSigner;

class SignatureValidatorTest {

    private static final String A = FormFixture.FIRMA_A;
    private static final String B = FormFixture.FIRMA_B;
    private static final String C = FormFixture.FIRMA_C;

    @Test
    void certificationAllowsFormFillingAndLaterSignatures() throws Exception {
        byte[] pdf = certify(FormFixture.create(), PdfSignatureAppearance.CERTIFIED_FORM_FILLING);
        pdf = DocumentEditor.fillField(pdf, FormFixture.NOTE, "Allowed");
        pdf = sign(pdf, B);

        DocumentValidationReport report = validate(pdf);

        assertStatus(report, A, SignatureStatus.VALID_WITH_ALLOWED_CHANGES);
        assertStatus(report, B, SignatureStatus.VALID);
        assertThat(report.isFullyConformant()).as("%s", report).isTrue();
        assertThat(result(report, A).mdpStatus()).isEqualTo(MdpStatus.ALLOWED_CHANGES);
        assertThat(result(report, B).revision()).isEqualTo(2);
    }

    @Test
    void changingAnIncludedFieldViolatesTheFieldMdpOfTheLockingSignature() throws Exception {
        byte[] pdf = FormFixture.builder().lock(A, SignatureFieldLock.include(FormFixture.NOME)).build();
        pdf = sign(pdf, A);
        pdf = DocumentEditor.fillField(pdf, FormFixture.NOME, "Changed");
        pdf = sign(pdf, B);

        DocumentValidationReport report = validate(pdf);

        assertStatus(report, A, SignatureStatus.UNAUTHORIZED_CHANGE);
        assertThat(result(report, A).violations())
                .isNotEmpty()
                .allSatisfy(v -> {
                    assertThat(v.fieldName()).isEqualTo(FormFixture.NOME);
                    assertThat(v.constraint()).isEqualTo("FieldMDP lock of " + A);
                    assertThat(v.revision()).isEqualTo(2);
                });
        assertStatus(report, B, SignatureStatus.VALID);
    }

    @Test
    void fillingAnUnlockedFieldIsAllowed() throws Exception {
        byte[] pdf = FormFixture.builder().lock(A, SignatureFieldLock.include(FormFixture.NOME)).build();
        pdf = sign(pdf, A);
        pdf = DocumentEditor.fillField(pdf, FormFixture.NOTE, "Allowed");
        pdf = sign(pdf, B);

        DocumentValidationReport report = validate(pdf);

        assertStatus(report, A, SignatureStatus.VALID_WITH_ALLOWED_CHANGES);
        assertStatus(report, B, SignatureStatus.VALID);
    }

    @Test
    void excludeLocksEveryOtherField() throws Exception {
        byte[] pdf = FormFixture.builder().lock(A, SignatureFieldLock.exclude(FormFixture.NOTE)).build();
        pdf = sign(pdf, A);
        byte[] noteFilled = DocumentEditor.fillField(pdf, FormFixture.NOTE, "Allowed");
        byte[] amountFilled = DocumentEditor.fillField(pdf, FormFixture.IMPORTO, "100");

        assertStatus(validate(noteFilled), A, SignatureStatus.VALID_WITH_ALLOWED_CHANGES);
        assertStatus(validate(amountFilled), A, SignatureStatus.UNAUTHORIZED_CHANGE);
    }

    @Test
    void aLaterLockCannotWidenThePermission() throws Exception {
        byte[] pdf = FormFixture.builder().pdfVersion(PdfWriter.VERSION_2_0)
                .lock(B, SignatureFieldLock.include(FormFixture.NOME).withPermission(LockPermission.FORM_FILLING))
                .build();
        pdf = certify(pdf, PdfSignatureAppearance.CERTIFIED_FORM_FILLING_AND_ANNOTATIONS);
        pdf = sign(pdf, B);
        byte[] beforeC = pdf;

        // the writer refuses to widen the permission
        assertThatThrownBy(() -> TestSigner.sign(beforeC, TestSigner.newInvisibleField("FirmaD").signer("C")
                .lock(SignatureFieldLock.all().withPermission(LockPermission.FORM_FILLING_AND_ANNOTATIONS))))
                .isInstanceOf(DocumentException.class);

        // a hand-built signature declaring P=3 is reported and ignored
        SignatureFieldLock widening = SignatureFieldLock.include(FormFixture.IMPORTO)
                .withPermission(LockPermission.FORM_FILLING_AND_ANNOTATIONS);
        pdf = TestSigner.sign(pdf, TestSigner.existingField(C).signer("C")
                .signatureEvent(signature -> addFieldMdp(signature, widening)));
        pdf = DocumentEditor.addComment(pdf);

        DocumentValidationReport report = validate(pdf);

        assertStatus(report, A, SignatureStatus.VALID_WITH_ALLOWED_CHANGES);
        assertStatus(report, B, SignatureStatus.UNAUTHORIZED_CHANGE);
        assertThat(result(report, B).violations())
                .anySatisfy(v -> assertThat(v.role()).isEqualTo(ChangeRole.ANNOTATION));
        SignatureValidationResult c = result(report, C);
        assertThat(c.hasDiagnostic(Diagnostic.Code.PERMISSION_WIDENING_IGNORED)).isTrue();
        assertThat(c.isFullyConformant()).isFalse();
    }

    @Test
    void overwritingSignedBytesBreaksTheSignature() throws Exception {
        byte[] pdf = certify(FormFixture.create(), PdfSignatureAppearance.CERTIFIED_FORM_FILLING_AND_ANNOTATIONS);
        pdf = sign(pdf, B);

        DocumentValidationReport report = validate(DocumentEditor.tamperFirstRevision(pdf));

        assertStatus(report, A, SignatureStatus.CRYPTO_INVALID);
        assertThat(result(report, A).cryptoStatus()).isEqualTo(CryptoStatus.INVALID);
    }

    @Test
    void addingDataFieldsIsNotFormFilling() throws Exception {
        byte[] pdf = certify(FormFixture.create(), PdfSignatureAppearance.CERTIFIED_FORM_FILLING);
        pdf = DocumentEditor.addTextField(pdf, "Extra");

        DocumentValidationReport report = validate(pdf);

        assertStatus(report, A, SignatureStatus.UNAUTHORIZED_CHANGE);
        assertThat(result(report, A).violations())
                .singleElement()
                .satisfies(v -> {
                    assertThat(v.fieldName()).isEqualTo("Extra");
                    assertThat(v.role()).isEqualTo(ChangeRole.FIELD);
                });
    }

    @Test
    void lockWithoutFieldMdpIsReportedAndEnforced() throws Exception {
        byte[] pdf = FormFixture.builder().lock(A, SignatureFieldLock.include(FormFixture.NOME)).build();
        pdf = TestSigner.sign(pdf, TestSigner.existingField(A).signer("A")
                .signatureEvent(signature -> signature.remove(PdfName.REFERENCE)));

        SignatureValidationResult signed = result(validate(pdf), A);
        assertThat(signed.hasDiagnostic(Diagnostic.Code.LOCK_WITHOUT_FIELDMDP)).isTrue();
        assertThat(signed.isFullyConformant()).isFalse();

        pdf = DocumentEditor.fillField(pdf, FormFixture.NOME, "Changed");
        assertStatus(validate(pdf), A, SignatureStatus.UNAUTHORIZED_CHANGE);
    }

    @Test
    void malformedReferencesAreReported() throws Exception {
        byte[] pdf = TestSigner.sign(FormFixture.create(), TestSigner.existingField(A).signer("A")
                .signatureEvent(signature -> signature.put(PdfName.REFERENCE, PdfName.FIELDMDP)));

        SignatureValidationResult signed = result(validate(pdf), A);

        assertThat(signed.status()).isEqualTo(SignatureStatus.MALFORMED);
        assertThat(signed.hasDiagnostic(Diagnostic.Code.REFERENCE_MALFORMED)).isTrue();
        assertThat(signed.cryptoStatus()).isEqualTo(CryptoStatus.VALID);
    }

    @Test
    void noChangesCertificationForbidsLaterSignatures() throws Exception {
        byte[] pdf = certify(FormFixture.create(), PdfSignatureAppearance.CERTIFIED_NO_CHANGES_ALLOWED);
        pdf = sign(pdf, B);

        DocumentValidationReport report = validate(pdf);

        assertStatus(report, A, SignatureStatus.UNAUTHORIZED_CHANGE);
        assertThat(result(report, A).violations())
                .allSatisfy(v -> {
                    assertThat(v.fieldName()).isEqualTo(B);
                    assertThat(v.constraint()).startsWith("DocMDP P=1");
                });
        assertStatus(report, B, SignatureStatus.VALID);
    }

    @Test
    void documentSecurityStoreIsAllowedEvenWithNoChanges() throws Exception {
        byte[] pdf = certify(FormFixture.create(), PdfSignatureAppearance.CERTIFIED_NO_CHANGES_ALLOWED);
        pdf = DocumentEditor.addDss(pdf);

        assertStatus(validate(pdf), A, SignatureStatus.VALID_WITH_ALLOWED_CHANGES);
    }

    @Test
    void signingAnInvisibleExistingFieldKeepsOneValidSignature() throws Exception {
        byte[] pdf = sign(FormFixture.create(), FormFixture.FIRMA_INV);

        DocumentValidationReport report = validate(pdf);

        assertThat(report.signatures()).hasSize(1);
        assertStatus(report, FormFixture.FIRMA_INV, SignatureStatus.VALID);
    }

    @Test
    void annotationsNeedPermissionThree() throws Exception {
        byte[] formFilling = DocumentEditor.addComment(
                certify(FormFixture.create(), PdfSignatureAppearance.CERTIFIED_FORM_FILLING));
        byte[] annotations = DocumentEditor.addComment(
                certify(FormFixture.create(), PdfSignatureAppearance.CERTIFIED_FORM_FILLING_AND_ANNOTATIONS));

        DocumentValidationReport formFillingReport = validate(formFilling);
        assertStatus(formFillingReport, A, SignatureStatus.UNAUTHORIZED_CHANGE);
        assertThat(result(formFillingReport, A).violations())
                .singleElement()
                .satisfies(v -> assertThat(v.role()).isEqualTo(ChangeRole.ANNOTATION));
        assertStatus(validate(annotations), A, SignatureStatus.VALID_WITH_ALLOWED_CHANGES);
    }

    @Test
    void approvalSignaturesDoNotRestrictChanges() throws Exception {
        byte[] pdf = DocumentEditor.addComment(sign(FormFixture.create(), A));

        DocumentValidationReport report = validate(pdf);

        assertStatus(report, A, SignatureStatus.VALID_WITH_ALLOWED_CHANGES);
        assertThat(report.diagnostics())
                .anySatisfy(d -> assertThat(d.code()).isEqualTo(Diagnostic.Code.NO_MDP_RESTRICTION));
    }

    @Test
    void lateSignatureFieldIsAllowedWithWarning() throws Exception {
        byte[] pdf = certify(FormFixture.create(), PdfSignatureAppearance.CERTIFIED_FORM_FILLING);
        pdf = TestSigner.sign(pdf, TestSigner.newVisibleField("FirmaNuova", new Rectangle(300, 350, 500, 420), 1)
                .signer("B"));

        SignatureValidationResult certification = result(validate(pdf), A);

        assertThat(certification.status()).as("%s", certification)
                .isEqualTo(SignatureStatus.VALID_WITH_ALLOWED_CHANGES);
        assertThat(certification.hasDiagnostic(Diagnostic.Code.LATE_SIGNATURE_FIELD)).isTrue();
        assertThat(certification.isFullyConformant()).isFalse();
    }

    @Test
    void editingSignatureDictionaryAfterSigningIsDetected() throws Exception {
        byte[] pdf = sign(FormFixture.create(), A);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (PdfReader reader = new PdfReader(pdf)) {
            PdfStamper stamper = new PdfStamper(reader, out, null, true);
            PdfDictionary signature = stamper.getAcroFields().getSignatureDictionary(A);
            signature.put(PdfName.REASON, new PdfString("Forged reason"));
            stamper.markUsed(signature);
            stamper.close();
        }

        SignatureValidationResult result = result(validate(out.toByteArray()), A);

        assertThat(result.cryptoStatus()).isEqualTo(CryptoStatus.VALID);
        assertThat(result.status()).isEqualTo(SignatureStatus.UNAUTHORIZED_CHANGE);
        assertThat(result.violations())
                .anySatisfy(v -> assertThat(v.role()).isEqualTo(ChangeRole.SIGNATURE_DICTIONARY));
    }

    private static byte[] certify(byte[] pdf, int level) throws Exception {
        return TestSigner.sign(pdf, TestSigner.existingField(A).signer("A").certify(level));
    }

    private static byte[] sign(byte[] pdf, String field) throws Exception {
        return TestSigner.sign(pdf, TestSigner.existingField(field).signer(field));
    }

    private static void addFieldMdp(PdfDictionary signature, SignatureFieldLock lock) {
        PdfDictionary reference = new PdfDictionary(PdfName.SIGREF);
        reference.put(PdfName.TRANSFORMMETHOD, PdfName.FIELDMDP);
        reference.put(PdfName.TRANSFORMPARAMS, lock.toTransformParams());
        PdfArray references = new PdfArray();
        references.add(reference);
        signature.put(PdfName.REFERENCE, references);
    }

    private static DocumentValidationReport validate(byte[] pdf) throws Exception {
        try (PdfReader reader = new PdfReader(pdf)) {
            return new SignatureValidator().validate(reader);
        }
    }

    private static SignatureValidationResult result(DocumentValidationReport report, String name) {
        return report.getSignature(name).orElseThrow(() -> new AssertionError("No signature " + name));
    }

    private static void assertStatus(DocumentValidationReport report, String name, SignatureStatus expected) {
        assertThat(result(report, name).status()).as("%s", report).isEqualTo(expected);
    }
}
