package old.org.bouncycastle.asn1.esf;

import java.math.BigInteger;

import old.org.bouncycastle.asn1.ASN1Encodable;
import old.org.bouncycastle.asn1.ASN1EncodableVector;
import old.org.bouncycastle.asn1.ASN1Sequence;
import old.org.bouncycastle.asn1.DERInteger;
import old.org.bouncycastle.asn1.DERObject;
import old.org.bouncycastle.asn1.DERSequence;
import old.org.bouncycastle.asn1.DERUTCTime;
import old.org.bouncycastle.asn1.x500.X500Name;

/**
 * <pre>
 *  CrlIdentifier ::= SEQUENCE 
 * {
 *   crlissuer    Name,
 *   crlIssuedTime  UTCTime,
 *   crlNumber    INTEGER OPTIONAL
 * }
 * </pre>
 */
public class CrlIdentifier
    extends ASN1Encodable
{
    private X500Name crlIssuer;
    private DERUTCTime crlIssuedTime;
    private DERInteger crlNumber;

    public static CrlIdentifier getInstance(Object obj)
    {
        if (obj instanceof CrlIdentifier)
        {
            return (CrlIdentifier)obj;
        }
        else if (obj != null)
        {
            return new CrlIdentifier(ASN1Sequence.getInstance(obj));
        }

        throw new IllegalArgumentException("null value in getInstance");
    }

    private CrlIdentifier(ASN1Sequence seq)
    {
        if (seq.size() < 2 || seq.size() > 3)
        {
            throw new IllegalArgumentException();
        }
        this.crlIssuer = X500Name.getInstance(seq.getObjectAt(0));
        this.crlIssuedTime = DERUTCTime.getInstance(seq.getObjectAt(1));
        if (seq.size() > 2)
        {
            this.crlNumber = DERInteger.getInstance(seq.getObjectAt(2));
        }
    }

    public CrlIdentifier(X500Name crlIssuer, DERUTCTime crlIssuedTime)
    {
        this(crlIssuer, crlIssuedTime, null);
    }

    public CrlIdentifier(X500Name crlIssuer, DERUTCTime crlIssuedTime,
                         BigInteger crlNumber)
    {
        this.crlIssuer = crlIssuer;
        this.crlIssuedTime = crlIssuedTime;
        if (null != crlNumber)
        {
            this.crlNumber = new DERInteger(crlNumber);
        }
    }

    public X500Name getCrlIssuer()
    {
        return this.crlIssuer;
    }

    public DERUTCTime getCrlIssuedTime()
    {
        return this.crlIssuedTime;
    }

    public BigInteger getCrlNumber()
    {
        if (null == this.crlNumber)
        {
            return null;
        }
        return this.crlNumber.getValue();
    }

    public DERObject toASN1Object()
    {
        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(this.crlIssuer.toASN1Object());
        v.add(this.crlIssuedTime);
        if (null != this.crlNumber)
        {
            v.add(this.crlNumber);
        }
        return new DERSequence(v);
    }

}
