package old.org.bouncycastle.mail.smime.examples;

import java.io.FileInputStream;
import java.util.Properties;

import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;

import old.org.bouncycastle.mail.smime.SMIMECompressed;
import old.org.bouncycastle.mail.smime.SMIMEUtil;

/**
 * a simple example that reads a compressed email.
 * <p>
 */
public class ReadCompressedMail
{
    public static void main(
        String args[])
        throws Exception
    {
        //
        // Get a Session object with the default properties.
        //         
        Properties props = System.getProperties();

        Session session = Session.getDefaultInstance(props, null);

        MimeMessage msg = new MimeMessage(session, new FileInputStream("compressed.message"));

        SMIMECompressed     m = new SMIMECompressed(msg);

        MimeBodyPart        res = SMIMEUtil.toMimeBodyPart(m.getContent());

        System.out.println("Message Contents");
        System.out.println("----------------");
        System.out.println(res.getContent());
    }
}
