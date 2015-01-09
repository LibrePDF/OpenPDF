package old.org.bouncycastle.ocsp;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

import old.org.bouncycastle.asn1.ASN1InputStream;
import old.org.bouncycastle.asn1.ASN1OctetString;
import old.org.bouncycastle.asn1.DERInteger;
import old.org.bouncycastle.asn1.DERNull;
import old.org.bouncycastle.asn1.DERObjectIdentifier;
import old.org.bouncycastle.asn1.DEROctetString;
import old.org.bouncycastle.asn1.ocsp.CertID;
import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import old.org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import old.org.bouncycastle.jce.PrincipalUtil;
import old.org.bouncycastle.jce.X509Principal;

public class CertificateID
{
    public static final String HASH_SHA1 = "1.3.14.3.2.26";

    private final CertID id;

    public CertificateID(
        CertID id)
    {
        if (id == null)
        {
            throw new IllegalArgumentException("'id' cannot be null");
        }
        this.id = id;
    }

    /**
     * create from an issuer certificate and the serial number of the
     * certificate it signed.
     *
     * @param hashAlgorithm hash algorithm to use
     * @param issuerCert issuing certificate
     * @param number serial number
     * @param provider provider to use for hashAlgorithm, null if the default one should be used.
     *
     * @exception OCSPException if any problems occur creating the id fields.
     */
    public CertificateID(
        String          hashAlgorithm,
        X509Certificate issuerCert,
        BigInteger      number,
        String          provider)
        throws OCSPException
    {
        AlgorithmIdentifier hashAlg = new AlgorithmIdentifier(
            new DERObjectIdentifier(hashAlgorithm), DERNull.INSTANCE);

        this.id = createCertID(hashAlg, issuerCert, new DERInteger(number), provider);
    }

    /**
     * create using the BC provider
     */
    public CertificateID(
        String          hashAlgorithm,
        X509Certificate issuerCert,
        BigInteger      number)
        throws OCSPException
    {
        this(hashAlgorithm, issuerCert, number, "BC");
    }

    public String getHashAlgOID()
    {
        return id.getHashAlgorithm().getObjectId().getId();
    }

    public byte[] getIssuerNameHash()
    {
        return id.getIssuerNameHash().getOctets();
    }

    public byte[] getIssuerKeyHash()
    {
        return id.getIssuerKeyHash().getOctets();
    }

    /**
     * return the serial number for the certificate associated
     * with this request.
     */
    public BigInteger getSerialNumber()
    {
        return id.getSerialNumber().getValue();
    }

    public boolean matchesIssuer(X509Certificate issuerCert, String provider)
        throws OCSPException
    {
        return createCertID(id.getHashAlgorithm(), issuerCert, id.getSerialNumber(), provider)
            .equals(id);
    }

    public CertID toASN1Object()
    {
        return id;
    }

    public boolean equals(
        Object  o)
    {
        if (!(o instanceof CertificateID))
        {
            return false;
        }

        CertificateID   obj = (CertificateID)o;

        return id.getDERObject().equals(obj.id.getDERObject());
    }

    public int hashCode()
    {
        return id.getDERObject().hashCode();
    }

    /**
     * Create a new CertificateID for a new serial number derived from a previous one
     * calculated for the same CA certificate.
     *
     * @param original the previously calculated CertificateID for the CA.
     * @param newSerialNumber the serial number for the new certificate of interest.
     *
     * @return a new CertificateID for newSerialNumber
     */
    public static CertificateID deriveCertificateID(CertificateID original, BigInteger newSerialNumber)
    {
        return new CertificateID(new CertID(original.id.getHashAlgorithm(), original.id.getIssuerNameHash(), original.id.getIssuerKeyHash(), new DERInteger(newSerialNumber)));
    }

    private static CertID createCertID(AlgorithmIdentifier hashAlg, X509Certificate issuerCert,
        DERInteger serialNumber, String provider)
        throws OCSPException
    {
        try
        {
            MessageDigest digest = OCSPUtil.createDigestInstance(hashAlg.getAlgorithm() .getId(),
                provider);

            X509Principal issuerName = PrincipalUtil.getSubjectX509Principal(issuerCert);

            digest.update(issuerName.getEncoded());

            ASN1OctetString issuerNameHash = new DEROctetString(digest.digest());
            PublicKey issuerKey = issuerCert.getPublicKey();

            ASN1InputStream aIn = new ASN1InputStream(issuerKey.getEncoded());
            SubjectPublicKeyInfo info = SubjectPublicKeyInfo.getInstance(aIn.readObject());

            digest.update(info.getPublicKeyData().getBytes());

            ASN1OctetString issuerKeyHash = new DEROctetString(digest.digest());

            return new CertID(hashAlg, issuerNameHash, issuerKeyHash, serialNumber);
        }
        catch (Exception e)
        {
            throw new OCSPException("problem creating ID: " + e, e);
        }
    }
}
