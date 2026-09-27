package org.openpdf.text.pdf.sign;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayOutputStream;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.openpdf.text.pdf.AcroFields;
import org.openpdf.text.pdf.LockAction;
import org.openpdf.text.pdf.LockPermission;
import org.openpdf.text.pdf.PdfArray;
import org.openpdf.text.pdf.PdfDictionary;
import org.openpdf.text.pdf.PdfName;
import org.openpdf.text.pdf.PdfNumber;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.PdfStamper;
import org.openpdf.text.pdf.PdfString;
import org.openpdf.text.pdf.PdfWriter;
import org.openpdf.text.pdf.SignatureFieldLock;

class SignatureFieldLockTest {

    @Test
    void everyActionAndPermissionSurvivesRoundTrip() {
        List<SignatureFieldLock> locks = List.of(
                SignatureFieldLock.all(),
                SignatureFieldLock.include("Nome", "Indirizzo.Città"),
                SignatureFieldLock.exclude("Note"),
                SignatureFieldLock.all().withPermission(LockPermission.NO_CHANGES),
                SignatureFieldLock.include("Nome").withPermission(LockPermission.FORM_FILLING),
                SignatureFieldLock.exclude("Note").withPermission(LockPermission.FORM_FILLING_AND_ANNOTATIONS));
        for (SignatureFieldLock lock : locks) {
            SignatureFieldLock.ParseResult result = SignatureFieldLock.parse(lock.toPdfDictionary());
            assertThat(result.isWellFormed()).as("%s", lock).isTrue();
            assertThat(result.lock()).isEqualTo(lock);
        }
    }

    @Test
    void lockDictionaryHasTheSpecifiedEntries() {
        PdfDictionary dictionary = SignatureFieldLock.include("Nome", "Importo")
                .withPermission(LockPermission.FORM_FILLING).toPdfDictionary();

        assertThat(dictionary.getAsName(PdfName.TYPE)).isEqualTo(PdfName.SIGFIELDLOCK);
        assertThat(dictionary.getAsName(PdfName.ACTION)).isEqualTo(PdfName.INCLUDE);
        PdfArray fields = dictionary.getAsArray(PdfName.FIELDS);
        assertThat(fields.size()).isEqualTo(2);
        assertThat(fields.getAsString(0).toUnicodeString()).isEqualTo("Nome");
        assertThat(dictionary.getAsNumber(PdfName.P).intValue()).isEqualTo(2);
        assertThat(SignatureFieldLock.all().toPdfDictionary().get(PdfName.FIELDS)).isNull();
    }

    @Test
    void transformParamsCarryTheTransformVersion() {
        PdfDictionary params = SignatureFieldLock.exclude("Note").toTransformParams();

        assertThat(params.getAsName(PdfName.TYPE)).isEqualTo(PdfName.TRANSFORMPARAMS);
        assertThat(params.getAsName(PdfName.V)).isEqualTo(new PdfName("1.2"));
        assertThat(params.getAsName(PdfName.ACTION)).isEqualTo(PdfName.EXCLUDE);
        assertThat(SignatureFieldLock.parse(params).lock()).isEqualTo(SignatureFieldLock.exclude("Note"));
    }

