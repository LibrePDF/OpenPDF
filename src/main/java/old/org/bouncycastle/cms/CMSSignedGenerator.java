package old.org.bouncycastle.cms;

import java.io.IOException;
import java.security.AlgorithmParameters;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.cert.CertStore;
import java.security.cert.CertStoreException;
import java.security.cert.X509Certificate;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import old.org.bouncycastle.asn1.ASN1Object;
import old.org.bouncycastle.asn1.ASN1Set;
import old.org.bouncycastle.asn1.DERNull;
import old.org.bouncycastle.asn1.DERObjectIdentifier;
import old.org.bouncycastle.asn1.DEROctetString;
import old.org.bouncycastle.asn1.DERSet;
import old.org.bouncycastle.asn1.DERTaggedObject;
import old.org.bouncycastle.asn1.cms.AttributeTable;
import old.org.bouncycastle.asn1.cms.CMSObjectIdentifiers;
import old.org.bouncycastle.asn1.cms.SignerIdentifier;
import old.org.bouncycastle.asn1.cryptopro.CryptoProObjectIdentifiers;
import old.org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import old.org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import old.org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import old.org.bouncycastle.asn1.teletrust.TeleTrusTObjectIdentifiers;
import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import old.org.bouncycastle.asn1.x509.AttributeCertificate;
import old.org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import old.org.bouncycastle.jce.interfaces.GOST3410PrivateKey;
import old.org.bouncycastle.util.Store;
import old.org.bouncycastle.x509.X509AttributeCertificate;
import old.org.bouncycastle.x509.X509Store;

public class CMSSignedGenerator
{
    /**
     * Default type for the signed data.
     */
    public static final String  DATA = CMSObjectIdentifiers.data.getId();
    
    public static final String  DIGEST_SHA1 = OIWObjectIdentifiers.idSHA1.getId();
    public static final String  DIGEST_SHA224 = NISTObjectIdentifiers.id_sha224.getId();
    public static final String  DIGEST_SHA256 = NISTObjectIdentifiers.id_sha256.getId();
    public static final String  DIGEST_SHA384 = NISTObjectIdentifiers.id_sha384.getId();
    public static final String  DIGEST_SHA512 = NISTObjectIdentifiers.id_sha512.getId();
    public static final String  DIGEST_MD5 = PKCSObjectIdentifiers.md5.getId();
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

    private static final String  ENCRYPTION_ECDSA_WITH_SHA1 = X9ObjectIdentifiers.ecdsa_with_SHA1.getId();
    private static final String  ENCRYPTION_ECDSA_WITH_SHA224 = X9ObjectIdentifiers.ecdsa_with_SHA224.getId();
    private static final String  ENCRYPTION_ECDSA_WITH_SHA256 = X9ObjectIdentifiers.ecdsa_with_SHA256.getId();
    private static final String  ENCRYPTION_ECDSA_WITH_SHA384 = X9ObjectIdentifiers.ecdsa_with_SHA384.getId();
    private static final String  ENCRYPTION_ECDSA_WITH_SHA512 = X9ObjectIdentifiers.ecdsa_with_SHA512.getId();

    private static final Set NO_PARAMS = new HashSet();
    private static final Map EC_ALGORITHMS = new HashMap();

    static
    {
        NO_PARAMS.add(ENCRYPTION_DSA);
        NO_PARAMS.add(ENCRYPTION_ECDSA);
        NO_PARAMS.add(ENCRYPTION_ECDSA_WITH_SHA1);
        NO_PARAMS.add(ENCRYPTION_ECDSA_WITH_SHA224);
        NO_PARAMS.add(ENCRYPTION_ECDSA_WITH_SHA256);
        NO_PARAMS.add(ENCRYPTION_ECDSA_WITH_SHA384);
        NO_PARAMS.add(ENCRYPTION_ECDSA_WITH_SHA512);

        EC_ALGORITHMS.put(DIGEST_SHA1, ENCRYPTION_ECDSA_WITH_SHA1);
        EC_ALGORITHMS.put(DIGEST_SHA224, ENCRYPTION_ECDSA_WITH_SHA224);
        EC_ALGORITHMS.put(DIGEST_SHA256, ENCRYPTION_ECDSA_WITH_SHA256);
        EC_ALGORITHMS.put(DIGEST_SHA384, ENCRYPTION_ECDSA_WITH_SHA384);
        EC_ALGORITHMS.put(DIGEST_SHA512, ENCRYPTION_ECDSA_WITH_SHA512);
    }

    protected List certs = new ArrayList();
    protected List crls = new ArrayList();
    protected List _signers = new ArrayList();
    protected List signerGens = new ArrayList();
    protected Map digests = new HashMap();

    protected final SecureRandom rand;

    /**
     * base constructor
     */
    protected CMSSignedGenerator()
    {
        this(new SecureRandom());
    }

    /**
     * constructor allowing specific source of randomness
     * @param rand instance of SecureRandom to use
     */
    protected CMSSignedGenerator(
        SecureRandom rand)
    {
        this.rand = rand;
    }
    
