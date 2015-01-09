package old.org.bouncycastle.asn1.x509;

import old.org.bouncycastle.asn1.ASN1Encodable;
import old.org.bouncycastle.asn1.ASN1Sequence;
import old.org.bouncycastle.asn1.ASN1TaggedObject;
import old.org.bouncycastle.asn1.DERBitString;
import old.org.bouncycastle.asn1.DERInteger;
import old.org.bouncycastle.asn1.DERObject;
import old.org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;

/**
 * an X509Certificate structure.
 * <pre>
 *  Certificate ::= SEQUENCE {
 *      tbsCertificate          TBSCertificate,
 *      signatureAlgorithm      AlgorithmIdentifier,
 *      signature               BIT STRING
 *  }
 * </pre>
 */
public class X509CertificateStructure
    extends ASN1Encodable
    implements X509ObjectIdentifiers, PKCSObjectIdentifiers
{
    ASN1Sequence  seq;
    TBSCertificateStructure tbsCert;
    AlgorithmIdentifier     sigAlgId;
    DERBitString            sig;

    public static X509CertificateStructure getInstance(
        ASN1TaggedObject obj,
        boolean          explicit)
    {
        return getInstance(ASN1Sequence.getInstance(obj, explicit));
    }

    public static X509CertificateStructure getInstance(
        Object  obj)
    {
        if (obj instanceof X509CertificateStructure)
        {
            return (X509CertificateStructure)obj;
        }
        else if (obj != null)
        {
            return new X509CertificateStructure(ASN1Sequence.getInstance(obj));
        }

        return null;
    }

    public X509CertificateStructure(
        ASN1Sequence  seq)
    {
        this.seq = seq;

        //
        // correct x509 certficate
        //
        if (seq.size() == 3)
        {
            tbsCert = TBSCertificateStructure.getInstance(seq.getObjectAt(0));
            sigAlgId = AlgorithmIdentifier.getInstance(seq.getObjectAt(1));

            sig = DERBitString.getInstance(seq.getObjectAt(2));
        }
        else
        {
            throw new IllegalArgumentException("sequence wrong size for a certificate");
        }
    }

    public TBSCertificateStructure getTBSCertificate()
    {
        return tbsCert;
    }

    public int getVersion()
    {
        return tbsCert.getVersion();
    }

    public DERInteger getSerialNumber()
    {
        return tbsCert.getSerialNumber();
    }

    public X509Name getIssuer()
    {
        return tbsCert.getIssuer();
    }

    public Time getStartDate()
    {
        return tbsCert.getStartDate();
    }

    public Time getEndDate()
    {
        return tbsCert.getEndDate();
    }

    public X509Name getSubject()
    {
        return tbsCert.getSubject();
    }

    public SubjectPublicKeyInfo getSubjectPublicKeyInfo()
    {
        return tbsCert.getSubjectPublicKeyInfo();
    }

    public AlgorithmIdentifier getSignatureAlgorithm()
    {
        return sigAlgId;
    }

    public DERBitString getSignature()
    {
        return sig;
    }

    public DERObject toASN1Object()
    {
        return seq;
    }
}
