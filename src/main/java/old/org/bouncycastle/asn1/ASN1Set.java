package old.org.bouncycastle.asn1;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

abstract public class ASN1Set
    extends ASN1Object
{
    protected Vector set = new Vector();

    /**
     * return an ASN1Set from the given object.
     *
     * @param obj the object we want converted.
     * @exception IllegalArgumentException if the object cannot be converted.
     */
    public static ASN1Set getInstance(
        Object  obj)
    {
        if (obj == null || obj instanceof ASN1Set)
        {
            return (ASN1Set)obj;
        }

        throw new IllegalArgumentException("unknown object in getInstance: " + obj.getClass().getName());
    }

    /**
     * Return an ASN1 set from a tagged object. There is a special
     * case here, if an object appears to have been explicitly tagged on 
     * reading but we were expecting it to be implicitly tagged in the 
     * normal course of events it indicates that we lost the surrounding
     * set - so we need to add it back (this will happen if the tagged
     * object is a sequence that contains other sequences). If you are
     * dealing with implicitly tagged sets you really <b>should</b>
     * be using this method.
     *
     * @param obj the tagged object.
     * @param explicit true if the object is meant to be explicitly tagged
     *          false otherwise.
     * @exception IllegalArgumentException if the tagged object cannot
     *          be converted.
     */
    public static ASN1Set getInstance(
        ASN1TaggedObject    obj,
        boolean             explicit)
    {
        if (explicit)
        {
            if (!obj.isExplicit())
            {
                throw new IllegalArgumentException("object implicit - explicit expected.");
            }

            return (ASN1Set)obj.getObject();
        }
        else
        {
            //
            // constructed object which appears to be explicitly tagged
            // and it's really implicit means we have to add the
            // surrounding sequence.
            //
            if (obj.isExplicit())
            {
                ASN1Set    set = new DERSet(obj.getObject());

                return set;
            }
            else
            {
                if (obj.getObject() instanceof ASN1Set)
                {
                    return (ASN1Set)obj.getObject();
                }

                //
                // in this case the parser returns a sequence, convert it
                // into a set.
                //
                ASN1EncodableVector  v = new ASN1EncodableVector();

                if (obj.getObject() instanceof ASN1Sequence)
                {
                    ASN1Sequence s = (ASN1Sequence)obj.getObject();
                    Enumeration e = s.getObjects();

                    while (e.hasMoreElements())
                    {
                        v.add((DEREncodable)e.nextElement());
                    }

                    return new DERSet(v, false);
                }
            }
        }

        throw new IllegalArgumentException("unknown object in getInstance: " + obj.getClass().getName());
    }

    public ASN1Set()
    {
    }

    public Enumeration getObjects()
    {
        return set.elements();
    }

    /**
     * return the object at the set position indicated by index.
     *
     * @param index the set number (starting at zero) of the object
     * @return the object at the set position indicated by index.
     */
    public DEREncodable getObjectAt(
        int index)
    {
        return (DEREncodable)set.elementAt(index);
    }

    /**
     * return the number of objects in this set.
     *
     * @return the number of objects in this set.
     */
    public int size()
    {
        return set.size();
    }

    public ASN1Encodable[] toArray()
    {
        ASN1Encodable[] values = new ASN1Encodable[this.size()];

        for (int i = 0; i != this.size(); i++)
        {
            values[i] = (ASN1Encodable)this.getObjectAt(i);
        }

        return values;
    }

    public ASN1SetParser parser()
    {
        final ASN1Set outer = this;

        return new ASN1SetParser()
        {
            private final int max = size();

            private int index;

            public DEREncodable readObject() throws IOException
            {
                if (index == max)
                {
                    return null;
                }

                DEREncodable obj = getObjectAt(index++);
                if (obj instanceof ASN1Sequence)
                {
                    return ((ASN1Sequence)obj).parser();
                }
                if (obj instanceof ASN1Set)
                {
                    return ((ASN1Set)obj).parser();
                }

                return obj;
            }

            public DERObject getLoadedObject()
            {
                return outer;
            }

            public DERObject getDERObject()
            {
                return outer;
            }
        };
    }

    public int hashCode()
    {
        Enumeration             e = this.getObjects();
        int                     hashCode = size();

        while (e.hasMoreElements())
        {
            Object o = getNext(e);
            hashCode *= 17;

            hashCode ^= o.hashCode();
        }

        return hashCode;
    }

    boolean asn1Equals(
        DERObject  o)
    {
        if (!(o instanceof ASN1Set))
        {
            return false;
        }

        ASN1Set   other = (ASN1Set)o;

        if (this.size() != other.size())
        {
            return false;
        }

        Enumeration s1 = this.getObjects();
        Enumeration s2 = other.getObjects();

        while (s1.hasMoreElements())
        {
            DEREncodable  obj1 = getNext(s1);
            DEREncodable  obj2 = getNext(s2);

            DERObject  o1 = obj1.getDERObject();
            DERObject  o2 = obj2.getDERObject();

            if (o1 == o2 || o1.equals(o2))
            {
                continue;
            }

            return false;
        }

        return true;
    }

    private DEREncodable getNext(Enumeration e)
    {
        DEREncodable encObj = (DEREncodable)e.nextElement();

        // unfortunately null was allowed as a substitute for DER null
        if (encObj == null)
        {
            return DERNull.INSTANCE;
        }

        return encObj;
    }

    /**
     * return true if a <= b (arrays are assumed padded with zeros).
     */
    private boolean lessThanOrEqual(
         byte[] a,
         byte[] b)
    {
        int len = Math.min(a.length, b.length);
        for (int i = 0; i != len; ++i)
        {
            if (a[i] != b[i])
            {
                return (a[i] & 0xff) < (b[i] & 0xff);
            }
        }
        return len == a.length;
    }

    private byte[] getEncoded(
        DEREncodable obj)
    {
        ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
        ASN1OutputStream        aOut = new ASN1OutputStream(bOut);

        try
        {
            aOut.writeObject(obj);
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException("cannot encode object added to SET");
        }

        return bOut.toByteArray();
    }

    protected void sort()
    {
        if (set.size() > 1)
        {
            boolean    swapped = true;
            int        lastSwap = set.size() - 1;

            while (swapped)
            {
                int    index = 0;
                int    swapIndex = 0;
                byte[] a = getEncoded((DEREncodable)set.elementAt(0));
                
                swapped = false;

                while (index != lastSwap)
                {
                    byte[] b = getEncoded((DEREncodable)set.elementAt(index + 1));

                    if (lessThanOrEqual(a, b))
                    {
                        a = b;
                    }
                    else
                    {
                        Object  o = set.elementAt(index);

                        set.setElementAt(set.elementAt(index + 1), index);
                        set.setElementAt(o, index + 1);

                        swapped = true;
                        swapIndex = index;
                    }

                    index++;
                }

                lastSwap = swapIndex;
            }
        }
    }

    protected void addObject(
        DEREncodable obj)
    {
        set.addElement(obj);
    }

    abstract void encode(DEROutputStream out)
            throws IOException;

    public String toString() 
    {
      return set.toString();
    }
}
