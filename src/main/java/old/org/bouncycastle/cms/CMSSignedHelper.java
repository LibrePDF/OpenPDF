package old.org.bouncycastle.cms;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Signature;
import java.security.cert.CRLException;
import java.security.cert.CertStore;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CollectionCertStoreParameters;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import old.org.bouncycastle.asn1.ASN1Sequence;
import old.org.bouncycastle.asn1.ASN1Set;
import old.org.bouncycastle.asn1.ASN1TaggedObject;
import old.org.bouncycastle.asn1.DEREncodable;
import old.org.bouncycastle.asn1.DERNull;
import old.org.bouncycastle.asn1.DERObject;
import old.org.bouncycastle.asn1.DERObjectIdentifier;
import old.org.bouncycastle.asn1.cryptopro.CryptoProObjectIdentifiers;
import old.org.bouncycastle.asn1.eac.EACObjectIdentifiers;
import old.org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import old.org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import old.org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import old.org.bouncycastle.asn1.teletrust.TeleTrusTObjectIdentifiers;
import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import old.org.bouncycastle.asn1.x509.X509ObjectIdentifiers;
import old.org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import old.org.bouncycastle.x509.NoSuchStoreException;
import old.org.bouncycastle.x509.X509CollectionStoreParameters;
import old.org.bouncycastle.x509.X509Store;
import old.org.bouncycastle.x509.X509V2AttributeCertificate;

class CMSSignedHelper
{
    static final CMSSignedHelper INSTANCE = new CMSSignedHelper();

    private static final Map     encryptionAlgs = new HashMap();
    private static final Map     digestAlgs = new HashMap();
    private static final Map     digestAliases = new HashMap();

    private static void addEntries(DERObjectIdentifier alias, String digest, String encryption)
    {
        digestAlgs.put(alias.getId(), digest);
        encryptionAlgs.put(alias.getId(), encryption);
    }