    @Test
    void invalidLocksAreRejected() {
        assertThatThrownBy(SignatureFieldLock::include).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> SignatureFieldLock.exclude(" ")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> SignatureFieldLock.include("a..b")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> SignatureFieldLock.include("a.")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> SignatureFieldLock.include("Nome", "Nome"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> SignatureFieldLock.of(LockAction.ALL, List.of("Nome"), null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void locksMatchFieldsAndTheirDescendants() {
        SignatureFieldLock include = SignatureFieldLock.include("Indirizzo");
        assertThat(include.locks("Indirizzo")).isTrue();
        assertThat(include.locks("Indirizzo.Città")).isTrue();
        assertThat(include.locks("IndirizzoBis")).isFalse();

        SignatureFieldLock exclude = SignatureFieldLock.exclude("Note");
        assertThat(exclude.locks("Note")).isFalse();
        assertThat(exclude.locks("Nome")).isTrue();

        assertThat(SignatureFieldLock.all().locks("anything")).isTrue();
    }

    @Test
    void parsingReportsProblemsInsteadOfThrowing() {
        PdfDictionary noAction = new PdfDictionary();
        assertThat(SignatureFieldLock.parse(noAction).lock()).isNull();
        assertThat(SignatureFieldLock.parse(null).problems()).isNotEmpty();

        PdfDictionary badEntries = new PdfDictionary();
        badEntries.put(PdfName.ACTION, PdfName.INCLUDE);
        PdfArray fields = new PdfArray();
        fields.add(new PdfString("Nome"));
        fields.add(new PdfNumber(3));
        badEntries.put(PdfName.FIELDS, fields);
        badEntries.put(PdfName.P, new PdfNumber(7));
        SignatureFieldLock.ParseResult result = SignatureFieldLock.parse(badEntries);

        assertThat(result.lock()).isEqualTo(SignatureFieldLock.include("Nome"));
        assertThat(result.problems()).hasSize(2);
        assertThat(result.isWellFormed()).isFalse();
    }

    @Test
    void lockOfNewDocumentFieldIsIndirectAndSurvivesReopening() throws Exception {
        SignatureFieldLock lock = SignatureFieldLock.include(FormFixture.NOME, FormFixture.IMPORTO);
        byte[] pdf = FormFixture.builder().lock(FormFixture.FIRMA_A, lock).build();

        try (PdfReader reader = new PdfReader(pdf)) {
            AcroFields fields = reader.getAcroFields();
            assertThat(fields.getSignatureFieldLock(FormFixture.FIRMA_A)).isEqualTo(lock);
            assertThat(fields.getSignatureFieldLock(FormFixture.FIRMA_B)).isNull();
            assertThat(fields.getFieldItem(FormFixture.FIRMA_A).getMerged(0).get(PdfName.LOCK).isIndirect())
                    .isTrue();
        }
    }

    @Test
    void lockPermissionRequiresPdf20Output() throws Exception {
        SignatureFieldLock lock = SignatureFieldLock.include(FormFixture.NOME)
                .withPermission(LockPermission.FORM_FILLING);

        assertThatThrownBy(() -> FormFixture.builder().pdfVersion(PdfWriter.VERSION_1_7)
                .lock(FormFixture.FIRMA_A, lock).build())
                .isInstanceOf(IllegalStateException.class);

        byte[] pdf = FormFixture.builder().pdfVersion(PdfWriter.VERSION_2_0).lock(FormFixture.FIRMA_A, lock).build();
        try (PdfReader reader = new PdfReader(pdf)) {
            assertThat(reader.getAcroFields().getSignatureFieldLock(FormFixture.FIRMA_A)).isEqualTo(lock);
        }
    }

    @Test
    void stamperAddsLockedSignatureFieldsInAppendMode() throws Exception {
        byte[] original = FormFixture.create();
        SignatureFieldLock lock = SignatureFieldLock.exclude(FormFixture.NOTE)
                .withPermission(LockPermission.FORM_FILLING);

        try (PdfReader reader = new PdfReader(original)) {
            PdfStamper stamper = new PdfStamper(reader, new ByteArrayOutputStream(), null, true);
            assertThatThrownBy(() -> stamper.addSignature("FirmaD", 1, 300, 350, 500, 420, lock))
                    .isInstanceOf(IllegalStateException.class);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (PdfReader reader = new PdfReader(original)) {
            PdfStamper stamper = new PdfStamper(reader, out, null, true);
            stamper.getWriter().setPdfVersion(PdfWriter.PDF_VERSION_2_0);
            stamper.addSignature("FirmaD", 1, 300, 350, 500, 420, lock);
            stamper.close();
        }
        try (PdfReader reader = new PdfReader(out.toByteArray())) {
            assertThat(reader.getAcroFields().getSignatureFieldLock("FirmaD")).isEqualTo(lock);
            assertThat(reader.getCatalog().getAsName(PdfName.VERSION)).isEqualTo(PdfWriter.PDF_VERSION_2_0);
        }
    }
}
