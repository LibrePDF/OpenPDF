package org.openpdf.text.pdf.sign;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.PdfDate;
import org.openpdf.text.pdf.PdfDictionary;
import org.openpdf.text.pdf.PdfName;
import org.openpdf.text.pdf.PdfPKCS7;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.PdfSignatureAppearance;
import org.openpdf.text.pdf.PdfStamper;
import org.openpdf.text.pdf.PdfString;
import org.openpdf.text.pdf.PdfWriter;
import org.openpdf.text.pdf.SignatureFieldLock;

/**
 * Signs PDF documents in append mode with a real SHA-256 / RSA {@code adbe.pkcs7.detached} signature, using the
 * external signature API of {@link PdfSignatureAppearance}.
 */
public final class TestSigner {

    private static final int CONTENTS_SIZE = 8192;

    private TestSigner() {
    }

    /**
     * Starts the options for signing an existing, empty signature field.
     *
     * @param fieldName the name of the existing field
     * @return the options
     */
    public static Options existingField(String fieldName) {
        Options options = new Options();
        options.existingField = fieldName;
        return options;
    }

    /**
     * Starts the options for signing a new visible signature field.
     *
     * @param fieldName the name of the new field
     * @param rectangle the position of the field
     * @param page      the page of the field
     * @return the options
     */
    public static Options newVisibleField(String fieldName, Rectangle rectangle, int page) {
        Options options = new Options();
        options.newField = fieldName;
        options.rectangle = rectangle;
        options.page = page;
        return options;
    }

    /**
     * Starts the options for signing a new invisible signature field.
     *
     * @param fieldName the name of the new field
     * @return the options
     */
    public static Options newInvisibleField(String fieldName) {
        Options options = new Options();
        options.newField = fieldName;
        return options;
    }

    /**
     * Signs a document.
     *
     * @param pdf     the document
     * @param options the signing options
     * @return the signed document, with the signature appended as a new revision
     * @throws Exception on error
     */
    public static byte[] sign(byte[] pdf, Options options) throws Exception {
        TestSigningKeys keys = TestSigningKeys.forName(options.signer);
        Calendar signDate = Calendar.getInstance();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (PdfReader reader = new PdfReader(pdf)) {
            PdfStamper stamper = PdfStamper.createSignature(reader, out, null, null, true);
            if (options.pdf20) {
                stamper.getWriter().setPdfVersion(PdfWriter.PDF_VERSION_2_0);
            }
            PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
            appearance.setCrypto(null, keys.getChain(), null, null);
            if (options.existingField != null) {
                appearance.setVisibleSignature(options.existingField);
            } else if (options.rectangle != null) {
                appearance.setVisibleSignature(options.rectangle, options.page, options.newField);
            } else {
                appearance.setFieldNameForInvisibleSignatures(options.newField);
            }
            appearance.setFieldLock(options.lock);
            appearance.setCertificationLevel(options.certificationLevel);
            appearance.setSignatureEvent(options.signatureEvent);
            appearance.setReason("OpenPDF test");
            appearance.setSignDate(signDate);

            PdfDictionary cryptoDictionary = new PdfDictionary(PdfName.SIG);
            cryptoDictionary.put(PdfName.FILTER, PdfName.ADOBE_PPKLITE);
            cryptoDictionary.put(PdfName.SUBFILTER, PdfName.ADBE_PKCS7_DETACHED);
            cryptoDictionary.put(PdfName.M, new PdfDate(signDate));
            appearance.setCryptoDictionary(cryptoDictionary);

            Map<PdfName, Integer> exclusions = new HashMap<>();
            exclusions.put(PdfName.CONTENTS, CONTENTS_SIZE * 2 + 2);
            appearance.preClose(exclusions);

            byte[] hash = digest(appearance.getRangeStream());
            PdfPKCS7 pkcs7 = new PdfPKCS7(keys.getPrivateKey(), keys.getChain(), null, "SHA-256", null, false);
            byte[] attributes = pkcs7.getAuthenticatedAttributeBytes(hash, signDate, null);
            pkcs7.update(attributes, 0, attributes.length);
            byte[] encoded = pkcs7.getEncodedPKCS7(hash, signDate);
            if (encoded.length > CONTENTS_SIZE) {
                throw new IllegalStateException("The signature does not fit the reserved space");
            }
            PdfDictionary update = new PdfDictionary();
            update.put(PdfName.CONTENTS, new PdfString(Arrays.copyOf(encoded, CONTENTS_SIZE)).setHexWriting(true));
            appearance.close(update);
        }
        return out.toByteArray();
    }

    private static byte[] digest(InputStream data) throws Exception {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        byte[] buffer = new byte[8192];
        int read;
        while ((read = data.read(buffer)) > 0) {
            messageDigest.update(buffer, 0, read);
        }
        return messageDigest.digest();
    }

    /**
     * The options of one signature.
     */
    public static final class Options {

        private String existingField;
        private String newField;
        private Rectangle rectangle;
        private int page = 1;
        private String signer = "Signer";
        private int certificationLevel = PdfSignatureAppearance.NOT_CERTIFIED;
        private SignatureFieldLock lock;
        private boolean pdf20;
        private PdfSignatureAppearance.SignatureEvent signatureEvent;

        private Options() {
        }

        /**
         * Sets a hook that edits the signature dictionary before it is written, to build malformed documents.
         *
         * @param signatureEvent the hook
         * @return these options
         */
        public Options signatureEvent(PdfSignatureAppearance.SignatureEvent signatureEvent) {
            this.signatureEvent = signatureEvent;
            return this;
        }

        /**
         * Sets the common name of the signing identity.
         *
         * @param commonName the common name
         * @return these options
         */
        public Options signer(String commonName) {
            this.signer = commonName;
            return this;
        }

        /**
         * Makes the signature a certification signature.
         *
         * @param level one of the {@code PdfSignatureAppearance.CERTIFIED_*} constants
         * @return these options
         */
        public Options certify(int level) {
            this.certificationLevel = level;
            return this;
        }

        /**
         * Sets the lock of a new signature field.
         *
         * @param lock the lock
         * @return these options
         */
        public Options lock(SignatureFieldLock lock) {
            this.lock = lock;
            return this;
        }

        /**
         * Raises the output version to PDF 2.0 in the signature revision.
         *
         * @return these options
         */
        public Options pdf20() {
            this.pdf20 = true;
            return this;
        }
    }
}
