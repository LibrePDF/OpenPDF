package old.org.bouncycastle.mail.smime;

import java.io.IOException;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.cert.CertStore;
import java.security.cert.CertStoreException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import old.org.bouncycastle.asn1.cms.AttributeTable;
import old.org.bouncycastle.asn1.cryptopro.CryptoProObjectIdentifiers;
import old.org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import old.org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import old.org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import old.org.bouncycastle.asn1.teletrust.TeleTrusTObjectIdentifiers;
import old.org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import old.org.bouncycastle.cms.CMSException;
import old.org.bouncycastle.cms.CMSSignedDataStreamGenerator;
import old.org.bouncycastle.cms.SignerInfoGenerator;
import old.org.bouncycastle.cms.SignerInformation;
import old.org.bouncycastle.cms.SignerInformationStore;
import old.org.bouncycastle.mail.smime.util.CRLFOutputStream;
import old.org.bouncycastle.util.Store;
import old.org.bouncycastle.x509.X509Store;

/**
 * general class for generating a pkcs7-signature message.
 * <p>
 * A simple example of usage.
 *
 * <pre>
 *      X509Certificate signCert = ...
 *      KeyPair         signKP = ...
 *
 *      List certList = new ArrayList();
 *
 *      certList.add(signCert);
 *
 *      Store certs = new JcaCertStore(certList);
 *
 *      SMIMESignedGenerator gen = new SMIMESignedGenerator();
 *
 *      gen.addSignerInfoGenerator(new JcaSimpleSignerInfoGeneratorBuilder().setProvider("BC").build("SHA1withRSA", signKP.getPrivate(), signCert));
 *
 *      gen.addCertificates(certs);
 *
 *      MimeMultipart       smime = fact.generate(content);
 * </pre>
 * <p>
 * Note: if you are using this class with AS2 or some other protocol
 * that does not use "7bit" as the default content transfer encoding you
 * will need to use the constructor that allows you to specify the default
 * content transfer encoding, such as "binary".
 * </p>
 */
