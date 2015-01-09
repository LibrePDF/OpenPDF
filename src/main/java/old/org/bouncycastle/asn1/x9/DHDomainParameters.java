package old.org.bouncycastle.asn1.x9;

import java.util.Enumeration;

import old.org.bouncycastle.asn1.ASN1Encodable;
import old.org.bouncycastle.asn1.ASN1EncodableVector;
import old.org.bouncycastle.asn1.ASN1Sequence;
import old.org.bouncycastle.asn1.ASN1TaggedObject;
import old.org.bouncycastle.asn1.DEREncodable;
import old.org.bouncycastle.asn1.DERInteger;
import old.org.bouncycastle.asn1.DERObject;
import old.org.bouncycastle.asn1.DERSequence;

public class DHDomainParameters
    extends ASN1Encodable
{
    private DERInteger p, g, q, j;
    private DHValidationParms validationParms;

    public static DHDomainParameters getInstance(ASN1TaggedObject obj, boolean explicit)
    {
        return getInstance(ASN1Sequence.getInstance(obj, explicit));
    }

    public static DHDomainParameters getInstance(Object obj)
    {
        if (obj == null || obj instanceof DHDomainParameters)
        {
            return (DHDomainParameters)obj;
        }

        if (obj instanceof ASN1Sequence)
        {
            return new DHDomainParameters((ASN1Sequence)obj);
        }

        throw new IllegalArgumentException("Invalid DHDomainParameters: "
            + obj.getClass().getName());
    }

    public DHDomainParameters(DERInteger p, DERInteger g, DERInteger q, DERInteger j,
        DHValidationParms validationParms)
    {
        if (p == null)
        {
            throw new IllegalArgumentException("'p' cannot be null");
        }
        if (g == null)
        {
            throw new IllegalArgumentException("'g' cannot be null");
        }
        if (q == null)
        {
            throw new IllegalArgumentException("'q' cannot be null");
        }

        this.p = p;
        this.g = g;
        this.q = q;
        this.j = j;
        this.validationParms = validationParms;
    }

    private DHDomainParameters(ASN1Sequence seq)
    {
        if (seq.size() < 3 || seq.size() > 5)
        {
            throw new IllegalArgumentException("Bad sequence size: " + seq.size());
        }

        Enumeration e = seq.getObjects();
        this.p = DERInteger.getInstance(e.nextElement());
        this.g = DERInteger.getInstance(e.nextElement());
        this.q = DERInteger.getInstance(e.nextElement());

        DEREncodable next = getNext(e);

        if (next != null && next instanceof DERInteger)
        {
            this.j = DERInteger.getInstance(next);
            next = getNext(e);
        }

        if (next != null)
        {
            this.validationParms = DHValidationParms.getInstance(next.getDERObject());
        }
    }

    private static DEREncodable getNext(Enumeration e)
    {
        return e.hasMoreElements() ? (DEREncodable)e.nextElement() : null;
    }

    public DERInteger getP()
    {
        return this.p;
    }

    public DERInteger getG()
    {
        return this.g;
    }

    public DERInteger getQ()
    {
        return this.q;
    }

    public DERInteger getJ()
    {
        return this.j;
    }

    public DHValidationParms getValidationParms()
    {
        return this.validationParms;
    }

    public DERObject toASN1Object()
    {
        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(this.p);
        v.add(this.g);
        v.add(this.q);

        if (this.j != null)
        {
            v.add(this.j);
        }

        if (this.validationParms != null)
        {
            v.add(this.validationParms);
        }

        return new DERSequence(v);
    }
}
