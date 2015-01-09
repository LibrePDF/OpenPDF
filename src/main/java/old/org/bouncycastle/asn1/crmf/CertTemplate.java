package old.org.bouncycastle.asn1.crmf;

import java.util.Enumeration;

import old.org.bouncycastle.asn1.ASN1Encodable;
import old.org.bouncycastle.asn1.ASN1Sequence;
import old.org.bouncycastle.asn1.ASN1TaggedObject;
import old.org.bouncycastle.asn1.DERBitString;
import old.org.bouncycastle.asn1.DERInteger;
import old.org.bouncycastle.asn1.DERObject;
import old.org.bouncycastle.asn1.x500.X500Name;
import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import old.org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import old.org.bouncycastle.asn1.x509.X509Extensions;

public class CertTemplate
    extends ASN1Encodable
{
    private ASN1Sequence seq;

    private DERInteger version;
    private DERInteger serialNumber;
    private AlgorithmIdentifier signingAlg;
    private X500Name issuer;
    private OptionalValidity validity;
    private X500Name subject;
    private SubjectPublicKeyInfo publicKey;
    private DERBitString issuerUID;
    private DERBitString subjectUID;
    private X509Extensions extensions;

    private CertTemplate(ASN1Sequence seq)
    {
        this.seq = seq;

        Enumeration en = seq.getObjects();
        while (en.hasMoreElements())
        {
            ASN1TaggedObject tObj = (ASN1TaggedObject)en.nextElement();

            switch (tObj.getTagNo())
            {
            case 0:
                version = DERInteger.getInstance(tObj, false);
                break;
            case 1:
                serialNumber = DERInteger.getInstance(tObj, false);
                break;
            case 2:
                signingAlg = AlgorithmIdentifier.getInstance(tObj, false);
                break;
            case 3:
                issuer = X500Name.getInstance(tObj, true); // CHOICE
                break;
            case 4:
                validity = OptionalValidity.getInstance(ASN1Sequence.getInstance(tObj, false));
                break;
            case 5:
                subject = X500Name.getInstance(tObj, true); // CHOICE
                break;
            case 6:
                publicKey = SubjectPublicKeyInfo.getInstance(tObj, false);
                break;
            case 7:
                issuerUID = DERBitString.getInstance(tObj, false);
                break;
            case 8:
                subjectUID = DERBitString.getInstance(tObj, false);
                break;
            case 9:
                extensions = X509Extensions.getInstance(tObj, false);
                break;
            default:
                throw new IllegalArgumentException("unknown tag: " + tObj.getTagNo());
            }
        }
    }

    public static CertTemplate getInstance(Object o)
    {
        if (o instanceof CertTemplate)
        {
            return (CertTemplate)o;
        }
        else if (o != null)
        {
            return new CertTemplate(ASN1Sequence.getInstance(o));
        }

        return null;
    }

    public int getVersion()
    {
        return version.getValue().intValue();
    }

    public DERInteger getSerialNumber()
    {
        return serialNumber;
    }

    public AlgorithmIdentifier getSigningAlg()
    {
        return signingAlg;
    }

    public X500Name getIssuer()
    {
        return issuer;
    }

    public OptionalValidity getValidity()
    {
        return validity;
    }

    public X500Name getSubject()
    {
        return subject;
    }

    public SubjectPublicKeyInfo getPublicKey()
    {
        return publicKey;
    }

    public void setPublicKey(SubjectPublicKeyInfo spki)
    {
        publicKey = spki;
    }

    public DERBitString getIssuerUID()
    {
        return issuerUID;
    }

    public DERBitString getSubjectUID()
    {
        return subjectUID;
    }

    public X509Extensions getExtensions()
    {
        return extensions;
    }

    /**
     * <pre>
     *  CertTemplate ::= SEQUENCE {
     *      version      [0] Version               OPTIONAL,
     *      serialNumber [1] INTEGER               OPTIONAL,
     *      signingAlg   [2] AlgorithmIdentifier   OPTIONAL,
     *      issuer       [3] Name                  OPTIONAL,
     *      validity     [4] OptionalValidity      OPTIONAL,
     *      subject      [5] Name                  OPTIONAL,
     *      publicKey    [6] SubjectPublicKeyInfo  OPTIONAL,
     *      issuerUID    [7] UniqueIdentifier      OPTIONAL,
     *      subjectUID   [8] UniqueIdentifier      OPTIONAL,
     *      extensions   [9] Extensions            OPTIONAL }
     * </pre>
     * @return a basic ASN.1 object representation.
     */
    public DERObject toASN1Object()
    {
        return seq;
    }
}
