package old.org.bouncycastle.asn1.icao;

import old.org.bouncycastle.asn1.ASN1Encodable;
import old.org.bouncycastle.asn1.ASN1EncodableVector;
import old.org.bouncycastle.asn1.ASN1Sequence;
import old.org.bouncycastle.asn1.ASN1Set;
import old.org.bouncycastle.asn1.DERInteger;
import old.org.bouncycastle.asn1.DERObject;
import old.org.bouncycastle.asn1.DERSequence;
import old.org.bouncycastle.asn1.DERSet;
import old.org.bouncycastle.asn1.x509.X509CertificateStructure;

/**
 * The CscaMasterList object. This object can be wrapped in a
 * CMSSignedData to be published in LDAP.
 *
 * <pre>
 * CscaMasterList ::= SEQUENCE {
 *   version                CscaMasterListVersion,
 *   certList               SET OF Certificate }
 *   
 * CscaMasterListVersion :: INTEGER {v0(0)}
 * </pre>
 */

public class CscaMasterList 
    extends ASN1Encodable 
{
    private DERInteger version = new DERInteger(0);
    private X509CertificateStructure[] certList;

    public static CscaMasterList getInstance(
        Object obj)
    {
        if (obj instanceof CscaMasterList)
        {
            return (CscaMasterList)obj;
        }
        else if (obj != null)
        {
            return new CscaMasterList(ASN1Sequence.getInstance(obj));            
        }

        return null;
    }    
    
    private CscaMasterList(
        ASN1Sequence seq)
    {
        if (seq == null || seq.size() == 0)
        {
            throw new IllegalArgumentException(
                    "null or empty sequence passed.");
        }
        if (seq.size() != 2) {
            throw new IllegalArgumentException(
                    "Incorrect sequence size: " + seq.size());
        }
        
        version = DERInteger.getInstance(seq.getObjectAt(0));
        ASN1Set certSet = ASN1Set.getInstance(seq.getObjectAt(1));
        certList = new X509CertificateStructure[certSet.size()];
        for (int i = 0; i < certList.length; i++) {
            certList[i]
                = X509CertificateStructure.getInstance(certSet.getObjectAt(i));
        }
    }

    public CscaMasterList(
        X509CertificateStructure[] certStructs)
    {
        certList = copyCertList(certStructs);
    }

    public int getVersion() {
        return version.getValue().intValue();
    }

    public X509CertificateStructure[] getCertStructs()
    {
        return copyCertList(certList);
    }

    private X509CertificateStructure[] copyCertList(X509CertificateStructure[] orig)
    {
        X509CertificateStructure[] certs = new X509CertificateStructure[orig.length];

        for (int i = 0; i != certs.length; i++)
        {
            certs[i] = orig[i];
        }

        return certs;
    }

    public DERObject toASN1Object() 
    {
        ASN1EncodableVector seq = new ASN1EncodableVector();

        seq.add(version);

        ASN1EncodableVector certSet = new ASN1EncodableVector();
        for (int i = 0; i < certList.length; i++) 
        {
            certSet.add(certList[i]);
        }            
        seq.add(new DERSet(certSet));                   

        return new DERSequence(seq);
    }          
}
