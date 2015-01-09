package old.org.bouncycastle.asn1.x9;

import old.org.bouncycastle.asn1.ASN1Encodable;
import old.org.bouncycastle.asn1.ASN1EncodableVector;
import old.org.bouncycastle.asn1.ASN1Sequence;
import old.org.bouncycastle.asn1.ASN1TaggedObject;
import old.org.bouncycastle.asn1.DERBitString;
import old.org.bouncycastle.asn1.DERInteger;
import old.org.bouncycastle.asn1.DERObject;
import old.org.bouncycastle.asn1.DERSequence;

public class DHValidationParms extends ASN1Encodable
{
    private DERBitString seed;
    private DERInteger pgenCounter;

    public static DHValidationParms getInstance(ASN1TaggedObject obj, boolean explicit)
    {
        return getInstance(ASN1Sequence.getInstance(obj, explicit));
    }

    public static DHValidationParms getInstance(Object obj)
    {
        if (obj == null || obj instanceof DHDomainParameters)
        {
            return (DHValidationParms)obj;
        }

        if (obj instanceof ASN1Sequence)
        {
            return new DHValidationParms((ASN1Sequence)obj);
        }

        throw new IllegalArgumentException("Invalid DHValidationParms: " + obj.getClass().getName());
    }

    public DHValidationParms(DERBitString seed, DERInteger pgenCounter)
    {
        if (seed == null)
        {
            throw new IllegalArgumentException("'seed' cannot be null");
        }
        if (pgenCounter == null)
        {
            throw new IllegalArgumentException("'pgenCounter' cannot be null");
        }

        this.seed = seed;
        this.pgenCounter = pgenCounter;
    }

    private DHValidationParms(ASN1Sequence seq)
    {
        if (seq.size() != 2)
        {
            throw new IllegalArgumentException("Bad sequence size: " + seq.size());
        }

        this.seed = DERBitString.getInstance(seq.getObjectAt(0));
        this.pgenCounter = DERInteger.getInstance(seq.getObjectAt(1));
    }

    public DERBitString getSeed()
    {
        return this.seed;
    }

    public DERInteger getPgenCounter()
    {
        return this.pgenCounter;
    }

    public DERObject toASN1Object()
    {
        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(this.seed);
        v.add(this.pgenCounter);
        return new DERSequence(v);
    }
}
