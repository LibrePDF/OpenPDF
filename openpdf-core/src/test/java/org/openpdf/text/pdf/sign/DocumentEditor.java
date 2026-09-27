package org.openpdf.text.pdf.sign;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.PdfAnnotation;
import org.openpdf.text.pdf.PdfArray;
import org.openpdf.text.pdf.PdfDictionary;
import org.openpdf.text.pdf.PdfName;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.PdfStamper;
import org.openpdf.text.pdf.PdfStream;
import org.openpdf.text.pdf.PdfWriter;
import org.openpdf.text.pdf.TextField;

/**
 * Applies incremental (append mode) changes to test documents, including changes that MDP rules forbid.
 */
public final class DocumentEditor {

    private DocumentEditor() {
    }

    /**
     * Fills a text field in a new revision.
     *
     * @param pdf   the document
     * @param field the fully qualified field name
     * @param value the new value
     * @return the updated document
     * @throws Exception on error
     */
    public static byte[] fillField(byte[] pdf, String field, String value) throws Exception {
        return edit(pdf, stamper -> {
            if (!stamper.getAcroFields().setField(field, value)) {
                throw new IllegalArgumentException("Cannot fill the field " + field);
            }
        });
    }

    /**
     * Adds a new text field in a new revision.
     *
     * @param pdf  the document
     * @param name the name of the new field
     * @return the updated document
     * @throws Exception on error
     */
    public static byte[] addTextField(byte[] pdf, String name) throws Exception {
        return edit(pdf, stamper -> {
            TextField field = new TextField(stamper.getWriter(), new Rectangle(150, 250, 450, 274), name);
            stamper.addAnnotation(field.getTextField(), 1);
        });
    }

    /**
     * Adds a text annotation (a comment) in a new revision.
     *
     * @param pdf the document
     * @return the updated document
     * @throws Exception on error
     */
    public static byte[] addComment(byte[] pdf) throws Exception {
        return edit(pdf, stamper -> {
            PdfAnnotation comment = PdfAnnotation.createText(stamper.getWriter(),
                    new Rectangle(500, 780, 520, 800), "Reviewer", "A comment", false, "Comment");
            stamper.addAnnotation(comment, 1);
        });
    }

    /**
     * Adds a document security store (DSS) with one certificate in a new revision.
     *
     * @param pdf the document
     * @return the updated document
     * @throws Exception on error
     */
    public static byte[] addDss(byte[] pdf) throws Exception {
        return edit(pdf, stamper -> {
            PdfWriter writer = stamper.getWriter();
            byte[] certificate = TestSigningKeys.forName("DSS").getChain()[0].getEncoded();
            PdfArray certs = new PdfArray();
            certs.add(writer.addToBody(new PdfStream(certificate)).getIndirectReference());
            PdfDictionary dss = new PdfDictionary(PdfName.DSS);
            dss.put(PdfName.CERTS, certs);
            PdfDictionary catalog = stamper.getReader().getCatalog();
            catalog.put(PdfName.DSS, writer.addToBody(dss).getIndirectReference());
            stamper.markUsed(catalog);
        });
    }

    /**
     * Overwrites one byte of the {@code /CreationDate} of the first revision, which every signature covers.
     *
     * @param pdf the document
     * @return a copy of the document with one byte changed in place
     */
    public static byte[] tamperFirstRevision(byte[] pdf) {
        byte[] copy = Arrays.copyOf(pdf, pdf.length);
        String text = new String(copy, StandardCharsets.ISO_8859_1);
        int position = text.indexOf("/CreationDate(D:");
        if (position < 0) {
            throw new IllegalStateException("No /CreationDate found");
        }
        int digit = position + "/CreationDate(D:".length();
        copy[digit] = (byte) (copy[digit] == '1' ? '2' : '1');
        return copy;
    }

    /**
     * Writes a test document to {@code target/signature-fixtures} for manual checks with external validators.
     *
     * @param name the file name
     * @param pdf  the document
     * @throws Exception on error
     */
    public static void save(String name, byte[] pdf) throws Exception {
        Path directory = Path.of("target", "signature-fixtures");
        Files.createDirectories(directory);
        Files.write(directory.resolve(name), pdf);
    }

    private static byte[] edit(byte[] pdf, Edit edit) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (PdfReader reader = new PdfReader(pdf)) {
            PdfStamper stamper = new PdfStamper(reader, out, null, true);
            edit.apply(stamper);
            stamper.close();
        }
        return out.toByteArray();
    }

    @FunctionalInterface
    private interface Edit {

        void apply(PdfStamper stamper) throws Exception;
    }
}