public class SMIMESignedGenerator
    extends SMIMEGenerator
{
    public static final String  DIGEST_SHA1 = OIWObjectIdentifiers.idSHA1.getId();
    public static final String  DIGEST_MD5 = PKCSObjectIdentifiers.md5.getId();
    public static final String  DIGEST_SHA224 = NISTObjectIdentifiers.id_sha224.getId();
    public static final String  DIGEST_SHA256 = NISTObjectIdentifiers.id_sha256.getId();
    public static final String  DIGEST_SHA384 = NISTObjectIdentifiers.id_sha384.getId();
    public static final String  DIGEST_SHA512 = NISTObjectIdentifiers.id_sha512.getId();
    public static final String  DIGEST_GOST3411 = CryptoProObjectIdentifiers.gostR3411.getId();
    public static final String  DIGEST_RIPEMD128 = TeleTrusTObjectIdentifiers.ripemd128.getId();
    public static final String  DIGEST_RIPEMD160 = TeleTrusTObjectIdentifiers.ripemd160.getId();
    public static final String  DIGEST_RIPEMD256 = TeleTrusTObjectIdentifiers.ripemd256.getId();

    public static final String  ENCRYPTION_RSA = PKCSObjectIdentifiers.rsaEncryption.getId();
    public static final String  ENCRYPTION_DSA = X9ObjectIdentifiers.id_dsa_with_sha1.getId();
    public static final String  ENCRYPTION_ECDSA = X9ObjectIdentifiers.ecdsa_with_SHA1.getId();
    public static final String  ENCRYPTION_RSA_PSS = PKCSObjectIdentifiers.id_RSASSA_PSS.getId();
    public static final String  ENCRYPTION_GOST3410 = CryptoProObjectIdentifiers.gostR3410_94.getId();
    public static final String  ENCRYPTION_ECGOST3410 = CryptoProObjectIdentifiers.gostR3410_2001.getId();

    private static final String CERTIFICATE_MANAGEMENT_CONTENT = "application/pkcs7-mime; name=smime.p7c; smime-type=certs-only";
    private static final String DETACHED_SIGNATURE_TYPE = "application/pkcs7-signature; name=smime.p7s; smime-type=signed-data";
    private static final String ENCAPSULATED_SIGNED_CONTENT_TYPE = "application/pkcs7-mime; name=smime.p7m; smime-type=signed-data";

    private final String        _defaultContentTransferEncoding;

    private List                _certStores = new ArrayList();
    private List                certStores = new ArrayList();
    private List                crlStores = new ArrayList();
    private List                attrCertStores = new ArrayList();
    private List                signerInfoGens = new ArrayList();
    private List                _signers = new ArrayList();
    private List                _oldSigners = new ArrayList();
    private List                _attributeCerts = new ArrayList();
    private Map                 _digests = new HashMap();
    
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
     * base constructor - default content transfer encoding 7bit
     */
    public SMIMESignedGenerator()
    {
        _defaultContentTransferEncoding = "7bit";
    }

    /**
     * base constructor - default content transfer encoding explicitly set
     * 
     * @param defaultContentTransferEncoding new default to use.
     */
    public SMIMESignedGenerator(
        String defaultContentTransferEncoding)
    {
        _defaultContentTransferEncoding = defaultContentTransferEncoding;
    }
    
    /**
     * add a signer - no attributes other than the default ones will be
     * provided here.
     *
     * @param key key to use to generate the signature
     * @param cert the public key certificate associated with the signer's key.
     * @param digestOID object ID of the digest algorithm to use.
     * @exception IllegalArgumentException any of the arguments are inappropriate
     * @deprecated use addSignerInfoGenerator()
     */
    public void addSigner(
        PrivateKey      key,
        X509Certificate cert,
        String          digestOID)
        throws IllegalArgumentException
    {
        _signers.add(new Signer(key, cert, digestOID, null, null));
    }

    /**
     * add a signer - no attributes other than the default ones will be
     * provided here.
     *
     * @param key key to use to generate the signature
     * @param cert the public key certificate associated with the signer's key.
     * @param encryptionOID object ID of the digest ecnryption algorithm to use.
     * @param digestOID object ID of the digest algorithm to use.
     * @exception IllegalArgumentException any of the arguments are inappropriate
     * @deprecated use addSignerInfoGenerator()
     */
    public void addSigner(
        PrivateKey      key,
        X509Certificate cert,
        String          encryptionOID,
        String          digestOID)
        throws IllegalArgumentException
    {
        _signers.add(new Signer(key, cert, encryptionOID, digestOID, null, null));
    }

    /**
     * Add a signer with extra signed/unsigned attributes or overrides
     * for the standard attributes. For example this method can be used to
     * explictly set default attributes such as the signing time.
     *
     * @param key key to use to generate the signature
     * @param cert the public key certificate associated with the signer's key.
     * @param digestOID object ID of the digest algorithm to use.
     * @param signedAttr signed attributes to be included in the signature.
     * @param unsignedAttr unsigned attribitues to be included.
     * @exception IllegalArgumentException any of the arguments are inappropriate
     * @deprecated use addSignerInfoGenerator()
     */
    public void addSigner(
        PrivateKey      key,
        X509Certificate cert,
        String          digestOID,
        AttributeTable  signedAttr,
        AttributeTable  unsignedAttr)
        throws IllegalArgumentException
    {
        _signers.add(new Signer(key, cert, digestOID, signedAttr, unsignedAttr));
    }

    /**
     * Add a signer with extra signed/unsigned attributes or overrides
     * for the standard attributes and a digest encryption algorithm. For
     * example this method can be used to explictly set default attributes
     * such as the signing time.
     *
     * @param key key to use to generate the signature
     * @param cert the public key certificate associated with the signer's key.
     * @param encryptionOID the digest encryption algorithm OID.
     * @param digestOID object ID of the digest algorithm to use.
     * @param signedAttr signed attributes to be included in the signature.
     * @param unsignedAttr unsigned attribitues to be included.
     * @exception IllegalArgumentException any of the arguments are inappropriate
     * @deprecated use addSignerInfoGenerator()
     */
    public void addSigner(
        PrivateKey      key,
        X509Certificate cert,
        String          encryptionOID,
        String          digestOID,
        AttributeTable  signedAttr,
        AttributeTable  unsignedAttr)
        throws IllegalArgumentException
    {
        _signers.add(new Signer(key, cert, encryptionOID, digestOID, signedAttr, unsignedAttr));
    }

    /**
     * Add a store of precalculated signers to the generator.
     *
     * @param signerStore store of signers
     */
    public void addSigners(
        SignerInformationStore signerStore)
    {
        Iterator    it = signerStore.getSigners().iterator();

        while (it.hasNext())
        {
            _oldSigners.add(it.next());
        }
    }

    public void addSignerInfoGenerator(SignerInfoGenerator sigInfoGen)
    {
        signerInfoGens.add(sigInfoGen);
    }

    /**
     * add the certificates and CRLs contained in the given CertStore
     * to the pool that will be included in the encoded signature block.
     * <p>
     * Note: this assumes the CertStore will support null in the get
     * methods.
     * </p>
     * @param certStore CertStore containing the certificates and CRLs to be added.
     * @deprecated use addCertificates(Store) and addCRLs(Store)
     */
    public void addCertificatesAndCRLs(
        CertStore               certStore)
        throws CertStoreException, SMIMEException
    {
        _certStores.add(certStore);
    }

    public void addCertificates(
        Store certStore)
    {
        certStores.add(certStore);
    }

    public void addCRLs(
        Store crlStore)
    {
        crlStores.add(crlStore);
    }

    public void addAttributeCertificates(
        Store certStore)
    {
        attrCertStores.add(certStore);
    }

    /**
     * Add the attribute certificates contained in the passed in store to the
     * generator.
     *
     * @param store a store of Version 2 attribute certificates
     * @throws CMSException if an error occurse processing the store.
     * @deprecated use addAttributeCertificates(Store)
     */
    public void addAttributeCertificates(
        X509Store store)
        throws CMSException
    {
        _attributeCerts.add(store);
    }

    private void addHashHeader(
        StringBuffer header,
        List         signers)
    {
        int                 count = 0;
        
        //
        // build the hash header
        //
        Iterator   it = signers.iterator();
        Set        micAlgs = new HashSet();
        
        while (it.hasNext())
        {
            Object       signer = it.next();
            String       digestOID;

            if (signer instanceof Signer)
            {
                digestOID = ((Signer)signer).getDigestOID();
            }
            else if (signer instanceof SignerInformation)
            {
                digestOID = ((SignerInformation)signer).getDigestAlgOID();
            }
            else
            {
                digestOID = ((SignerInfoGenerator)signer).getDigestAlgorithm().getAlgorithm().getId();
            }

            if (digestOID.equals(DIGEST_SHA1))
            {
                micAlgs.add("sha1");
            }
            else if (digestOID.equals(DIGEST_MD5))
            {
                micAlgs.add("md5");
            }
            else if (digestOID.equals(DIGEST_SHA224))
            {
                micAlgs.add("sha224");
            }
            else if (digestOID.equals(DIGEST_SHA256))
            {
                micAlgs.add("sha256");
            }
            else if (digestOID.equals(DIGEST_SHA384))
            {
                micAlgs.add("sha384");
            }
            else if (digestOID.equals(DIGEST_SHA512))
            {
                micAlgs.add("sha512");
            }
            else if (digestOID.equals(DIGEST_GOST3411))
            {
                micAlgs.add("gostr3411-94");
            }
            else
            {
                micAlgs.add("unknown");
            }
        }
        
        it = micAlgs.iterator();
        
        while (it.hasNext())
        {
            String    alg = (String)it.next();

            if (count == 0)
            {
                if (micAlgs.size() != 1)
                {
                    header.append("; micalg=\"");
                }
                else
                {
                    header.append("; micalg=");
                }
            }
            else
            {
                header.append(',');
            }

            header.append(alg);

            count++;
        }

        if (count != 0)
        {
            if (micAlgs.size() != 1)
            {
                header.append('\"');
            }
        }
    }
    
    /*
     * at this point we expect our body part to be well defined.
     */
    private MimeMultipart make(
        MimeBodyPart    content,
        Provider        sigProvider)
        throws NoSuchAlgorithmException, SMIMEException
    {
        try
        {
            MimeBodyPart sig = new MimeBodyPart();

            sig.setContent(new ContentSigner(content, false, sigProvider), DETACHED_SIGNATURE_TYPE);
            sig.addHeader("Content-Type", DETACHED_SIGNATURE_TYPE);
            sig.addHeader("Content-Disposition", "attachment; filename=\"smime.p7s\"");
            sig.addHeader("Content-Description", "S/MIME Cryptographic Signature");
            sig.addHeader("Content-Transfer-Encoding", encoding);

            //
            // build the multipart header
            //
            StringBuffer        header = new StringBuffer(
                    "signed; protocol=\"application/pkcs7-signature\"");

            List allSigners = new ArrayList(_signers);

            allSigners.addAll(_oldSigners);

            allSigners.addAll(signerInfoGens);

            addHashHeader(header, allSigners);

            MimeMultipart   mm = new MimeMultipart(header.toString());

            mm.addBodyPart(content);
            mm.addBodyPart(sig);

            return mm;
        }
        catch (MessagingException e)
        {
            throw new SMIMEException("exception putting multi-part together.", e);
        }
    }

    private MimeMultipart make(
        MimeBodyPart    content)
    throws SMIMEException
    {
        try
        {
            MimeBodyPart sig = new MimeBodyPart();

            sig.setContent(new ContentSigner(content, false), DETACHED_SIGNATURE_TYPE);
            sig.addHeader("Content-Type", DETACHED_SIGNATURE_TYPE);
            sig.addHeader("Content-Disposition", "attachment; filename=\"smime.p7s\"");
            sig.addHeader("Content-Description", "S/MIME Cryptographic Signature");
            sig.addHeader("Content-Transfer-Encoding", encoding);

            //
            // build the multipart header
            //
            StringBuffer        header = new StringBuffer(
                    "signed; protocol=\"application/pkcs7-signature\"");

            List allSigners = new ArrayList(_signers);

            allSigners.addAll(_oldSigners);

            allSigners.addAll(signerInfoGens);

            addHashHeader(header, allSigners);

            MimeMultipart   mm = new MimeMultipart(header.toString());

            mm.addBodyPart(content);
            mm.addBodyPart(sig);

            return mm;
        }
        catch (MessagingException e)
        {
            throw new SMIMEException("exception putting multi-part together.", e);
        }
    }

    /*
     * at this point we expect our body part to be well defined - generate with data in the signature
     */
    private MimeBodyPart makeEncapsulated(
        MimeBodyPart    content,
        Provider        sigProvider)
        throws NoSuchAlgorithmException, SMIMEException
    {
        try
        {
            MimeBodyPart sig = new MimeBodyPart();
            
            sig.setContent(new ContentSigner(content, true, sigProvider), ENCAPSULATED_SIGNED_CONTENT_TYPE);
            sig.addHeader("Content-Type", ENCAPSULATED_SIGNED_CONTENT_TYPE);
            sig.addHeader("Content-Disposition", "attachment; filename=\"smime.p7m\"");
            sig.addHeader("Content-Description", "S/MIME Cryptographic Signed Data");
            sig.addHeader("Content-Transfer-Encoding", encoding);
            
            return sig;
        }
        catch (MessagingException e)
        {
            throw new SMIMEException("exception putting body part together.", e);
        }
    }

    /*
     * at this point we expect our body part to be well defined - generate with data in the signature
     */
    private MimeBodyPart makeEncapsulated(
        MimeBodyPart    content)
        throws SMIMEException
    {
        try
        {
            MimeBodyPart sig = new MimeBodyPart();

            sig.setContent(new ContentSigner(content, true), ENCAPSULATED_SIGNED_CONTENT_TYPE);
            sig.addHeader("Content-Type", ENCAPSULATED_SIGNED_CONTENT_TYPE);
            sig.addHeader("Content-Disposition", "attachment; filename=\"smime.p7m\"");
            sig.addHeader("Content-Description", "S/MIME Cryptographic Signed Data");
            sig.addHeader("Content-Transfer-Encoding", encoding);

            return sig;
        }
        catch (MessagingException e)
        {
            throw new SMIMEException("exception putting body part together.", e);
        }
    }

    /**
     * Return a map of oids and byte arrays representing the digests calculated on the content during
     * the last generate.
     *
     * @return a map of oids (as String objects) and byte[] representing digests.
     */
    public Map getGeneratedDigests()
    {
        return new HashMap(_digests);
    }

    /**
     * generate a signed object that contains an SMIME Signed Multipart
     * object using the given provider.
     * @param content the MimeBodyPart to be signed.
     * @param sigProvider the provider to be used for the signature.
     * @return a Multipart containing the content and signature.
     * @throws NoSuchAlgorithmException if the required algorithms for the signature cannot be found.
     * @throws NoSuchProviderException if no provider can be found.
     * @throws SMIMEException if an exception occurs in processing the signature.
     * @deprecated use generate(MimeBodyPart)
     */
    public MimeMultipart generate(
        MimeBodyPart    content,
        String          sigProvider)
        throws NoSuchAlgorithmException, NoSuchProviderException, SMIMEException
    {
        return make(makeContentBodyPart(content), SMIMEUtil.getProvider(sigProvider));
    }

    /**
     * generate a signed object that contains an SMIME Signed Multipart
     * object using the given provider.
     * @param content the MimeBodyPart to be signed.
     * @param sigProvider the provider to be used for the signature.
     * @return a Multipart containing the content and signature.
     * @throws NoSuchAlgorithmException if the required algorithms for the signature cannot be found.
     * @throws SMIMEException if an exception occurs in processing the signature.
     */
    public MimeMultipart generate(
        MimeBodyPart    content,
        Provider        sigProvider)
        throws NoSuchAlgorithmException, SMIMEException
    {
        return make(makeContentBodyPart(content), sigProvider);
    }

    /**
     * generate a signed object that contains an SMIME Signed Multipart
     * object using the given provider from the given MimeMessage
     *
     * @throws NoSuchAlgorithmException if the required algorithms for the signature cannot be found.
     * @throws NoSuchProviderException if no provider can be found.
     * @throws SMIMEException if an exception occurs in processing the signature.
     */
    public MimeMultipart generate(
        MimeMessage     message,
        String          sigProvider)
        throws NoSuchAlgorithmException, NoSuchProviderException, SMIMEException
    {
        return generate(message, SMIMEUtil.getProvider(sigProvider));
    }

    /**
     * generate a signed object that contains an SMIME Signed Multipart
     * object using the given provider from the given MimeMessage
     *
     * @throws NoSuchAlgorithmException if the required algorithms for the signature cannot be found.
     * @throws NoSuchProviderException if no provider can be found.
     * @throws SMIMEException if an exception occurs in processing the signature.
     */
    public MimeMultipart generate(
        MimeMessage     message,
        Provider        sigProvider)
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

        return make(makeContentBodyPart(message), sigProvider);
    }

    public MimeMultipart generate(
        MimeBodyPart    content)
        throws SMIMEException
    {
        return make(makeContentBodyPart(content));
    }

    /**
     * generate a signed message with encapsulated content
     * <p>
     * Note: doing this is strongly <b>not</b> recommended as it means a
     * recipient of the message will have to be able to read the signature to read the
     * message.
     */
    public MimeBodyPart generateEncapsulated(
        MimeBodyPart    content)
        throws SMIMEException
    {
        return makeEncapsulated(makeContentBodyPart(content));
    }

    /**
     * generate a signed message with encapsulated content
     * <p>
     * Note: doing this is strongly <b>not</b> recommended as it means a
     * recipient of the message will have to be able to read the signature to read the 
     * message.
     * @deprecated use generateEncapsulated(content)
     */
    public MimeBodyPart generateEncapsulated(
        MimeBodyPart    content,
        String          sigProvider)
        throws NoSuchAlgorithmException, NoSuchProviderException, SMIMEException
    {
        return makeEncapsulated(makeContentBodyPart(content), SMIMEUtil.getProvider(sigProvider));
    }

    /**
     * generate a signed message with encapsulated content
     * <p>
     * Note: doing this is strongly <b>not</b> recommended as it means a
     * recipient of the message will have to be able to read the signature to read the
     * message.
     * @deprecated use generateEncapsulated(content)
     */
    public MimeBodyPart generateEncapsulated(
        MimeBodyPart    content,
        Provider        sigProvider)
        throws NoSuchAlgorithmException, NoSuchProviderException, SMIMEException
    {
        return makeEncapsulated(makeContentBodyPart(content), sigProvider);
    }

    /**
     * generate a signed object that contains an SMIME Signed Multipart
     * object using the given provider from the given MimeMessage.
     * <p>
     * Note: doing this is strongly <b>not</b> recommended as it means a
     * recipient of the message will have to be able to read the signature to read the
     * message.
     * @deprecated use generateEncapsulated(content)
     */
    public MimeBodyPart generateEncapsulated(
        MimeMessage     message,
        String          sigProvider)
        throws NoSuchAlgorithmException, NoSuchProviderException, SMIMEException
    {
        return generateEncapsulated(message, SMIMEUtil.getProvider(sigProvider));
    }

    /**
     * generate a signed object that contains an SMIME Signed Multipart
     * object using the given provider from the given MimeMessage.
     * <p>
     * Note: doing this is strongly <b>not</b> recommended as it means a
     * recipient of the message will have to be able to read the signature to read the 
     * message.
     * @deprecated use generateEncapsulated(content)
     */
    public MimeBodyPart generateEncapsulated(
        MimeMessage     message,
        Provider        sigProvider)
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

        return makeEncapsulated(makeContentBodyPart(message), sigProvider);
    }

    /**
     * Creates a certificate management message which is like a signed message with no content
     * or signers but that still carries certificates and CRLs.
     *
     * @return a MimeBodyPart containing the certs and CRLs.
     */
    public MimeBodyPart generateCertificateManagement(
       String provider)
       throws SMIMEException, NoSuchProviderException
    {
        return generateCertificateManagement(SMIMEUtil.getProvider(provider));
    }

    /**
     * Creates a certificate management message which is like a signed message with no content
     * or signers but that still carries certificates and CRLs.
     * 
     * @return a MimeBodyPart containing the certs and CRLs.
     */
    public MimeBodyPart generateCertificateManagement(
       Provider provider)
       throws SMIMEException
    {
        try
        {
            MimeBodyPart sig = new MimeBodyPart();
            
            sig.setContent(new ContentSigner(null, true, provider), CERTIFICATE_MANAGEMENT_CONTENT);
            sig.addHeader("Content-Type", CERTIFICATE_MANAGEMENT_CONTENT);
            sig.addHeader("Content-Disposition", "attachment; filename=\"smime.p7c\"");
            sig.addHeader("Content-Description", "S/MIME Certificate Management Message");
            sig.addHeader("Content-Transfer-Encoding", encoding);

            return sig;
        }
        catch (MessagingException e)
        {
            throw new SMIMEException("exception putting body part together.", e);
        }
    }
    
    private class Signer
    {
        final PrivateKey      key;
        final X509Certificate cert;
        final String          encryptionOID;
        final String          digestOID;
        final AttributeTable  signedAttr;
        final AttributeTable  unsignedAttr;
        
        Signer(
            PrivateKey      key,
            X509Certificate cert,
            String          digestOID,
            AttributeTable  signedAttr,
            AttributeTable  unsignedAttr)
        {
            this(key, cert, null, digestOID, signedAttr, unsignedAttr);
        }

        Signer(
            PrivateKey      key,
            X509Certificate cert,
            String          encryptionOID,
            String          digestOID,
            AttributeTable  signedAttr,
            AttributeTable  unsignedAttr)
        {
            this.key = key;
            this.cert = cert;
            this.encryptionOID = encryptionOID;
            this.digestOID = digestOID;
            this.signedAttr = signedAttr;
            this.unsignedAttr = unsignedAttr;
        }

        public X509Certificate getCert()
        {
            return cert;
        }

        public String getEncryptionOID()
        {
            return encryptionOID;
        }

        public String getDigestOID()
        {
            return digestOID;
        }

        public PrivateKey getKey()
        {
            return key;
        }

        public AttributeTable getSignedAttr()
        {
            return signedAttr;
        }

        public AttributeTable getUnsignedAttr()
        {
            return unsignedAttr;
        }
    }

    private class ContentSigner
        implements SMIMEStreamingProcessor
    {
        private final MimeBodyPart content;
        private final boolean encapsulate;
        private final Provider provider;
        private final boolean  noProvider;

        ContentSigner(
            MimeBodyPart content,
            boolean      encapsulate,
            Provider     provider)
        {
            this.content = content;
            this.encapsulate = encapsulate;
            this.provider = provider;
            this.noProvider = false;
        }

        ContentSigner(
            MimeBodyPart content,
            boolean      encapsulate)
        {
            this.content = content;
            this.encapsulate = encapsulate;
            this.provider = null;
            this.noProvider = true;
        }

        protected CMSSignedDataStreamGenerator getGenerator()
            throws CMSException, CertStoreException, InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException
        {
            CMSSignedDataStreamGenerator gen = new CMSSignedDataStreamGenerator();
            
            for (Iterator it = _certStores.iterator(); it.hasNext();)
            {
                gen.addCertificatesAndCRLs((CertStore)it.next());
            }

            for (Iterator it = certStores.iterator(); it.hasNext();)
            {
                gen.addCertificates((Store)it.next());
            }

            for (Iterator it = crlStores.iterator(); it.hasNext();)
            {
                gen.addCRLs((Store)it.next());
            }

            for (Iterator it = attrCertStores.iterator(); it.hasNext();)
            {
                gen.addAttributeCertificates((Store)it.next());
            }

            for (Iterator it = _attributeCerts.iterator(); it.hasNext();)
            {
                gen.addAttributeCertificates((X509Store)it.next());
            }

            for (Iterator it = _signers.iterator(); it.hasNext();)
            {
                Signer signer = (Signer)it.next();

                if (signer.getEncryptionOID() != null)
                {
                    gen.addSigner(signer.getKey(), signer.getCert(), signer.getEncryptionOID(), signer.getDigestOID(), signer.getSignedAttr(), signer.getUnsignedAttr(), provider);
                }
                else
                {
                    gen.addSigner(signer.getKey(), signer.getCert(), signer.getDigestOID(), signer.getSignedAttr(), signer.getUnsignedAttr(), provider);
                }
            }

            for (Iterator it = signerInfoGens.iterator(); it.hasNext();)
            {
                gen.addSignerInfoGenerator((SignerInfoGenerator)it.next());
            }

            gen.addSigners(new SignerInformationStore(_oldSigners));
            
            return gen;
        }

        private void writeBodyPart(
            OutputStream out,
            MimeBodyPart bodyPart)
            throws IOException, MessagingException
        {
            if (bodyPart.getContent() instanceof Multipart)
            {
                Multipart mp = (Multipart)bodyPart.getContent();
                ContentType contentType = new ContentType(mp.getContentType());
                String boundary = "--" + contentType.getParameter("boundary");

                SMIMEUtil.LineOutputStream lOut = new SMIMEUtil.LineOutputStream(out);

                Enumeration headers = bodyPart.getAllHeaderLines();
                while (headers.hasMoreElements())
                {
                    lOut.writeln((String)headers.nextElement());
                }

                lOut.writeln();      // CRLF separator

                SMIMEUtil.outputPreamble(lOut, bodyPart, boundary);

                for (int i = 0; i < mp.getCount(); i++)
                {
                    lOut.writeln(boundary);
                    writeBodyPart(out, (MimeBodyPart)mp.getBodyPart(i));
                    lOut.writeln();       // CRLF terminator
                }
                
                lOut.writeln(boundary + "--");
            }
            else
            {
                if (SMIMEUtil.isCanonicalisationRequired(bodyPart, _defaultContentTransferEncoding))
                {
                    out = new CRLFOutputStream(out);
                }

                bodyPart.writeTo(out);
            }
        }

        public void write(OutputStream out)
            throws IOException
        {
            try
            {
                CMSSignedDataStreamGenerator gen = getGenerator();
                
                OutputStream signingStream = gen.open(out, encapsulate);
                
                if (content != null)
                {
                    if (!encapsulate)
                    {
                        writeBodyPart(signingStream, content);
                    }
                    else
                    {
                        content.getDataHandler().setCommandMap(addCommands(CommandMap.getDefaultCommandMap()));

                        content.writeTo(signingStream);
                    }
                }
                
                signingStream.close();

                _digests = gen.getGeneratedDigests();
            }
            catch (MessagingException e)
            {
                throw new IOException(e.toString());
            }
            catch (NoSuchAlgorithmException e)
            {
                throw new IOException(e.toString());
            }
            catch (NoSuchProviderException e)
            {
                throw new IOException(e.toString());
            }
            catch (CMSException e)
            {
                throw new IOException(e.toString());
            }
            catch (InvalidKeyException e)
            {
                throw new IOException(e.toString());
            }
            catch (CertStoreException e)
            {
                throw new IOException(e.toString());
            }
        }
    }
}
