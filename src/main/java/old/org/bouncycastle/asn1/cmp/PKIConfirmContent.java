package old.org.bouncycastle.asn1.cmp;

import old.org.bouncycastle.asn1.ASN1Encodable;
import old.org.bouncycastle.asn1.ASN1Null;
import old.org.bouncycastle.asn1.DERNull;
import old.org.bouncycastle.asn1.DERObject;

public class PKIConfirmContent
    extends ASN1Encodable
{
    private ASN1Null val;

    private PKIConfirmContent(ASN1Null val)
    {
        this.val = val;
    }

    public static PKIConfirmContent getInstance(Object o)
    {
        if (o instanceof PKIConfirmContent)
        {
            return (PKIConfirmContent)o;
        }

        if (o instanceof ASN1Null)
        {
            return new PKIConfirmContent((ASN1Null)o);
        }

        throw new IllegalArgumentException("Invalid object: " + o.getClass().getName());
    }

    public PKIConfirmContent()
    {
        val = DERNull.INSTANCE;
    }

    /**
     * <pre>
     * PKIConfirmContent ::= NULL
     * </pre>
     * @return a basic ASN.1 object representation.
     */
    public DERObject toASN1Object()
    {
        return val;
    }
}