    static
    {
        addEntries(NISTObjectIdentifiers.dsa_with_sha224, "SHA224", "DSA");
        addEntries(NISTObjectIdentifiers.dsa_with_sha256, "SHA256", "DSA");
        addEntries(NISTObjectIdentifiers.dsa_with_sha384, "SHA384", "DSA");
        addEntries(NISTObjectIdentifiers.dsa_with_sha512, "SHA512", "DSA");
        addEntries(OIWObjectIdentifiers.dsaWithSHA1, "SHA1", "DSA");
        addEntries(OIWObjectIdentifiers.md4WithRSA, "MD4", "RSA");
        addEntries(OIWObjectIdentifiers.md4WithRSAEncryption, "MD4", "RSA");
        addEntries(OIWObjectIdentifiers.md5WithRSA, "MD5", "RSA");
        addEntries(OIWObjectIdentifiers.sha1WithRSA, "SHA1", "RSA");
        addEntries(PKCSObjectIdentifiers.md2WithRSAEncryption, "MD2", "RSA");
        addEntries(PKCSObjectIdentifiers.md4WithRSAEncryption, "MD4", "RSA");
        addEntries(PKCSObjectIdentifiers.md5WithRSAEncryption, "MD5", "RSA");
        addEntries(PKCSObjectIdentifiers.sha1WithRSAEncryption, "SHA1", "RSA");
        addEntries(PKCSObjectIdentifiers.sha224WithRSAEncryption, "SHA224", "RSA");
        addEntries(PKCSObjectIdentifiers.sha256WithRSAEncryption, "SHA256", "RSA");
        addEntries(PKCSObjectIdentifiers.sha384WithRSAEncryption, "SHA384", "RSA");
        addEntries(PKCSObjectIdentifiers.sha512WithRSAEncryption, "SHA512", "RSA");
        addEntries(X9ObjectIdentifiers.ecdsa_with_SHA1, "SHA1", "ECDSA");
        addEntries(X9ObjectIdentifiers.ecdsa_with_SHA224, "SHA224", "ECDSA");
        addEntries(X9ObjectIdentifiers.ecdsa_with_SHA256, "SHA256", "ECDSA");
        addEntries(X9ObjectIdentifiers.ecdsa_with_SHA384, "SHA384", "ECDSA");
        addEntries(X9ObjectIdentifiers.ecdsa_with_SHA512, "SHA512", "ECDSA");
        addEntries(X9ObjectIdentifiers.id_dsa_with_sha1, "SHA1", "DSA");
        addEntries(EACObjectIdentifiers.id_TA_ECDSA_SHA_1, "SHA1", "ECDSA");
        addEntries(EACObjectIdentifiers.id_TA_ECDSA_SHA_224, "SHA224", "ECDSA");
        addEntries(EACObjectIdentifiers.id_TA_ECDSA_SHA_256, "SHA256", "ECDSA");
        addEntries(EACObjectIdentifiers.id_TA_ECDSA_SHA_384, "SHA384", "ECDSA");
        addEntries(EACObjectIdentifiers.id_TA_ECDSA_SHA_512, "SHA512", "ECDSA");
        addEntries(EACObjectIdentifiers.id_TA_RSA_v1_5_SHA_1, "SHA1", "RSA");
        addEntries(EACObjectIdentifiers.id_TA_RSA_v1_5_SHA_256, "SHA256", "RSA");
        addEntries(EACObjectIdentifiers.id_TA_RSA_PSS_SHA_1, "SHA1", "RSAandMGF1");
        addEntries(EACObjectIdentifiers.id_TA_RSA_PSS_SHA_256, "SHA256", "RSAandMGF1");

        encryptionAlgs.put(X9ObjectIdentifiers.id_dsa.getId(), "DSA");
        encryptionAlgs.put(PKCSObjectIdentifiers.rsaEncryption.getId(), "RSA");
        encryptionAlgs.put(TeleTrusTObjectIdentifiers.teleTrusTRSAsignatureAlgorithm, "RSA");
        encryptionAlgs.put(X509ObjectIdentifiers.id_ea_rsa.getId(), "RSA");
        encryptionAlgs.put(CMSSignedDataGenerator.ENCRYPTION_RSA_PSS, "RSAandMGF1");
        encryptionAlgs.put(CryptoProObjectIdentifiers.gostR3410_94.getId(), "GOST3410");
        encryptionAlgs.put(CryptoProObjectIdentifiers.gostR3410_2001.getId(), "ECGOST3410");
        encryptionAlgs.put("1.3.6.1.4.1.5849.1.6.2", "ECGOST3410");
        encryptionAlgs.put("1.3.6.1.4.1.5849.1.1.5", "GOST3410");
        encryptionAlgs.put(CryptoProObjectIdentifiers.gostR3411_94_with_gostR3410_2001.getId(), "ECGOST3410");
        encryptionAlgs.put(CryptoProObjectIdentifiers.gostR3411_94_with_gostR3410_94.getId(), "GOST3410");

        digestAlgs.put(PKCSObjectIdentifiers.md2.getId(), "MD2");
        digestAlgs.put(PKCSObjectIdentifiers.md4.getId(), "MD4");
        digestAlgs.put(PKCSObjectIdentifiers.md5.getId(), "MD5");
        digestAlgs.put(OIWObjectIdentifiers.idSHA1.getId(), "SHA1");
        digestAlgs.put(NISTObjectIdentifiers.id_sha224.getId(), "SHA224");
        digestAlgs.put(NISTObjectIdentifiers.id_sha256.getId(), "SHA256");
        digestAlgs.put(NISTObjectIdentifiers.id_sha384.getId(), "SHA384");
        digestAlgs.put(NISTObjectIdentifiers.id_sha512.getId(), "SHA512");
        digestAlgs.put(TeleTrusTObjectIdentifiers.ripemd128.getId(), "RIPEMD128");
        digestAlgs.put(TeleTrusTObjectIdentifiers.ripemd160.getId(), "RIPEMD160");
        digestAlgs.put(TeleTrusTObjectIdentifiers.ripemd256.getId(), "RIPEMD256");
        digestAlgs.put(CryptoProObjectIdentifiers.gostR3411.getId(),  "GOST3411");
        digestAlgs.put("1.3.6.1.4.1.5849.1.2.1",  "GOST3411");

        digestAliases.put("SHA1", new String[] { "SHA-1" });
        digestAliases.put("SHA224", new String[] { "SHA-224" });
        digestAliases.put("SHA256", new String[] { "SHA-256" });
        digestAliases.put("SHA384", new String[] { "SHA-384" });
        digestAliases.put("SHA512", new String[] { "SHA-512" });
    }
    
