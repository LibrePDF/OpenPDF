package old.org.bouncycastle.mail.smime;

import java.io.IOException;
import java.io.OutputStream;

import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;

import old.org.bouncycastle.cms.CMSCompressedDataGenerator;
import old.org.bouncycastle.cms.CMSCompressedDataStreamGenerator;

/**
 * General class for generating a pkcs7-mime compressed message.
 *
 * A simple example of usage.
 *
 * <pre>
 *      SMIMECompressedGenerator  fact = new SMIMECompressedGenerator();
 *
 *      MimeBodyPart           smime = fact.generate(content, algorithm);
 * </pre>
 *
 * <b>Note:<b> Most clients expect the MimeBodyPart to be in a MimeMultipart
 * when it's sent.
 */
public class SMIMECompressedGenerator
    extends SMIMEGenerator
{
    public static final String  ZLIB    = CMSCompressedDataGenerator.ZLIB;

    private static final String COMPRESSED_CONTENT_TYPE = "application/pkcs7-mime; name=\"smime.p7z\"; smime-type=compressed-data";

    static
    {
        MailcapCommandMap mc = (MailcapCommandMap)CommandMap.getDefaultCommandMap();

        mc.addMailcap("application/pkcs7-mime;; x-java-content-handler=org.bouncycastle.mail.smime.handlers.pkcs7_mime");
        mc.addMailcap("application/x-pkcs7-mime;; x-java-content-handler=org.bouncycastle.mail.smime.handlers.x_pkcs7_mime");

        CommandMap.setDefaultCommandMap(mc);
    }

    /**
     * generate an compressed object that contains an SMIME Compressed
     * object using the given compression algorithm.
     */
    private MimeBodyPart make(
        MimeBodyPart    content,
        String          compressionOID)
        throws SMIMEException
    {
        try
        {  
            MimeBodyPart data = new MimeBodyPart();
        
            data.setContent(new ContentCompressor(content, compressionOID), COMPRESSED_CONTENT_TYPE);
            data.addHeader("Content-Type", COMPRESSED_CONTENT_TYPE);
            data.addHeader("Content-Disposition", "attachment; filename=\"smime.p7z\"");
            data.addHeader("Content-Description", "S/MIME Compressed Message");
            data.addHeader("Content-Transfer-Encoding", encoding);

            return data;
        }
        catch (MessagingException e)
        {
            throw new SMIMEException("exception putting multi-part together.", e);
        }
    }

    /**
     * generate an compressed object that contains an SMIME Compressed
     * object using the given provider from the contents of the passed in
     * message
     */
    public MimeBodyPart generate(
        MimeBodyPart    content,
        String          compressionOID)
        throws SMIMEException
    {
        return make(makeContentBodyPart(content), compressionOID);
    }

    /**
     * generate an compressed object that contains an SMIME Compressed
     * object using the given provider from the contents of the passed in
     * message
     */
    public MimeBodyPart generate(
        MimeMessage     message,
        String          compressionOID)
        throws SMIMEException
    {
        try
        {
            message.saveChanges();      // make sure we're up to date.
        }
        catch (MessagingException e)
        {
            throw new SMIMEException("unable to save message", e);
        }
                        
        return make(makeContentBodyPart(message), compressionOID);
    }
    
    private class ContentCompressor
        implements SMIMEStreamingProcessor
    {
        private final MimeBodyPart _content;
        private final String _compressionOid;
        
        ContentCompressor(
            MimeBodyPart content,
            String       compressionOid)
        {
            _content = content;
            _compressionOid = compressionOid;
        }

        public void write(OutputStream out)
            throws IOException
        {
            CMSCompressedDataStreamGenerator cGen = new CMSCompressedDataStreamGenerator();
            
            OutputStream compressed = cGen.open(out, _compressionOid);
            
            try
            {
                _content.writeTo(compressed);
                
                compressed.close();
            }
            catch (MessagingException e)
            {
                throw new IOException(e.toString());
            }
        }
    }
}
