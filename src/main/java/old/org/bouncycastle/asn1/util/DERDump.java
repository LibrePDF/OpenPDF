package old.org.bouncycastle.asn1.util;

import old.org.bouncycastle.asn1.DEREncodable;
import old.org.bouncycastle.asn1.DERObject;

/**
 * @deprecated use ASN1Dump.
 */
public class DERDump
    extends ASN1Dump
{
    /**
     * dump out a DER object as a formatted string
     *
     * @param obj the DERObject to be dumped out.
     */
    public static String dumpAsString(
        DERObject   obj)
    {
        StringBuffer buf = new StringBuffer();

        _dumpAsString("", false, obj, buf);

        return buf.toString();
    }

    /**
     * dump out a DER object as a formatted string
     *
     * @param obj the DERObject to be dumped out.
     */
    public static String dumpAsString(
        DEREncodable   obj)
    {
        StringBuffer buf = new StringBuffer();

        _dumpAsString("", false, obj.getDERObject(), buf);

        return buf.toString();
    }
}
