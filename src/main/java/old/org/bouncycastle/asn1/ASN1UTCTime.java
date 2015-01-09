package old.org.bouncycastle.asn1;

import java.util.Date;

public class ASN1UTCTime
    extends DERUTCTime
{
    ASN1UTCTime(byte[] bytes)
    {
        super(bytes);
    }

    public ASN1UTCTime(Date time)
    {
        super(time);
    }

    public ASN1UTCTime(String time)
    {
        super(time);
    }
}
