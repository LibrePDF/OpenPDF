package old.org.bouncycastle.mail.smime.examples;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import old.org.bouncycastle.asn1.ASN1EncodableVector;
import old.org.bouncycastle.asn1.cms.AttributeTable;
import old.org.bouncycastle.asn1.cms.IssuerAndSerialNumber;
import old.org.bouncycastle.asn1.smime.SMIMECapabilitiesAttribute;
import old.org.bouncycastle.asn1.smime.SMIMECapability;
import old.org.bouncycastle.asn1.smime.SMIMECapabilityVector;
import old.org.bouncycastle.asn1.smime.SMIMEEncryptionKeyPreferenceAttribute;
import old.org.bouncycastle.asn1.x500.X500Name;
import old.org.bouncycastle.cert.jcajce.JcaCertStore;
import old.org.bouncycastle.cms.CMSAlgorithm;
import old.org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoGeneratorBuilder;
import old.org.bouncycastle.cms.jcajce.JceCMSContentEncryptorBuilder;
import old.org.bouncycastle.cms.jcajce.JceKeyTransRecipientInfoGenerator;
import old.org.bouncycastle.jce.provider.BouncyCastleProvider;
import old.org.bouncycastle.mail.smime.SMIMEEnvelopedGenerator;
import old.org.bouncycastle.mail.smime.SMIMEException;
import old.org.bouncycastle.mail.smime.SMIMESignedGenerator;
import old.org.bouncycastle.util.Store;
import old.org.bouncycastle.util.Strings;

/**
 * Example that sends a signed and encrypted mail message.
 */
public class SendSignedAndEncryptedMail
{
    public static void main(String args[])
    {
        if (args.length != 5)
        {
            System.err
                    .println("usage: SendSignedAndEncryptedMail <pkcs12Keystore> <password> <keyalias> <smtp server> <email address>");
            System.exit(0);
        }

        try
        {
            MailcapCommandMap mailcap = (MailcapCommandMap)CommandMap
                    .getDefaultCommandMap();

            mailcap
                    .addMailcap("application/pkcs7-signature;; x-java-content-handler=org.bouncycastle.mail.smime.handlers.pkcs7_signature");
            mailcap
                    .addMailcap("application/pkcs7-mime;; x-java-content-handler=org.bouncycastle.mail.smime.handlers.pkcs7_mime");
            mailcap
                    .addMailcap("application/x-pkcs7-signature;; x-java-content-handler=org.bouncycastle.mail.smime.handlers.x_pkcs7_signature");
            mailcap
                    .addMailcap("application/x-pkcs7-mime;; x-java-content-handler=org.bouncycastle.mail.smime.handlers.x_pkcs7_mime");
            mailcap
                    .addMailcap("multipart/signed;; x-java-content-handler=org.bouncycastle.mail.smime.handlers.multipart_signed");

            CommandMap.setDefaultCommandMap(mailcap);

            /* Add BC */
            Security.addProvider(new BouncyCastleProvider());

            /* Open the keystore */
            KeyStore keystore = KeyStore.getInstance("PKCS12", "BC");
            keystore.load(new FileInputStream(args[0]), args[1].toCharArray());
            Certificate[] chain = keystore.getCertificateChain(args[2]);

            /* Get the private key to sign the message with */
            PrivateKey privateKey = (PrivateKey)keystore.getKey(args[2],
                    args[1].toCharArray());
            if (privateKey == null)
            {
                throw new Exception("cannot find private key for alias: "
                        + args[2]);
            }

            /* Create the message to sign and encrypt */
            Properties props = System.getProperties();
            props.put("mail.smtp.host", args[3]);
            Session session = Session.getDefaultInstance(props, null);

            MimeMessage body = new MimeMessage(session);
            body.setFrom(new InternetAddress(args[4]));
            body.setRecipient(Message.RecipientType.TO, new InternetAddress(
                    args[4]));
            body.setSubject("example encrypted message");
            body.setContent("example encrypted message", "text/plain");
            body.saveChanges();

            /* Create the SMIMESignedGenerator */
            SMIMECapabilityVector capabilities = new SMIMECapabilityVector();
            capabilities.addCapability(SMIMECapability.dES_EDE3_CBC);
            capabilities.addCapability(SMIMECapability.rC2_CBC, 128);
            capabilities.addCapability(SMIMECapability.dES_CBC);

            ASN1EncodableVector attributes = new ASN1EncodableVector();
            attributes.add(new SMIMEEncryptionKeyPreferenceAttribute(
                    new IssuerAndSerialNumber(
                            new X500Name(((X509Certificate)chain[0])
                                    .getIssuerDN().getName()),
                            ((X509Certificate)chain[0]).getSerialNumber())));
            attributes.add(new SMIMECapabilitiesAttribute(capabilities));

            SMIMESignedGenerator signer = new SMIMESignedGenerator();
            signer.addSignerInfoGenerator(new JcaSimpleSignerInfoGeneratorBuilder().setProvider("BC").setSignedAttributeGenerator(new AttributeTable(attributes)).build("DSA".equals(privateKey.getAlgorithm()) ? "SHA1withDSA" : "MD5withDSA", privateKey, (X509Certificate)chain[0]));


            /* Add the list of certs to the generator */
            List certList = new ArrayList();
            certList.add(chain[0]);
            Store certs = new JcaCertStore(certList);
            signer.addCertificates(certs);

            /* Sign the message */
            MimeMultipart mm = signer.generate(body, "BC");
            MimeMessage signedMessage = new MimeMessage(session);

            /* Set all original MIME headers in the signed message */
            Enumeration headers = body.getAllHeaderLines();
            while (headers.hasMoreElements())
            {
                signedMessage.addHeaderLine((String)headers.nextElement());
            }

            /* Set the content of the signed message */
            signedMessage.setContent(mm);
            signedMessage.saveChanges();

            /* Create the encrypter */
            SMIMEEnvelopedGenerator encrypter = new SMIMEEnvelopedGenerator();
            encrypter.addRecipientInfoGenerator(new JceKeyTransRecipientInfoGenerator((X509Certificate)chain[0]).setProvider("BC"));

            /* Encrypt the message */
            MimeBodyPart encryptedPart = encrypter.generate(signedMessage,
                    new JceCMSContentEncryptorBuilder(CMSAlgorithm.RC2_CBC).setProvider("BC").build());

            /*
             * Create a new MimeMessage that contains the encrypted and signed
             * content
             */
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            encryptedPart.writeTo(out);

            MimeMessage encryptedMessage = new MimeMessage(session,
                    new ByteArrayInputStream(out.toByteArray()));

            /* Set all original MIME headers in the encrypted message */
            headers = body.getAllHeaderLines();
            while (headers.hasMoreElements())
            {
                String headerLine = (String)headers.nextElement();
                /*
                 * Make sure not to override any content-* headers from the
                 * original message
                 */
                if (!Strings.toLowerCase(headerLine).startsWith("content-"))
                {
                    encryptedMessage.addHeaderLine(headerLine);
                }
            }

            Transport.send(encryptedMessage);
        }
        catch (SMIMEException ex)
        {
            ex.getUnderlyingException().printStackTrace(System.err);
            ex.printStackTrace(System.err);
        }
        catch (Exception ex)
        {
            ex.printStackTrace(System.err);
        }
    }
}
