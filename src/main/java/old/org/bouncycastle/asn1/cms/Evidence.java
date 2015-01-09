package old.org.bouncycastle.asn1.cms;

import old.org.bouncycastle.asn1.ASN1Choice;
import old.org.bouncycastle.asn1.ASN1Encodable;
import old.org.bouncycastle.asn1.ASN1TaggedObject;
import old.org.bouncycastle.asn1.DERObject;
import old.org.bouncycastle.asn1.DERTaggedObject;

public class Evidence
    extends ASN1Encodable
    implements ASN1Choice
{
    private TimeStampTokenEvidence tstEvidence;

    public Evidence(TimeStampTokenEvidence tstEvidence)
    {
        this.tstEvidence = tstEvidence;
    }

    private Evidence(ASN1TaggedObject tagged)
    {
        if (tagged.getTagNo() == 0)
        {
            this.tstEvidence = TimeStampTokenEvidence.getInstance(tagged, false);
        }
    }

    public static Evidence getInstance(Object obj)
    {
        if (obj instanceof Evidence)
        {
            return (Evidence)obj;
        }
        else if (obj instanceof ASN1TaggedObject)
        {
            return new Evidence(ASN1TaggedObject.getInstance(obj));
        }

        throw new IllegalArgumentException("unknown object in getInstance");
    }

    public TimeStampTokenEvidence getTstEvidence()
    {
        return tstEvidence;
    }

    public DERObject toASN1Object()
    {
       if (tstEvidence != null)
       {
           return new DERTaggedObject(false, 0, tstEvidence);
       }

       return null;
    }
}
