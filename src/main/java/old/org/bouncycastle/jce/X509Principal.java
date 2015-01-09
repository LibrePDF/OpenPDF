package old.org.bouncycastle.jce;

import java.io.IOException;
import java.security.Principal;
import java.util.Hashtable;
import java.util.Vector;

import old.org.bouncycastle.asn1.ASN1Encodable;
import old.org.bouncycastle.asn1.ASN1InputStream;
import old.org.bouncycastle.asn1.ASN1Sequence;
import old.org.bouncycastle.asn1.x509.X509Name;

/**
 * a general extension of X509Name with a couple of extra methods and
 * constructors.
 * <p>
 * Objects of this type can be created from certificates and CRLs using the
 * PrincipalUtil class.
 * </p>
 * @see old.org.bouncycastle.jce.PrincipalUtil
 */
public class X509Principal
    extends X509Name
    implements Principal
{
    private static ASN1Sequence readSequence(
        ASN1InputStream aIn)
        throws IOException
    {
        try
        {
            return ASN1Sequence.getInstance(aIn.readObject());
        }
        catch (IllegalArgumentException e)
        {
            throw new IOException("not an ASN.1 Sequence: " + e);
        }
    }

    /**
     * Constructor from an encoded byte array.
     */
    public X509Principal(
        byte[]  bytes)
        throws IOException
    {
        super(readSequence(new ASN1InputStream(bytes)));
    }

    /**
     * Constructor from an X509Name object.
     */
    public X509Principal(
        X509Name  name)
    {
        super((ASN1Sequence)name.getDERObject());
    }

    /**
     * constructor from a table of attributes.
     * <p>
     * it's is assumed the table contains OID/String pairs.
     */
    public X509Principal(
        Hashtable  attributes)
    {
        super(attributes);
    }

    /**
     * constructor from a table of attributes and a vector giving the
     * specific ordering required for encoding or conversion to a string.
     * <p>
     * it's is assumed the table contains OID/String pairs.
     */
    public X509Principal(
        Vector      ordering,
        Hashtable   attributes)
    {
        super(ordering, attributes);
    }

    /**
     * constructor from a vector of attribute values and a vector of OIDs.
     */
    public X509Principal(
        Vector      oids,
        Vector      values)
    {
        super(oids, values);
    }

    /**
     * takes an X509 dir name as a string of the format "C=AU,ST=Victoria", or
     * some such, converting it into an ordered set of name attributes.
     */
    public X509Principal(
        String  dirName)
    {
        super(dirName);
    }

    /**
     * Takes an X509 dir name as a string of the format "C=AU,ST=Victoria", or
     * some such, converting it into an ordered set of name attributes. If reverse
     * is false the dir name will be encoded in the order of the (name, value) pairs 
     * presented, otherwise the encoding will start with the last (name, value) pair
     * and work back.
     */
    public X509Principal(
        boolean reverse,
        String  dirName)
    {
        super(reverse, dirName);
    }

    /**
     * Takes an X509 dir name as a string of the format "C=AU, ST=Victoria", or
     * some such, converting it into an ordered set of name attributes. lookUp 
     * should provide a table of lookups, indexed by lowercase only strings and
     * yielding a DERObjectIdentifier, other than that OID. and numeric oids
     * will be processed automatically.
     * <p>
     * If reverse is true, create the encoded version of the sequence starting
     * from the last element in the string.
     */
    public X509Principal(
        boolean     reverse,
        Hashtable   lookUp,
        String      dirName)
    {
        super(reverse, lookUp, dirName);
    }

    public String getName()
    {
        return this.toString();
    }

    /**
     * return a DER encoded byte array representing this object
     */
    public byte[] getEncoded()
    {
        try
        {
            return this.getEncoded(ASN1Encodable.DER);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e.toString());
        }
    }
}