    /**
     * Return the digest algorithm using one of the standard JCA string
     * representations rather than the algorithm identifier (if possible).
     */
    String getDigestAlgName(
        String digestAlgOID)
    {
        String algName = (String)digestAlgs.get(digestAlgOID);

        if (algName != null)
        {
            return algName;
        }

        return digestAlgOID;
    }

    String[] getDigestAliases(
        String algName)
    {
        String[] aliases = (String[])digestAliases.get(algName);

        if (aliases != null)
        {
            return aliases;
        }

        return new String[0];
    }

    /**
     * Return the digest encryption algorithm using one of the standard
     * JCA string representations rather the the algorithm identifier (if
     * possible).
     */
    String getEncryptionAlgName(
        String encryptionAlgOID)
    {
        String algName = (String)encryptionAlgs.get(encryptionAlgOID);

        if (algName != null)
        {
            return algName;
        }

        return encryptionAlgOID;
    }
    
    MessageDigest getDigestInstance(
        String algorithm, 
        Provider provider)
        throws NoSuchAlgorithmException
    {
        try
        {
            return createDigestInstance(algorithm, provider);
        }
        catch (NoSuchAlgorithmException e)
        {
            String[] aliases = getDigestAliases(algorithm);
            for (int i = 0; i != aliases.length; i++)
            {
                try
                {
                    return createDigestInstance(aliases[i], provider);
                }
                catch (NoSuchAlgorithmException ex)
                {
                    // continue
                }
            }
            if (provider != null)
            {
                return getDigestInstance(algorithm, null); // try rolling back
            }
            throw e;
        }
    }

    private MessageDigest createDigestInstance(
        String algorithm,
        Provider provider)
        throws NoSuchAlgorithmException
    {
        if (provider != null)
        {
            return MessageDigest.getInstance(algorithm, provider);
        }
        else
        {
            return MessageDigest.getInstance(algorithm);
        }
    }

    Signature getSignatureInstance(
        String algorithm, 
        Provider provider)
        throws NoSuchAlgorithmException
    {
        if (provider != null)
        {
            return Signature.getInstance(algorithm, provider);
        }
        else
        {
            return Signature.getInstance(algorithm);
        }
    }

    X509Store createAttributeStore(
        String type,
        Provider provider,
        ASN1Set certSet)
        throws NoSuchStoreException, CMSException
    {
        List certs = new ArrayList();

        if (certSet != null)
        {
            Enumeration e = certSet.getObjects();

            while (e.hasMoreElements())
            {
                try
                {
                    DERObject obj = ((DEREncodable)e.nextElement()).getDERObject();

                    if (obj instanceof ASN1TaggedObject)
                    {
                        ASN1TaggedObject tagged = (ASN1TaggedObject)obj;

                        if (tagged.getTagNo() == 2)
                        {
                            certs.add(new X509V2AttributeCertificate(ASN1Sequence.getInstance(tagged, false).getEncoded()));
                        }
                    }
                }
                catch (IOException ex)
                {
                    throw new CMSException(
                            "can't re-encode attribute certificate!", ex);
                }
            }
        }

        try
        {
            return X509Store.getInstance(
                         "AttributeCertificate/" +type, new X509CollectionStoreParameters(certs), provider);
        }
        catch (IllegalArgumentException e)
        {
            throw new CMSException("can't setup the X509Store", e);
        }
    }

    X509Store createCertificateStore(
        String type,
        Provider provider,
        ASN1Set certSet)
        throws NoSuchStoreException, CMSException
    {
        List certs = new ArrayList();

        if (certSet != null)
        {
            addCertsFromSet(certs, certSet, provider);
        }

        try
        {
            return X509Store.getInstance(
                         "Certificate/" +type, new X509CollectionStoreParameters(certs), provider);
        }
        catch (IllegalArgumentException e)
        {
            throw new CMSException("can't setup the X509Store", e);
        }
    }

