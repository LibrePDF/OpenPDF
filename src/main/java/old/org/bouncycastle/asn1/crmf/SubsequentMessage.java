package old.org.bouncycastle.asn1.crmf;

import old.org.bouncycastle.asn1.DERInteger;

public class SubsequentMessage
    extends DERInteger
{
    public static final SubsequentMessage encrCert = new SubsequentMessage(0);
    public static final SubsequentMessage challengeResp = new SubsequentMessage(1);
    
    private SubsequentMessage(int value)
    {
        super(value);
    }

    public static SubsequentMessage valueOf(int value)
    {
        if (value == 0)
        {
            return encrCert;
        }
        if (value == 1)
        {
            return challengeResp;
        }

        throw new IllegalArgumentException("unknown value: " + value);
    }
}
