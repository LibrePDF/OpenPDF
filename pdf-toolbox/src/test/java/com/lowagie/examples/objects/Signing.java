package com.lowagie.examples.objects;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.PdfDate;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfPKCS7;
import com.lowagie.text.pdf.PdfPKCS7.X509Name;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfSignatureAppearance;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.PdfString;
import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Signing {

    public static void main(String[] args) {
        try {
            addUnverifiedSignature(true);
            addUnverifiedSignature(false);
            extractVerifiedCryptoSignature();

        } catch (DocumentException e) {
            System.err.println(e.getMessage());
        }
    }

    private static void addUnverifiedSignature(boolean visible) {
        try {
            String visibility = visible ? "visible" : "invisible";
            String description = "Document with " + visibility + " signature";
            System.out.println(description);

            Document document = new Document();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            document.open();
            writer.getInfo().put(PdfName.CREATOR, new PdfString(Document.getVersion()));

            document.add(new Paragraph(description));
            document.close();

            PdfReader reader = new PdfReader(baos.toByteArray());
            // A verified signature would require a private key plus a valid certificate. see the JavaDoc of this
            // method for details
            PdfStamper stp = PdfStamper.createSignature(reader, baos, '\0', null, true);

            Calendar signDate = Calendar.getInstance();
            stp.setEnforcedModificationDate(signDate);

            PdfSignatureAppearance sap = stp.getSignatureAppearance();
            PdfDictionary dic = new PdfDictionary();
            // self signed
            dic.put(PdfName.FILTER, PdfName.ADOBE_PPKLITE);
            dic.put(PdfName.M, new PdfDate(signDate));
            sap.setCryptoDictionary(dic);
            sap.setSignDate(signDate);

            if (visible) {
                sap.setVisibleSignature(new Rectangle(100, 100), 1);
                sap.setLayer2Text("Test signer");
            }

            // exclude the signature from the hash of the PDF and fill the resulting gap
            Map<PdfName, Integer> exc = new HashMap<>();
            exc.put(PdfName.CONTENTS, 10);
            sap.preClose(exc);
            PdfDictionary update = new PdfDictionary();
            update.put(PdfName.CONTENTS, new PdfString("aaaa").setHexWriting(true));
            sap.close(update);

            String fileNamePrefix = visibility.substring(0, 1).toUpperCase() + visibility.substring(1);
            FileOutputStream fos = new FileOutputStream(fileNamePrefix + "Signature.pdf");
            fos.write(baos.toByteArray());
            fos.close();

            InputStream resultIS = new ByteArrayInputStream(baos.toByteArray());
            PdfReader resultReader = new PdfReader(resultIS);

            AcroFields fields = resultReader.getAcroFields();

            List<String> signatures = fields.getSignedFieldNames();
            for (String signature : signatures) {
                printSignatureDetails(fields, signature);
            }
        } catch (DocumentException | IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private static void extractVerifiedCryptoSignature() {

        System.out.println("Signature extraction");

        PdfPKCS7.loadCacertsKeyStore();

        try {
            InputStream is = Signing.class.getResourceAsStream("/CryptoSignedSha256.pdf");
            PdfReader reader = new PdfReader(is);
            AcroFields fields = reader.getAcroFields();

            List<String> signatures = fields.getSignedFieldNames();
            for (String signature : signatures) {
                printSignatureDetails(fields, signature);

                PdfPKCS7 pk = fields.verifySignature(signature);

                X509Certificate certificate = pk.getSigningCertificate();
                X509Name subjectFields = PdfPKCS7.getSubjectFields(certificate);
                System.out.println("Certificate subject fields: " + subjectFields);
                System.out.println("Certificate verified: " + pk.verify());

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                System.out.println("Date signed: " + sdf.format(pk.getSignDate().getTime()));
                System.out.println("Timestamp verified: " + pk.verifyTimestampImprint());
            }
        } catch (SignatureException | IOException | NoSuchAlgorithmException e) {
            System.err.println(e.getMessage());
        }
    }

    private static void printSignatureDetails(AcroFields fields, String signature) {
        System.out.println("Signature: " + signature);
        System.out.println("Signature covers whole document: " + fields.signatureCoversWholeDocument(signature));
        System.out.println("Revision: " + fields.getRevision(signature));
    }
}