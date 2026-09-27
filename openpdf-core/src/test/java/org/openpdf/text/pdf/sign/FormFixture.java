package org.openpdf.text.pdf.sign;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import org.openpdf.text.Document;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.PdfAcroForm;
import org.openpdf.text.pdf.PdfWriter;
import org.openpdf.text.pdf.SignatureFieldLock;
import org.openpdf.text.pdf.TextField;

/**
 * Builds the reference form used by the signature tests: the text fields {@code Nome}, {@code Importo} and
 * {@code Note}, the empty visible signature fields {@code FirmaA}, {@code FirmaB} and {@code FirmaC}, and the empty
 * invisible signature field {@code FirmaInv}.
 */
public final class FormFixture {

    /**
     * The name of the first text field.
     */
    public static final String NOME = "Nome";

    /**
     * The name of the second text field.
     */
    public static final String IMPORTO = "Importo";

    /**
     * The name of the third text field.
     */
    public static final String NOTE = "Note";

    /**
     * The name of the first signature field.
     */
    public static final String FIRMA_A = "FirmaA";

    /**
     * The name of the second signature field.
     */
    public static final String FIRMA_B = "FirmaB";

    /**
     * The name of the third signature field.
     */
    public static final String FIRMA_C = "FirmaC";

    /**
     * The name of the invisible signature field.
     */
    public static final String FIRMA_INV = "FirmaInv";

    private String pdfVersion = PdfWriter.VERSION_1_7;
    private final Map<String, SignatureFieldLock> locks = new HashMap<>();

    private FormFixture() {
    }

    /**
     * Starts a fixture with the default settings: PDF 1.7 and no locks.
     *
     * @return the fixture builder
     */
    public static FormFixture builder() {
        return new FormFixture();
    }

    /**
     * Builds the default fixture.
     *
     * @return the PDF document
     * @throws Exception on error
     */
    public static byte[] create() throws Exception {
        return builder().build();
    }

    /**
     * Sets the PDF version of the document.
     *
     * @param version one of the {@code PdfWriter.VERSION_*} constants
     * @return this builder
     */
    public FormFixture pdfVersion(String version) {
        this.pdfVersion = version;
        return this;
    }

    /**
     * Sets the lock of one of the signature fields.
     *
     * @param signatureField the signature field name
     * @param lock           the lock
     * @return this builder
     */
    public FormFixture lock(String signatureField, SignatureFieldLock lock) {
        locks.put(signatureField, lock);
        return this;
    }

    /**
     * Builds the document.
     *
     * @return the PDF document
     * @throws Exception on error
     */
    public byte[] build() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, out);
        writer.setPdfVersion(pdfVersion);
        document.open();
        document.add(new Paragraph("OpenPDF signature lock fixture"));
        addTextField(writer, NOME, 700);
        addTextField(writer, IMPORTO, 660);
        addTextField(writer, NOTE, 620);
        PdfAcroForm acroForm = writer.getAcroForm();
        acroForm.addSignature(FIRMA_A, 50, 450, 250, 520, locks.get(FIRMA_A));
        acroForm.addSignature(FIRMA_B, 300, 450, 500, 520, locks.get(FIRMA_B));
        acroForm.addSignature(FIRMA_C, 50, 350, 250, 420, locks.get(FIRMA_C));
        acroForm.addSignature(FIRMA_INV, 0, 0, 0, 0, locks.get(FIRMA_INV));
        document.close();
        return out.toByteArray();
    }

    private static void addTextField(PdfWriter writer, String name, float y) throws Exception {
        TextField field = new TextField(writer, new Rectangle(150, y, 450, y + 24), name);
        field.setBorderColor(java.awt.Color.BLACK);
        writer.addAnnotation(field.getTextField());
    }
}
