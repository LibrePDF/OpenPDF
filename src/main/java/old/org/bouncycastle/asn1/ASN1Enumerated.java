package old.org.bouncycastle.asn1;

import java.math.BigInteger;

public class ASN1Enumerated
    extends DEREnumerated
{
    ASN1Enumerated(byte[] bytes)
    {
        super(bytes);
    }

    public ASN1Enumerated(BigInteger value)
    {
        super(value);
    }

    public ASN1Enumerated(int value)
    {
        super(value);
    }
}