    protected String getEncOID(
        PrivateKey key,
        String     digestOID)
    {
        String encOID = null;
        
        if (key instanceof RSAPrivateKey || "RSA".equalsIgnoreCase(key.getAlgorithm()))
        {
            encOID = ENCRYPTION_RSA;
        }
        else if (key instanceof DSAPrivateKey || "DSA".equalsIgnoreCase(key.getAlgorithm()))
        {
            encOID = ENCRYPTION_DSA;
            if (!digestOID.equals(DIGEST_SHA1))
            {
                throw new IllegalArgumentException("can't mix DSA with anything but SHA1");
            }
        }
        else if ("ECDSA".equalsIgnoreCase(key.getAlgorithm()) || "EC".equalsIgnoreCase(key.getAlgorithm()))
        {
            encOID = (String)EC_ALGORITHMS.get(digestOID);
            if (encOID == null)
            {
                throw new IllegalArgumentException("can't mix ECDSA with anything but SHA family digests");
            }
        }
        else if (key instanceof GOST3410PrivateKey || "GOST3410".equalsIgnoreCase(key.getAlgorithm()))
        {
            encOID = ENCRYPTION_GOST3410;
        }
        else if ("ECGOST3410".equalsIgnoreCase(key.getAlgorithm()))
        {
            encOID = ENCRYPTION_ECGOST3410;
        }
        
        return encOID;
    }

    protected AlgorithmIdentifier getEncAlgorithmIdentifier(String encOid, Signature sig)
        throws IOException
    {
        if (NO_PARAMS.contains(encOid))
        {
            return new AlgorithmIdentifier(
                  new DERObjectIdentifier(encOid));
        }
        else
        {
            if (encOid.equals(CMSSignedGenerator.ENCRYPTION_RSA_PSS))
            {
                AlgorithmParameters sigParams = sig.getParameters();

                return new AlgorithmIdentifier(
                    new DERObjectIdentifier(encOid), ASN1Object.fromByteArray(sigParams.getEncoded()));
            }
            else
            {
                return new AlgorithmIdentifier(
                    new DERObjectIdentifier(encOid), new DERNull());
            }
        }
    }

    protected Map getBaseParameters(DERObjectIdentifier contentType, AlgorithmIdentifier digAlgId, byte[] hash)
    {
        Map param = new HashMap();
        param.put(CMSAttributeTableGenerator.CONTENT_TYPE, contentType);
        param.put(CMSAttributeTableGenerator.DIGEST_ALGORITHM_IDENTIFIER, digAlgId);
        param.put(CMSAttributeTableGenerator.DIGEST,  hash.clone());
        return param;
    }

    protected ASN1Set getAttributeSet(
        AttributeTable attr)
    {
        if (attr != null)
        {
            return new DERSet(attr.toASN1EncodableVector());
        }
        
        return null;
    }

    /**
     * add the certificates and CRLs contained in the given CertStore
     * to the pool that will be included in the encoded signature block.
     * <p>
     * Note: this assumes the CertStore will support null in the get
     * methods.
     * @param certStore CertStore containing the public key certificates and CRLs
     * @throws java.security.cert.CertStoreException  if an issue occurs processing the CertStore
     * @throws CMSException  if an issue occurse transforming data from the CertStore into the message
     * @deprecated use addCertificates and addCRLs
     */
    public void addCertificatesAndCRLs(
        CertStore certStore)
        throws CertStoreException, CMSException
    {
        certs.addAll(CMSUtils.getCertificatesFromStore(certStore));
        crls.addAll(CMSUtils.getCRLsFromStore(certStore));
    }

    public void addCertificates(
        Store certStore)
        throws CMSException
    {
        certs.addAll(CMSUtils.getCertificatesFromStore(certStore));
    }

    public void addCRLs(
        Store crlStore)
        throws CMSException
    {
        crls.addAll(CMSUtils.getCRLsFromStore(crlStore));
    }

    public void addAttributeCertificates(
        Store attrStore)
        throws CMSException
    {
        certs.addAll(CMSUtils.getAttributeCertificatesFromStore(attrStore));
    }

    /**
     * Add the attribute certificates contained in the passed in store to the
     * generator.
     *
     * @param store a store of Version 2 attribute certificates
     * @throws CMSException if an error occurse processing the store.
     * @deprecated use basic Store method
     */
    public void addAttributeCertificates(
        X509Store store)
        throws CMSException
    {
        try
        {
            for (Iterator it = store.getMatches(null).iterator(); it.hasNext();)
            {
                X509AttributeCertificate attrCert = (X509AttributeCertificate)it.next();

                certs.add(new DERTaggedObject(false, 2,
                             AttributeCertificate.getInstance(ASN1Object.fromByteArray(attrCert.getEncoded()))));
            }
        }
        catch (IllegalArgumentException e)
        {
            throw new CMSException("error processing attribute certs", e);
        }
        catch (IOException e)
        {
            throw new CMSException("error processing attribute certs", e);
        }
    }


    /**
     * Add a store of precalculated signers to the generator.
     *
     * @param signerStore store of signers
     */
    public void addSigners(
        SignerInformationStore    signerStore)
    {
        Iterator    it = signerStore.getSigners().iterator();

        while (it.hasNext())
        {
            _signers.add(it.next());
        }
    }

    public void addSignerInfoGenerator(SignerInfoGenerator infoGen)
    {
         signerGens.add(infoGen);
    }

    /**
     * Return a map of oids and byte arrays representing the digests calculated on the content during
     * the last generate.
     *
     * @return a map of oids (as String objects) and byte[] representing digests.
     */
    public Map getGeneratedDigests()
    {
        return new HashMap(digests);
    }

    static SignerIdentifier getSignerIdentifier(X509Certificate cert)
    {
        return new SignerIdentifier(CMSUtils.getIssuerAndSerialNumber(cert));
    }

    static SignerIdentifier getSignerIdentifier(byte[] subjectKeyIdentifier)
    {
        return new SignerIdentifier(new DEROctetString(subjectKeyIdentifier));    
    }
}