    X509Store createCRLsStore(
        String type,
        Provider provider,
        ASN1Set crlSet)
        throws NoSuchStoreException, CMSException
    {
        List crls = new ArrayList();

        if (crlSet != null)
        {
            addCRLsFromSet(crls, crlSet, provider);
        }

        try
        {
            return X509Store.getInstance(
                         "CRL/" +type, new X509CollectionStoreParameters(crls), provider);
        }
        catch (IllegalArgumentException e)
        {
            throw new CMSException("can't setup the X509Store", e);
        }
    }

    CertStore createCertStore(
        String type,
        Provider provider,
        ASN1Set certSet,
        ASN1Set crlSet)
        throws CMSException, NoSuchAlgorithmException
    {
        List certsAndcrls = new ArrayList();

        //
        // load the certificates and revocation lists if we have any
        //

        if (certSet != null)
        {
            addCertsFromSet(certsAndcrls, certSet, provider);
        }

        if (crlSet != null)
        {
            addCRLsFromSet(certsAndcrls, crlSet, provider);
        }

        try
        {
            if (provider != null)
            {
                return CertStore.getInstance(type, new CollectionCertStoreParameters(certsAndcrls), provider);
            }
            else
            {
                return CertStore.getInstance(type, new CollectionCertStoreParameters(certsAndcrls));
            }
        }
        catch (InvalidAlgorithmParameterException e)
        {
            throw new CMSException("can't setup the CertStore", e);
        }
    }

    private void addCertsFromSet(List certs, ASN1Set certSet, Provider provider)
        throws CMSException
    {
        CertificateFactory cf;

        try
        {
            if (provider != null)
            {
                cf = CertificateFactory.getInstance("X.509", provider);
            }
            else
            {
                cf = CertificateFactory.getInstance("X.509");
            }
        }
        catch (CertificateException ex)
        {
            throw new CMSException("can't get certificate factory.", ex);
        }
        Enumeration e = certSet.getObjects();

        while (e.hasMoreElements())
        {
            try
            {
                DERObject obj = ((DEREncodable)e.nextElement()).getDERObject();

                if (obj instanceof ASN1Sequence)
                {
                    certs.add(cf.generateCertificate(
                        new ByteArrayInputStream(obj.getEncoded())));
                }
            }
            catch (IOException ex)
            {
                throw new CMSException(
                        "can't re-encode certificate!", ex);
            }
            catch (CertificateException ex)
            {
                throw new CMSException(
                        "can't re-encode certificate!", ex);
            }
        }
    }

    private void addCRLsFromSet(List crls, ASN1Set certSet, Provider provider)
        throws CMSException
    {
        CertificateFactory cf;

        try
        {
            if (provider != null)
            {
                cf = CertificateFactory.getInstance("X.509", provider);
            }
            else
            {
                cf = CertificateFactory.getInstance("X.509");
            }
        }
        catch (CertificateException ex)
        {
            throw new CMSException("can't get certificate factory.", ex);
        }
        Enumeration e = certSet.getObjects();

        while (e.hasMoreElements())
        {
            try
            {
                DERObject obj = ((DEREncodable)e.nextElement()).getDERObject();

                crls.add(cf.generateCRL(
                    new ByteArrayInputStream(obj.getEncoded())));
            }
            catch (IOException ex)
            {
                throw new CMSException("can't re-encode CRL!", ex);
            }
            catch (CRLException ex)
            {
                throw new CMSException("can't re-encode CRL!", ex);
            }
        }
    }

    AlgorithmIdentifier fixAlgID(AlgorithmIdentifier algId)
    {
        if (algId.getParameters() == null)
        {
            return new AlgorithmIdentifier(algId.getObjectId(), DERNull.INSTANCE);
        }

        return algId;
    }

    void setSigningEncryptionAlgorithmMapping(DERObjectIdentifier oid, String algorithmName)
    {
        encryptionAlgs.put(oid.getId(), algorithmName);
    }

    void setSigningDigestAlgorithmMapping(DERObjectIdentifier oid, String algorithmName)
    {
        digestAlgs.put(oid.getId(), algorithmName);
    }
}
