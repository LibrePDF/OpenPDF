package old.org.bouncycastle.asn1;

import java.util.Vector;

/**
 * the parent class for this will eventually disappear. Use this one!
 */
public class ASN1EncodableVector
    extends DEREncodableVector
{
    Vector v = new Vector();

    public ASN1EncodableVector()
    {

    }

    public void add(DEREncodable obj)
    {
        v.addElement(obj);
    }

    public DEREncodable get(int i)
    {
        return (DEREncodable)v.elementAt(i);
    }

    public int size()
    {
        return v.size();
    }
}
