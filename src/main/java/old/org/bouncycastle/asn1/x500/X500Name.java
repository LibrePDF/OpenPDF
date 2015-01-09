package old.org.bouncycastle.asn1.x500;

import java.util.Enumeration;

import old.org.bouncycastle.asn1.ASN1Choice;
import old.org.bouncycastle.asn1.ASN1Encodable;
import old.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import old.org.bouncycastle.asn1.ASN1Sequence;
import old.org.bouncycastle.asn1.ASN1TaggedObject;
import old.org.bouncycastle.asn1.DEREncodable;
import old.org.bouncycastle.asn1.DERObject;
import old.org.bouncycastle.asn1.DERSequence;
import old.org.bouncycastle.asn1.x500.style.BCStyle;
import old.org.bouncycastle.asn1.x509.X509Name;

/**
 * <pre>
 *     Name ::= CHOICE {
 *                       RDNSequence }
 *
 *     RDNSequence ::= SEQUENCE OF RelativeDistinguishedName
 *
 *     RelativeDistinguishedName ::= SET SIZE (1..MAX) OF AttributeTypeAndValue
 *
 *     AttributeTypeAndValue ::= SEQUENCE {
 *                                   type  OBJECT IDENTIFIER,
 *                                   value ANY }
 * </pre>
 */
public class X500Name
    extends ASN1Encodable
    implements ASN1Choice
{
    private static X500NameStyle    defaultStyle = BCStyle.INSTANCE;

    private boolean                 isHashCodeCalculated;
    private int                     hashCodeValue;

    private X500NameStyle style;
    private RDN[] rdns;

    public X500Name(X500NameStyle style, X500Name name)
    {
        this.rdns = name.rdns;
        this.style = style;
    }

    /**
     * Return a X509Name based on the passed in tagged object.
     * 
     * @param obj tag object holding name.
     * @param explicit true if explicitly tagged false otherwise.
     * @return the X509Name
     */
    public static X500Name getInstance(
        ASN1TaggedObject obj,
        boolean          explicit)
    {
        // must be true as choice item
        return getInstance(ASN1Sequence.getInstance(obj, true));
    }

    public static X500Name getInstance(
        Object  obj)
    {
        if (obj instanceof X500Name)
        {
            return (X500Name)obj;
        }
        else if (obj instanceof X509Name)
        {
            return new X500Name(ASN1Sequence.getInstance(((X509Name)obj).getDERObject()));
        }
        else if (obj != null)
        {
            return new X500Name(ASN1Sequence.getInstance(obj));
        }

        return null;
    }

    /**
     * Constructor from ASN1Sequence
     *
     * the principal will be a list of constructed sets, each containing an (OID, String) pair.
     */
    private X500Name(
        ASN1Sequence  seq)
    {
        this(defaultStyle, seq);
    }

    private X500Name(
        X500NameStyle style,
        ASN1Sequence  seq)
    {
        this.style = style;
        this.rdns = new RDN[seq.size()];

        int index = 0;

        for (Enumeration e = seq.getObjects(); e.hasMoreElements();)
        {
            rdns[index++] = RDN.getInstance(e.nextElement());
        }
    }

    public X500Name(
        RDN[] rDNs)
    {
        this(defaultStyle, rDNs);
    }

    public X500Name(
        X500NameStyle style,
        RDN[]         rDNs)
    {
        this.rdns = rDNs;
        this.style = style;
    }

    public X500Name(
        String dirName)
    {
        this(defaultStyle, dirName);
    }

    public X500Name(
        X500NameStyle style,
        String        dirName)
    {
        this(style.fromString(dirName));

        this.style = style;
    }

    /**
     * return an array of RDNs in structure order.
     *
     * @return an array of RDN objects.
     */
    public RDN[] getRDNs()
    {
        RDN[] tmp = new RDN[this.rdns.length];

        System.arraycopy(rdns, 0, tmp, 0, tmp.length);

        return tmp;
    }

    /**
     * return an array of RDNs containing the attribute type given by OID in structure order.
     *
     * @param oid the type OID we are looking for.
     * @return an array, possibly zero length, of RDN objects.
     */
    public RDN[] getRDNs(ASN1ObjectIdentifier oid)
    {
        RDN[] res = new RDN[rdns.length];
        int   count = 0;

        for (int i = 0; i != rdns.length; i++)
        {
            RDN rdn = rdns[i];

            if (rdn.isMultiValued())
            {
                AttributeTypeAndValue[] attr = rdn.getTypesAndValues();
                for (int j = 0; j != attr.length; j++)
                {
                    if (attr[j].getType().equals(oid))
                    {
                        res[count++] = rdn;
                        break;
                    }
                }
            }
            else
            {
                if (rdn.getFirst().getType().equals(oid))
                {
                    res[count++] = rdn;
                }
            }
        }

        RDN[] tmp = new RDN[count];

        System.arraycopy(res, 0, tmp, 0, tmp.length);

        return tmp;
    }

    public DERObject toASN1Object()
    {
        return new DERSequence(rdns);
    }

    public int hashCode()
    {
        if (isHashCodeCalculated)
        {
            return hashCodeValue;
        }

        isHashCodeCalculated = true;

        hashCodeValue = style.calculateHashCode(this);

        return hashCodeValue;
    }

    /**
     * test for equality - note: case is ignored.
     */
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }

        if (!(obj instanceof X500Name || obj instanceof ASN1Sequence))
        {
            return false;
        }
        
        DERObject derO = ((DEREncodable)obj).getDERObject();

        if (this.getDERObject().equals(derO))
        {
            return true;
        }

        try
        {
            return style.areEqual(this, new X500Name(ASN1Sequence.getInstance(((DEREncodable)obj).getDERObject())));
        }
        catch (Exception e)
        {
            return false;
        }
    }
    
    public String toString()
    {
        return style.toString(this);
    }

    /**
     * Set the default style for X500Name construction.
     *
     * @param style  an X500NameStyle
     */
    public static void setDefaultStyle(X500NameStyle style)
    {
        if (style == null)
        {
            throw new NullPointerException("cannot set style to null");
        }

        defaultStyle = style;
    }

    /**
     * Return the current default style.
     *
     * @return default style for X500Name construction.
     */
    public static X500NameStyle getDefaultStyle()
    {
        return defaultStyle;
    }
}
