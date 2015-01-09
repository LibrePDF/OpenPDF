package old.org.bouncycastle.mail.smime;

import java.io.IOException;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;
import javax.crypto.SecretKey;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;

import old.org.bouncycastle.asn1.ASN1EncodableVector;
import old.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import old.org.bouncycastle.cms.CMSEnvelopedDataGenerator;
import old.org.bouncycastle.cms.CMSEnvelopedDataStreamGenerator;
import old.org.bouncycastle.cms.CMSException;
import old.org.bouncycastle.cms.RecipientInfoGenerator;
import old.org.bouncycastle.cms.jcajce.JceCMSContentEncryptorBuilder;
import old.org.bouncycastle.cms.jcajce.JceKEKRecipientInfoGenerator;
import old.org.bouncycastle.cms.jcajce.JceKeyAgreeRecipientInfoGenerator;
import old.org.bouncycastle.cms.jcajce.JceKeyTransRecipientInfoGenerator;
import old.org.bouncycastle.operator.OperatorCreationException;
import old.org.bouncycastle.operator.OutputEncryptor;

/**
 * General class for generating a pkcs7-mime message.
 *
 * A simple example of usage.
 *
 * <pre>
 *      SMIMEEnvelopedGenerator  fact = new SMIMEEnvelopedGenerator();
 *
 *      fact.addRecipientInfoGenerator(new JceKeyTransRecipientInfoGenerator(recipientCert).setProvider("BC"));
 *
 *      MimeBodyPart mp = fact.generate(content, new JceCMSContentEncryptorBuilder(CMSAlgorithm.RC2_CBC, 40).setProvider("BC").build());
 * </pre>
 *
 * <b>Note:<b> Most clients expect the MimeBodyPart to be in a MimeMultipart
 * when it's sent.
 */
