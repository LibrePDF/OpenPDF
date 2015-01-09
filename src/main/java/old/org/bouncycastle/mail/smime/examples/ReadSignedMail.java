package old.org.bouncycastle.mail.smime.examples;

import java.io.FileInputStream;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import old.org.bouncycastle.cert.X509CertificateHolder;
import old.org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import old.org.bouncycastle.cms.SignerInformation;
import old.org.bouncycastle.cms.SignerInformationStore;
import old.org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import old.org.bouncycastle.jce.provider.BouncyCastleProvider;
import old.org.bouncycastle.mail.smime.SMIMESigned;
import old.org.bouncycastle.util.Store;

/**
 * a simple example that reads a basic SMIME signed mail file.
 */
public class ReadSignedMail
{
    private static final String BC = BouncyCastleProvider.PROVIDER_NAME;

    /**
     * verify the signature (assuming the cert is contained in the message)
     */
    private static void verify(
        SMIMESigned s)
        throws Exception
    {
        //
        // extract the information to verify the signatures.
        //

        //
        // certificates and crls passed in the signature
        //
        Store certs = s.getCertificates();

        //
        // SignerInfo blocks which contain the signatures
        //
        SignerInformationStore  signers = s.getSignerInfos();

        Collection              c = signers.getSigners();
        Iterator                it = c.iterator();

        //
        // check each signer
        //
        while (it.hasNext())
        {
            SignerInformation   signer = (SignerInformation)it.next();
            Collection          certCollection = certs.getMatches(signer.getSID());

            Iterator        certIt = certCollection.iterator();
            X509Certificate cert = new JcaX509CertificateConverter().setProvider(BC).getCertificate((X509CertificateHolder)certIt.next());

            //
            // verify that the sig is correct and that it was generated
            // when the certificate was current
            //
            if (signer.verify(new JcaSimpleSignerInfoVerifierBuilder().setProvider(BC).build(cert)))
            {
                System.out.println("signature verified");
            }
            else
            {
                System.out.println("signature failed!");
            }
        }
    }

    public static void main(
        String[]    args)
        throws Exception
    {
        //
        // Get a Session object with the default properties.
        //         
        Properties props = System.getProperties();

        Session session = Session.getDefaultInstance(props, null);

        MimeMessage msg = new MimeMessage(session, new FileInputStream("signed.message"));

        //
        // make sure this was a multipart/signed message - there should be
        // two parts as we have one part for the content that was signed and
        // one part for the actual signature.
        //
        if (msg.isMimeType("multipart/signed"))
        {
            SMIMESigned             s = new SMIMESigned(
                                            (MimeMultipart)msg.getContent());

            //
            // extract the content
            //
            MimeBodyPart            content = s.getContent();

            System.out.println("Content:");

            Object  cont = content.getContent();

            if (cont instanceof String)
            {
                System.out.println((String)cont);
            }
            else if (cont instanceof Multipart)
            {
                Multipart   mp = (Multipart)cont;
                int count = mp.getCount();
                for (int i = 0; i < count; i++)
                {
                    BodyPart    m = mp.getBodyPart(i);
                    Object      part = m.getContent();

                    System.out.println("Part " + i);
                    System.out.println("---------------------------");

                    if (part instanceof String)
                    {
                        System.out.println((String)part);
                    }
                    else
                    {
                        System.out.println("can't print...");
                    }
                }
            }

            System.out.println("Status:");

            verify(s);
        }
        else if (msg.isMimeType("application/pkcs7-mime")
                || msg.isMimeType("application/x-pkcs7-mime"))
        {
            //
            // in this case the content is wrapped in the signature block.
            //
            SMIMESigned             s = new SMIMESigned(msg);

            //
            // extract the content
            //
            MimeBodyPart            content = s.getContent();

            System.out.println("Content:");

            Object  cont = content.getContent();

            if (cont instanceof String)
            {
                System.out.println((String)cont);
            }

            System.out.println("Status:");

            verify(s);
        }
        else
        {
            System.err.println("Not a signed message!");
        }
    }
}
