package old.org.bouncycastle.asn1.esf;

import old.org.bouncycastle.asn1.ASN1Choice;
import old.org.bouncycastle.asn1.ASN1Encodable;
import old.org.bouncycastle.asn1.ASN1OctetString;
import old.org.bouncycastle.asn1.DERObject;
import old.org.bouncycastle.asn1.DEROctetString;
import old.org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;

/**
 * <pre>
 * OtherHash ::= CHOICE {
 *    sha1Hash  OtherHashValue, -- This contains a SHA-1 hash
 *   otherHash  OtherHashAlgAndValue
 *  }
 * </pre>
 */
public class OtherHash
    extends ASN1Encodable
    implements ASN1Choice
{

    private ASN1OctetString sha1Hash;
    private OtherHashAlgAndValue otherHash;

    public static OtherHash getInstance(Object obj)
    {
        if (obj instanceof OtherHash)
        {
            return (OtherHash)obj;
        }
        if (obj instanceof ASN1OctetString)
        {
            return new OtherHash((ASN1OctetString)obj);
        }
        return new OtherHash(OtherHashAlgAndValue.getInstance(obj));
    }

    private OtherHash(ASN1OctetString sha1Hash)
    {
        this.sha1Hash = sha1Hash;
    }

    public OtherHash(OtherHashAlgAndValue otherHash)
    {
        this.otherHash = otherHash;
    }

    public OtherHash(byte[] sha1Hash)
    {
        this.sha1Hash = new DEROctetString(sha1Hash);
    }

    public AlgorithmIdentifier getHashAlgorithm()
    {
        if (null == this.otherHash)
        {
            return new AlgorithmIdentifier(OIWObjectIdentifiers.idSHA1);
        }
        return this.otherHash.getHashAlgorithm();
    }

    public byte[] getHashValue()
    {
        if (null == this.otherHash)
        {
            return this.sha1Hash.getOctets();
        }
        return this.otherHash.getHashValue().getOctets();
    }

    public DERObject toASN1Object()
    {
        if (null == this.otherHash)
        {
            return this.sha1Hash;
        }
        return this.otherHash.toASN1Object();
    }
}
