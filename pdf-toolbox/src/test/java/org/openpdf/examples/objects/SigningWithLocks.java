package org.openpdf.examples.objects;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.openpdf.text.Document;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.PdfAcroForm;
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
import org.openpdf.text.pdf.TextField;
import org.openpdf.text.pdf.mdp.DocumentValidationReport;
import org.openpdf.text.pdf.mdp.SignatureValidationResult;
import org.openpdf.text.pdf.mdp.SignatureValidator;

/**
 * Serial signatures with field locks.
 *
 * <ol>
 *     <li>A form is created with the text fields {@code Name} and {@code Notes} and two empty signature fields. The
 *     first signature field locks {@code Name} once it is signed.</li>
 *     <li>The first signer certifies the document (form filling allowed) by signing the first field.</li>
 *     <li>{@code Notes} is filled in a new revision, then the second signer signs the second field.</li>
 *     <li>The signatures are validated revision by revision, and then again after {@code Name} is changed, which
 *     violates the lock of the first signature.</li>
 * </ol>
 *
 * <p>The example uses self-signed keys generated on the fly with Bouncy Castle.
 */
public class SigningWithLocks {

    private static final int CONTENTS_SIZE = 8192;

    /**
     * Runs the example.
     *
     * @param args not used
     * @throws Exception on error
     */
    public static void main(String[] args) throws Exception {
        byte[] pdf = createForm();
        pdf = sign(pdf, "Approval1", "First signer", PdfSignatureAppearance.CERTIFIED_FORM_FILLING);
        pdf = fill(pdf, "Notes", "Filled after the certification");
        pdf = sign(pdf, "Approval2", "Second signer", PdfSignatureAppearance.NOT_CERTIFIED);
        try (OutputStream out = new FileOutputStream("SigningWithLocks.pdf")) {
            out.write(pdf);
        }
        System.out.println("After the allowed changes:");
        print(pdf);

        System.out.println("After changing the locked field Name:");
        print(fill(pdf, "Name", "Changed"));
    }

    private static byte[] createForm() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, out);
        document.open();
        document.add(new Paragraph("Serial signatures with field locks"));
        writer.addAnnotation(new TextField(writer, new Rectangle(100, 700, 400, 724), "Name").getTextField());
        writer.addAnnotation(new TextField(writer, new Rectangle(100, 660, 400, 684), "Notes").getTextField());
        PdfAcroForm form = writer.getAcroForm();
        form.addSignature("Approval1", 100, 500, 300, 580, SignatureFieldLock.include("Name"));
        form.addSignature("Approval2", 320, 500, 520, 580);
        document.close();
        return out.toByteArray();
    }

    private static byte[] fill(byte[] pdf, String field, String value) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (PdfReader reader = new PdfReader(pdf)) {
            PdfStamper stamper = new PdfStamper(reader, out, null, true);
            stamper.getAcroFields().setField(field, value);
            stamper.close();
        }
        return out.toByteArray();
    }

    private static byte[] sign(byte[] pdf, String field, String signer, int certificationLevel) throws Exception {
        KeyPair keys = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        Certificate[] chain = {selfSigned(keys, signer)};
        Calendar signDate = Calendar.getInstance();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (PdfReader reader = new PdfReader(pdf)) {
            PdfStamper stamper = PdfStamper.createSignature(reader, out, null, null, true);
            PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
            appearance.setCrypto(null, chain, null, null);
            // the lock of an existing field is read from the field and becomes a FieldMDP transform
            appearance.setVisibleSignature(field);
            appearance.setCertificationLevel(certificationLevel);
            appearance.setSignDate(signDate);
            PdfDictionary cryptoDictionary = new PdfDictionary(PdfName.SIG);
            cryptoDictionary.put(PdfName.FILTER, PdfName.ADOBE_PPKLITE);
            cryptoDictionary.put(PdfName.SUBFILTER, PdfName.ADBE_PKCS7_DETACHED);
            cryptoDictionary.put(PdfName.M, new PdfDate(signDate));
            appearance.setCryptoDictionary(cryptoDictionary);
            Map<PdfName, Integer> exclusions = new HashMap<>();
            exclusions.put(PdfName.CONTENTS, CONTENTS_SIZE * 2 + 2);
            appearance.preClose(exclusions);

            byte[] hash = sha256(appearance.getRangeStream());
            PdfPKCS7 pkcs7 = new PdfPKCS7(keys.getPrivate(), chain, null, "SHA-256", null, false);
            byte[] attributes = pkcs7.getAuthenticatedAttributeBytes(hash, signDate, null);
            pkcs7.update(attributes, 0, attributes.length);
            byte[] encoded = pkcs7.getEncodedPKCS7(hash, signDate);
            PdfDictionary update = new PdfDictionary();
            update.put(PdfName.CONTENTS, new PdfString(Arrays.copyOf(encoded, CONTENTS_SIZE)).setHexWriting(true));
            appearance.close(update);
        }
        return out.toByteArray();
    }

    private static void print(byte[] pdf) throws Exception {
        try (PdfReader reader = new PdfReader(pdf)) {
            DocumentValidationReport report = new SignatureValidator().validate(reader);
            for (SignatureValidationResult signature : report.signatures()) {
                System.out.println("  " + signature.fieldName() + ": " + signature.status());
                signature.violations().forEach(violation -> System.out.println("    " + violation.message()
                        + " on " + violation.fieldName() + ", violates " + violation.constraint()));
            }
        }
    }

    private static X509Certificate selfSigned(KeyPair keys, String name) throws Exception {
        X500Name subject = new X500Name("CN=" + name);
        long now = System.currentTimeMillis();
        JcaX509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(subject, BigInteger.valueOf(now),
                new Date(now - 60_000), new Date(now + 86_400_000L), subject, keys.getPublic());
        return new JcaX509CertificateConverter()
                .getCertificate(builder.build(new JcaContentSignerBuilder("SHA256withRSA").build(keys.getPrivate())));
    }

    private static byte[] sha256(InputStream data) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] buffer = new byte[8192];
        int read;
        while ((read = data.read(buffer)) > 0) {
            digest.update(buffer, 0, read);
        }
        return digest.digest();
    }
}
