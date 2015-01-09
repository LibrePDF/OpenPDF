package old.org.bouncycastle.mail.smime.examples;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.Properties;

import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;

import old.org.bouncycastle.cms.RecipientId;
import old.org.bouncycastle.cms.RecipientInformation;
import old.org.bouncycastle.cms.RecipientInformationStore;
import old.org.bouncycastle.cms.jcajce.JceKeyTransEnvelopedRecipient;
import old.org.bouncycastle.cms.jcajce.JceKeyTransRecipientId;
import old.org.bouncycastle.mail.smime.SMIMEEnveloped;
import old.org.bouncycastle.mail.smime.SMIMEUtil;

/**
 * a simple example that reads an encrypted email.
 * <p>
 * The key store can be created using the class in
 * org.bouncycastle.jce.examples.PKCS12Example - the program expects only one
 * key to be present.
 */
public class ReadEncryptedMail
{
    public static void main(
        String args[])
        throws Exception
    {
        if (args.length != 2)
        {
            System.err.println("usage: ReadEncryptedMail pkcs12Keystore password");
            System.exit(0);
        }

        //
        // Open the key store
        //
        KeyStore    ks = KeyStore.getInstance("PKCS12", "BC");

        ks.load(new FileInputStream(args[0]), args[1].toCharArray());

        Enumeration e = ks.aliases();
        String      keyAlias = null;

        while (e.hasMoreElements())
        {
            String  alias = (String)e.nextElement();

            if (ks.isKeyEntry(alias))
            {
                keyAlias = alias;
            }
        }

        if (keyAlias == null)
        {
            System.err.println("can't find a private key!");
            System.exit(0);
        }

        //
        // find the certificate for the private key and generate a 
        // suitable recipient identifier.
        //
        X509Certificate cert = (X509Certificate)ks.getCertificate(keyAlias);
        RecipientId     recId = new JceKeyTransRecipientId(cert);

        //
        // Get a Session object with the default properties.
        //         
        Properties props = System.getProperties();

        Session session = Session.getDefaultInstance(props, null);

        MimeMessage msg = new MimeMessage(session, new FileInputStream("encrypted.message"));

        SMIMEEnveloped       m = new SMIMEEnveloped(msg);

        RecipientInformationStore   recipients = m.getRecipientInfos();
        RecipientInformation        recipient = recipients.get(recId);

        MimeBodyPart        res = SMIMEUtil.toMimeBodyPart(recipient.getContent(new JceKeyTransEnvelopedRecipient((PrivateKey)ks.getKey(keyAlias, null)).setProvider("BC")));

        System.out.println("Message Contents");
        System.out.println("----------------");
        System.out.println(res.getContent());
    }
}
