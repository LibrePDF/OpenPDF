package com.lowagie.bouncycastle;

import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.pdf.PdfArray;
import com.lowagie.text.pdf.PdfObject;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.CMSEnvelopedData;
import org.bouncycastle.cms.Recipient;
import org.bouncycastle.cms.RecipientInformation;
import org.bouncycastle.cms.jcajce.JceKeyTransEnvelopedRecipient;

import java.io.IOException;
import java.security.Key;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.Collection;
import java.util.List;

public class BouncyCastleHelper {
    public static void checkCertificateEncodingOrThrowException(Certificate certificate) {
        // OJO...
        try {
            new X509CertificateHolder(certificate.getEncoded());
        } catch (CertificateEncodingException | IOException f) {
            throw new ExceptionConverter(f);
        }
        // ******************************************************************************
    }

    @SuppressWarnings("unchecked")
    public static byte[] getEnvelopedData(PdfArray recipients, List<PdfObject> strings, Certificate certificate, Key certificateKey, String certificateKeyProvider) {
        byte[] envelopedData = null;
        for (PdfObject recipient : recipients.getElements()) {
            strings.remove(recipient);
            try {
                CMSEnvelopedData data = new CMSEnvelopedData(recipient.getBytes());

                final Collection<RecipientInformation> recipientInformations = data.getRecipientInfos().getRecipients();
                for (RecipientInformation recipientInfo : recipientInformations) {
                    if (recipientInfo.getRID().match(certificate)) {
                        // OJO...
                        // https://www.bouncycastle.org/docs/pkixdocs1.5on/org/bouncycastle/cms/CMSEnvelopedData.html
                        Recipient rec = new JceKeyTransEnvelopedRecipient(
                                (PrivateKey) certificateKey)
                                .setProvider(certificateKeyProvider);
                        envelopedData = recipientInfo.getContent(rec);
                        // ******************************************************************************
                        break;
                    }

                }
            } catch (Exception f) {
                throw new ExceptionConverter(f);
            }
        }
        return envelopedData;
    }
}
