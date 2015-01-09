package old.org.bouncycastle.asn1.x9;

import old.org.bouncycastle.asn1.ASN1Encodable;
import old.org.bouncycastle.asn1.ASN1TaggedObject;
import old.org.bouncycastle.asn1.DERInteger;
import old.org.bouncycastle.asn1.DERObject;

public class DHPublicKey
    extends ASN1Encodable
{
    private DERInteger y;

    public static DHPublicKey getInstance(ASN1TaggedObject obj, boolean explicit)
    {
        return getInstance(DERInteger.getInstance(obj, explicit));
    }

    public static DHPublicKey getInstance(Object obj)
    {
        if (obj == null || obj instanceof DHPublicKey)
        {
            return (DHPublicKey)obj;
        }

        if (obj instanceof DERInteger)
        {
            return new DHPublicKey((DERInteger)obj);
        }

        throw new IllegalArgumentException("Invalid DHPublicKey: " + obj.getClass().getName());
    }

    public DHPublicKey(DERInteger y)
    {
        if (y == null)
        {
            throw new IllegalArgumentException("'y' cannot be null");
        }

        this.y = y;
    }

    public DERInteger getY()
    {
        return this.y;
    }

    public DERObject toASN1Object()
    {
        return this.y;
    }
}