public class SMIMEEnvelopedGenerator
    extends SMIMEGenerator
{
    public static final String  DES_EDE3_CBC    = CMSEnvelopedDataGenerator.DES_EDE3_CBC;
    public static final String  RC2_CBC         = CMSEnvelopedDataGenerator.RC2_CBC;
    public static final String  IDEA_CBC        = CMSEnvelopedDataGenerator.IDEA_CBC;
    public static final String  CAST5_CBC       = CMSEnvelopedDataGenerator.CAST5_CBC;

    public static final String  AES128_CBC      = CMSEnvelopedDataGenerator.AES128_CBC;
    public static final String  AES192_CBC      = CMSEnvelopedDataGenerator.AES192_CBC;
    public static final String  AES256_CBC      = CMSEnvelopedDataGenerator.AES256_CBC;

    public static final String  CAMELLIA128_CBC = CMSEnvelopedDataGenerator.CAMELLIA128_CBC;
    public static final String  CAMELLIA192_CBC = CMSEnvelopedDataGenerator.CAMELLIA192_CBC;
    public static final String  CAMELLIA256_CBC = CMSEnvelopedDataGenerator.CAMELLIA256_CBC;

    public static final String  SEED_CBC        = CMSEnvelopedDataGenerator.SEED_CBC;

    public static final String  DES_EDE3_WRAP   = CMSEnvelopedDataGenerator.DES_EDE3_WRAP;
    public static final String  AES128_WRAP     = CMSEnvelopedDataGenerator.AES128_WRAP;
    public static final String  AES256_WRAP     = CMSEnvelopedDataGenerator.AES256_WRAP;
    public static final String  CAMELLIA128_WRAP = CMSEnvelopedDataGenerator.CAMELLIA128_WRAP;
    public static final String  CAMELLIA192_WRAP = CMSEnvelopedDataGenerator.CAMELLIA192_WRAP;
    public static final String  CAMELLIA256_WRAP = CMSEnvelopedDataGenerator.CAMELLIA256_WRAP;
    public static final String  SEED_WRAP       = CMSEnvelopedDataGenerator.SEED_WRAP;
    
    public static final String  ECDH_SHA1KDF    = CMSEnvelopedDataGenerator.ECDH_SHA1KDF;

    private static final String ENCRYPTED_CONTENT_TYPE = "application/pkcs7-mime; name=\"smime.p7m\"; smime-type=enveloped-data";
    
    private EnvelopedGenerator fact;
    private List               recipients = new ArrayList();

    static
    {
        CommandMap.setDefaultCommandMap(addCommands(CommandMap.getDefaultCommandMap()));
    }

    private static MailcapCommandMap addCommands(CommandMap cm)
    {
        MailcapCommandMap mc = (MailcapCommandMap)cm;

        mc.addMailcap("application/pkcs7-signature;; x-java-content-handler=org.bouncycastle.mail.smime.handlers.pkcs7_signature");
        mc.addMailcap("application/pkcs7-mime;; x-java-content-handler=org.bouncycastle.mail.smime.handlers.pkcs7_mime");
        mc.addMailcap("application/x-pkcs7-signature;; x-java-content-handler=org.bouncycastle.mail.smime.handlers.x_pkcs7_signature");
        mc.addMailcap("application/x-pkcs7-mime;; x-java-content-handler=org.bouncycastle.mail.smime.handlers.x_pkcs7_mime");
        mc.addMailcap("multipart/signed;; x-java-content-handler=org.bouncycastle.mail.smime.handlers.multipart_signed");

        return mc;
    }

    /**
     * base constructor
     */
    public SMIMEEnvelopedGenerator()
    {
        fact = new EnvelopedGenerator();
    }

    /**
     * add a recipientInfoGenerator.
     */
    public void addRecipientInfoGenerator(
        RecipientInfoGenerator recipientInfoGen)
        throws IllegalArgumentException
    {
        fact.addRecipientInfoGenerator(recipientInfoGen);
    }

    /**
     * add a recipient.
     * @deprecated use addRecipientInfoGenerator()
     */
    public void addKeyTransRecipient(
        X509Certificate cert)
        throws IllegalArgumentException
    {
        try
        {
            JceKeyTransRecipientInfoGenerator infoGenerator = new JceKeyTransRecipientInfoGenerator(cert);
            recipients.add(infoGenerator);
            fact.addRecipientInfoGenerator(infoGenerator);
        }
        catch (CertificateEncodingException e)
        {
            throw new IllegalArgumentException(e.toString());
        }
    }

    /**
     * add a recipient - note: this will only work on V3 and later clients.
     *
     * @param key the recipient's public key
     * @param subKeyId the subject key id for the recipient's public key
     * @deprecated use addRecipientInfoGenerator()
     */
    public void addKeyTransRecipient(
        PublicKey   key,
        byte[]      subKeyId)
        throws IllegalArgumentException
    {
        JceKeyTransRecipientInfoGenerator infoGenerator = new JceKeyTransRecipientInfoGenerator(subKeyId, key);
        recipients.add(infoGenerator);
        fact.addRecipientInfoGenerator(infoGenerator);
    }

    /**
     * add a KEK recipient.
     * @deprecated use addRecipientInfoGenerator()
     */
    public void addKEKRecipient(
        SecretKey   key,
        byte[]      keyIdentifier)
        throws IllegalArgumentException
    {
        JceKEKRecipientInfoGenerator infoGenerator = new JceKEKRecipientInfoGenerator(keyIdentifier, key);
        recipients.add(infoGenerator);
        fact.addRecipientInfoGenerator(infoGenerator);
    }

    /**
     * Add a key agreement based recipient.
     *
     * @param senderPrivateKey private key to initialise sender side of agreement with.
     * @param senderPublicKey sender public key to include with message.
     * @param recipientCert recipient's public key certificate.
     * @param cekWrapAlgorithm OID for key wrapping algorithm to use.
     * @param provider provider to use for the agreement calculation.
     * @deprecated use addRecipientInfoGenerator()
     */
    public void addKeyAgreementRecipient(
        String           agreementAlgorithm,
        PrivateKey       senderPrivateKey,
        PublicKey        senderPublicKey,
        X509Certificate  recipientCert,
        String           cekWrapAlgorithm,
        String           provider)
        throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeyException
    {
        this.addKeyAgreementRecipient(agreementAlgorithm, senderPrivateKey, senderPublicKey, recipientCert, cekWrapAlgorithm, provider);
    }

    /**
     * Add a key agreement based recipient.
     *
     * @param senderPrivateKey private key to initialise sender side of agreement with.
     * @param senderPublicKey sender public key to include with message.
     * @param recipientCert recipient's public key certificate.
     * @param cekWrapAlgorithm OID for key wrapping algorithm to use.
     * @param provider provider to use for the agreement calculation.
     * @deprecated use addRecipientInfoGenerator()
     */
    public void addKeyAgreementRecipient(
        String           agreementAlgorithm,
        PrivateKey       senderPrivateKey,
        PublicKey        senderPublicKey,
        X509Certificate  recipientCert,
        String           cekWrapAlgorithm,
        Provider         provider)
        throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeyException
    {
        try
        {
            JceKeyAgreeRecipientInfoGenerator infoGenerator = new JceKeyAgreeRecipientInfoGenerator(new ASN1ObjectIdentifier(agreementAlgorithm), senderPrivateKey, senderPublicKey, new ASN1ObjectIdentifier(cekWrapAlgorithm));

            infoGenerator.addRecipient(recipientCert);

            if (provider != null)
            {
                infoGenerator.setProvider(provider);
            }

            fact.addRecipientInfoGenerator(infoGenerator);
        }
        catch (CertificateEncodingException e)
        {
            throw new NoSuchAlgorithmException("cannot set up generator: " + e);
        }
        catch (CMSException e)
        {
            throw new NoSuchAlgorithmException("cannot set up generator: " + e);
        }
    }

    /**
     * Use a BER Set to store the recipient information
     */
    public void setBerEncodeRecipients(
        boolean berEncodeRecipientSet)
    {
        fact.setBEREncodeRecipients(berEncodeRecipientSet);
    }
    
    /**
     * if we get here we expect the Mime body part to be well defined.
     */
    private MimeBodyPart make(
        MimeBodyPart    content,
        ASN1ObjectIdentifier encryptionOID,
        int             keySize,
        Provider        provider)
        throws NoSuchAlgorithmException, SMIMEException
    {
        //
        // check the base algorithm and provider is available
        //
        createSymmetricKeyGenerator(encryptionOID.getId(), provider);
                
        try
        {  
            MimeBodyPart data = new MimeBodyPart();
        
            data.setContent(new ContentEncryptor(content, encryptionOID, keySize, provider), ENCRYPTED_CONTENT_TYPE);
            data.addHeader("Content-Type", ENCRYPTED_CONTENT_TYPE);
            data.addHeader("Content-Disposition", "attachment; filename=\"smime.p7m\"");
            data.addHeader("Content-Description", "S/MIME Encrypted Message");
            data.addHeader("Content-Transfer-Encoding", encoding);
    
            return data;
        }
        catch (MessagingException e)
        {
            throw new SMIMEException("exception putting S/MIME message together.", e);
        }
        catch (CMSException e)
        {
            throw new SMIMEException("exception putting envelope together.", e);
        }
    }

     /**
     * if we get here we expect the Mime body part to be well defined.
     */
    private MimeBodyPart make(
        MimeBodyPart    content,
        OutputEncryptor encryptor)
        throws SMIMEException
    {
        try
        {
            MimeBodyPart data = new MimeBodyPart();

            data.setContent(new ContentEncryptor(content, encryptor), ENCRYPTED_CONTENT_TYPE);
            data.addHeader("Content-Type", ENCRYPTED_CONTENT_TYPE);
            data.addHeader("Content-Disposition", "attachment; filename=\"smime.p7m\"");
            data.addHeader("Content-Description", "S/MIME Encrypted Message");
            data.addHeader("Content-Transfer-Encoding", encoding);

            return data;
        }
        catch (MessagingException e)
        {
            throw new SMIMEException("exception putting multi-part together.", e);
        }
    }

    /**
     * generate an enveloped object that contains an SMIME Enveloped
     * object using the given content encryptor
     */
    public MimeBodyPart generate(
        MimeBodyPart     content,
        OutputEncryptor  encryptor)
        throws SMIMEException
    {
        return make(makeContentBodyPart(content), encryptor);
    }

    /**
     * generate an enveloped object that contains an SMIME Enveloped
     * object using the given provider from the contents of the passed in
     * message
     */
    public MimeBodyPart generate(
        MimeMessage     message,
        OutputEncryptor  encryptor)
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

        return make(makeContentBodyPart(message), encryptor);
    }

    /**
     * generate an enveloped object that contains an SMIME Enveloped
     * object using the given provider.
     * @deprecated
     */
    public MimeBodyPart generate(
        MimeBodyPart    content,
        String          encryptionOID,
        String          provider)
        throws NoSuchAlgorithmException, NoSuchProviderException, SMIMEException
    {
        return make(makeContentBodyPart(content), new ASN1ObjectIdentifier(encryptionOID), 0, SMIMEUtil.getProvider(provider));
    }

    /**
     * generate an enveloped object that contains an SMIME Enveloped
     * object using the given provider.
     * @deprecated
     */
    public MimeBodyPart generate(
        MimeBodyPart    content,
        String          encryptionOID,
        Provider        provider)
        throws NoSuchAlgorithmException, SMIMEException
    {
        return make(makeContentBodyPart(content), new ASN1ObjectIdentifier(encryptionOID), 0, provider);
    }

    /**
     * generate an enveloped object that contains an SMIME Enveloped
     * object using the given provider from the contents of the passed in
     * message
     * @deprecated
     */
    public MimeBodyPart generate(
        MimeMessage     message,
        String          encryptionOID,
        String          provider)
        throws NoSuchAlgorithmException, NoSuchProviderException, SMIMEException
    {
        return generate(message, encryptionOID, SMIMEUtil.getProvider(provider));
    }

    /**
     * generate an enveloped object that contains an SMIME Enveloped
     * object using the given provider from the contents of the passed in
     * message
     * @deprecated
     */
    public MimeBodyPart generate(
        MimeMessage     message,
        String          encryptionOID,
        Provider        provider)
        throws NoSuchAlgorithmException, NoSuchProviderException, SMIMEException
    {
        try
        {
            message.saveChanges();      // make sure we're up to date.
        }
        catch (MessagingException e)
        {
            throw new SMIMEException("unable to save message", e);
        }
                        
        return make(makeContentBodyPart(message),new ASN1ObjectIdentifier(encryptionOID), 0, provider);
    }

    /**
     * generate an enveloped object that contains an SMIME Enveloped
     * object using the given provider. The size of the encryption key
     * is determined by keysize.
     * @deprecated
     */
    public MimeBodyPart generate(
        MimeBodyPart    content,
        String          encryptionOID,
        int             keySize,
        String          provider)
        throws NoSuchAlgorithmException, NoSuchProviderException, SMIMEException
    {
        return generate(content, encryptionOID, keySize, SMIMEUtil.getProvider(provider));
    }

    /**
     * generate an enveloped object that contains an SMIME Enveloped
     * object using the given provider. The size of the encryption key
     * is determined by keysize.
     * @deprecated
     */
    public MimeBodyPart generate(
        MimeBodyPart    content,
        String          encryptionOID,
        int             keySize,
        Provider        provider)
        throws NoSuchAlgorithmException, NoSuchProviderException, SMIMEException
    {
        return make(makeContentBodyPart(content), new ASN1ObjectIdentifier(encryptionOID), keySize, provider);
    }

    /**
     * generate an enveloped object that contains an SMIME Enveloped
     * object using the given provider from the contents of the passed in
     * message. The size of the encryption key used to protect the message
     * is determined by keysize.
     * @deprecated
     */
    public MimeBodyPart generate(
        MimeMessage     message,
        String          encryptionOID,
        int             keySize,
        String          provider)
        throws NoSuchAlgorithmException, NoSuchProviderException, SMIMEException
    {
        return generate(message, encryptionOID, keySize, SMIMEUtil.getProvider(provider));
    }

    /**
     * generate an enveloped object that contains an SMIME Enveloped
     * object using the given provider from the contents of the passed in
     * message. The size of the encryption key used to protect the message
     * is determined by keysize.
     * @deprecated
     */
    public MimeBodyPart generate(
        MimeMessage     message,
        String          encryptionOID,
        int             keySize,
        Provider        provider)
        throws NoSuchAlgorithmException, SMIMEException
    {
        try
        {
            message.saveChanges();      // make sure we're up to date.
        }
        catch (MessagingException e)
        {
            throw new SMIMEException("unable to save message", e);
        }
                        
        return make(makeContentBodyPart(message), new ASN1ObjectIdentifier(encryptionOID), keySize, provider);
    }
    
    private class ContentEncryptor
        implements SMIMEStreamingProcessor
    {
        private final MimeBodyPart _content;
        private OutputEncryptor _encryptor;

        private boolean _firstTime = true;
        
        ContentEncryptor(
            MimeBodyPart content,
            ASN1ObjectIdentifier       encryptionOid,
            int          keySize,
            Provider     provider)
            throws CMSException
        {
            _content = content;

            if (keySize == 0)  // use the default
            {
                _encryptor = new JceCMSContentEncryptorBuilder(encryptionOid).setProvider(provider).build();
            }
            else
            {
                _encryptor = new JceCMSContentEncryptorBuilder(encryptionOid, keySize).setProvider(provider).build();
            }

            if (provider != null)
            {
                for (Iterator it = recipients.iterator(); it.hasNext();)
                {
                    RecipientInfoGenerator rd = (RecipientInfoGenerator)it.next();

                    try
                    {
                        if (rd instanceof JceKeyTransRecipientInfoGenerator)
                        {
                            ((JceKeyTransRecipientInfoGenerator)rd).setProvider(provider);
                        }
                        else if (rd instanceof JceKEKRecipientInfoGenerator)
                        {
                            ((JceKEKRecipientInfoGenerator)rd).setProvider(provider);
                        }
                    }
                    catch (OperatorCreationException e)
                    {
                        throw new CMSException("cannot create recipient: " + e.getMessage(), e);
                    }
                }
            }
        }

        ContentEncryptor(
            MimeBodyPart content,
            OutputEncryptor encryptor)
        {
            _content = content;
            _encryptor = encryptor;
        }

        public void write(OutputStream out)
            throws IOException
        {
            OutputStream encrypted;
            
            try
            {
                if (_firstTime)
                {
                    encrypted = fact.open(out, _encryptor);
                    
                    _firstTime = false;
                }
                else
                {
                    encrypted = fact.regenerate(out, _encryptor);
                }

                _content.getDataHandler().setCommandMap(addCommands(CommandMap.getDefaultCommandMap()));
                
                _content.writeTo(encrypted);
                
                encrypted.close();
            }
            catch (MessagingException e)
            {
                throw new WrappingIOException(e.toString(), e);
            }
            catch (CMSException e)
            {
                throw new WrappingIOException(e.toString(), e);
            }
        }
    }
    
    private class EnvelopedGenerator
        extends CMSEnvelopedDataStreamGenerator
    {
        private ASN1ObjectIdentifier dataType;
        private ASN1EncodableVector  recipientInfos;

        protected OutputStream open(
            ASN1ObjectIdentifier dataType,
            OutputStream         out,
            ASN1EncodableVector  recipientInfos,
            OutputEncryptor      encryptor)
            throws IOException
        {
            this.dataType = dataType;
            this.recipientInfos = recipientInfos;

            return super.open(dataType, out, recipientInfos, encryptor);
        }

        OutputStream regenerate(
            OutputStream out,
            OutputEncryptor     encryptor)
            throws IOException
        {
            return super.open(dataType, out, recipientInfos, encryptor);
        }
    }

    private static class WrappingIOException
        extends IOException
    {
        private Throwable cause;

        WrappingIOException(String msg, Throwable cause)
        {
            super(msg);

            this.cause = cause;
        }

        public Throwable getCause()
        {
            return cause;
        }
    }
}
