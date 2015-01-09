package old.org.bouncycastle.mail.smime;

import old.org.bouncycastle.cms.CMSException;
import old.org.bouncycastle.cms.CMSProcessable;
import old.org.bouncycastle.cms.CMSSignedData;

import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimePart;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * general class for handling a pkcs7-signature message.
 * <p>
 * A simple example of usage - note, in the example below the validity of
 * the certificate isn't verified, just the fact that one of the certs 
 * matches the given signer...
 * <p>
 * <pre>
 *  CertStore               certs = s.getCertificates("Collection", "BC");
 *  SignerInformationStore  signers = s.getSignerInfos();
 *  Collection              c = signers.getSigners();
 *  Iterator                it = c.iterator();
 *  
 *  while (it.hasNext())
 *  {
 *      SignerInformation   signer = (SignerInformation)it.next();
 *      Collection          certCollection = certs.getCertificates(signer.getSID());
 *  
 *      Iterator        certIt = certCollection.iterator();
 *      X509Certificate cert = (X509Certificate)certIt.next();
 *  
 *      if (signer.verify(cert.getPublicKey()))
 *      {
 *          verified++;
 *      }   
 *  }
 * </pre>
 * <p>
 * Note: if you are using this class with AS2 or some other protocol
 * that does not use 7bit as the default content transfer encoding you
 * will need to use the constructor that allows you to specify the default
 * content transfer encoding, such as "binary".
 * </p>
 */
public class SMIMESigned
    extends CMSSignedData
{
    Object                  message;
    MimeBodyPart            content;

    private static InputStream getInputStream(
        Part    bodyPart)
        throws MessagingException
    {
        try
        {
            if (bodyPart.isMimeType("multipart/signed"))
            {
                throw new MessagingException("attempt to create signed data object from multipart content - use MimeMultipart constructor.");
            }
            
            return bodyPart.getInputStream();
        }
        catch (IOException e)
        {
            throw new MessagingException("can't extract input stream: " + e);
        }
    }

    static
    {
        MailcapCommandMap mc = (MailcapCommandMap)CommandMap.getDefaultCommandMap();

        mc.addMailcap("application/pkcs7-signature;; x-java-content-handler=org.bouncycastle.mail.smime.handlers.pkcs7_signature");
        mc.addMailcap("application/pkcs7-mime;; x-java-content-handler=org.bouncycastle.mail.smime.handlers.pkcs7_mime");
        mc.addMailcap("application/x-pkcs7-signature;; x-java-content-handler=org.bouncycastle.mail.smime.handlers.x_pkcs7_signature");
        mc.addMailcap("application/x-pkcs7-mime;; x-java-content-handler=org.bouncycastle.mail.smime.handlers.x_pkcs7_mime");
        mc.addMailcap("multipart/signed;; x-java-content-handler=org.bouncycastle.mail.smime.handlers.multipart_signed");
        
        CommandMap.setDefaultCommandMap(mc);
    }
    
    /**
     * base constructor using a defaultContentTransferEncoding of 7bit
     *
     * @exception MessagingException on an error extracting the signature or
     * otherwise processing the message.
     * @exception CMSException if some other problem occurs.
     */
    public SMIMESigned(
        MimeMultipart message) 
        throws MessagingException, CMSException
    {
        super(new CMSProcessableBodyPartInbound(message.getBodyPart(0)), getInputStream(message.getBodyPart(1)));

        this.message = message;
        this.content = (MimeBodyPart)message.getBodyPart(0);
    }

    /**
     * base constructor with settable contentTransferEncoding
     *
     * @param message the signed message
     * @param defaultContentTransferEncoding new default to use
     * @exception MessagingException on an error extracting the signature or
     * otherwise processing the message.
     * @exception CMSException if some other problem occurs.
     */
    public SMIMESigned(
        MimeMultipart message,
        String        defaultContentTransferEncoding) 
        throws MessagingException, CMSException
    {
        super(new CMSProcessableBodyPartInbound(message.getBodyPart(0), defaultContentTransferEncoding), getInputStream(message.getBodyPart(1)));

        this.message = message;
        this.content = (MimeBodyPart)message.getBodyPart(0);
    }
    
    /**
     * base constructor for a signed message with encapsulated content.
     *
     * @exception MessagingException on an error extracting the signature or
     * otherwise processing the message.
     * @exception SMIMEException if the body part encapsulated in the message cannot be extracted.
     * @exception CMSException if some other problem occurs.
     */
    public SMIMESigned(
        Part message) 
        throws MessagingException, CMSException, SMIMEException
    {
        super(getInputStream(message));

        this.message = message;

        CMSProcessable  cont = this.getSignedContent();

        if (cont != null)
        {
            byte[]  contBytes = (byte[])cont.getContent();
    
            this.content = SMIMEUtil.toMimeBodyPart(contBytes);
        }
    }

    /**
     * return the content that was signed.
     */
    public MimeBodyPart getContent()
    {
        return content;
    }

    /**
     * Return the content that was signed as a mime message.
     *
     * @param session
     * @return a MimeMessage holding the content.
     * @throws MessagingException
     */
    public MimeMessage getContentAsMimeMessage(Session session)
        throws MessagingException, IOException
    {
        Object content = getSignedContent().getContent();
        byte[] contentBytes = null;

        if (content instanceof byte[])
        {
            contentBytes = (byte[])content;
        }
        else if (content instanceof MimePart)
        {
            MimePart part = (MimePart)content;
            ByteArrayOutputStream out;
            
            if (part.getSize() > 0)
            {
                out = new ByteArrayOutputStream(part.getSize());
            }
            else
            {
                out = new ByteArrayOutputStream();
            }
            
            part.writeTo(out);
            contentBytes = out.toByteArray();
        }
        else
        {
            String type = "<null>";
            if (content != null)
            {
                type = content.getClass().getName();
            }

            throw new MessagingException(
                "Could not transfrom content of type "
                    + type
                    + " into MimeMessage.");
        }

        if (contentBytes != null)
        {
            ByteArrayInputStream in = new ByteArrayInputStream(contentBytes);

            return new MimeMessage(session, in);
        }

        return null;
    }

    /**
     * return the content that was signed - depending on whether this was
     * unencapsulated or not it will return a MimeMultipart or a MimeBodyPart
     */
    public Object getContentWithSignature()
    {
        return message;
    }
}
