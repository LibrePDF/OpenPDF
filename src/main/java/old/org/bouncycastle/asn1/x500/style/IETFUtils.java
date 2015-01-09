package old.org.bouncycastle.asn1.x500.style;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import old.org.bouncycastle.asn1.ASN1Encodable;
import old.org.bouncycastle.asn1.ASN1Object;
import old.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import old.org.bouncycastle.asn1.ASN1String;
import old.org.bouncycastle.asn1.DERObject;
import old.org.bouncycastle.asn1.DERUniversalString;
import old.org.bouncycastle.asn1.x500.AttributeTypeAndValue;
import old.org.bouncycastle.asn1.x500.RDN;
import old.org.bouncycastle.asn1.x500.X500NameBuilder;
import old.org.bouncycastle.asn1.x500.X500NameStyle;
import old.org.bouncycastle.util.Strings;
import old.org.bouncycastle.util.encoders.Hex;

public class IETFUtils
{
    public static RDN[] rDNsFromString(String name, X500NameStyle x500Style)
    {
        X500NameTokenizer nTok = new X500NameTokenizer(name);
        X500NameBuilder builder = new X500NameBuilder(x500Style);

        while (nTok.hasMoreTokens())
        {
            String  token = nTok.nextToken();
            int     index = token.indexOf('=');

            if (index == -1)
            {
                throw new IllegalArgumentException("badly formated directory string");
            }

            String               attr = token.substring(0, index);
            String               value = token.substring(index + 1);
            ASN1ObjectIdentifier oid = x500Style.attrNameToOID(attr);

            if (value.indexOf('+') > 0)
            {
                X500NameTokenizer   vTok = new X500NameTokenizer(value, '+');
                String  v = vTok.nextToken();

                Vector oids = new Vector();
                Vector values = new Vector();

                oids.addElement(oid);
                values.addElement(v);

                while (vTok.hasMoreTokens())
                {
                    String  sv = vTok.nextToken();
                    int     ndx = sv.indexOf('=');

                    String  nm = sv.substring(0, ndx);
                    String  vl = sv.substring(ndx + 1);

                    oids.addElement(x500Style.attrNameToOID(nm));
                    values.addElement(vl);
                }

                builder.addMultiValuedRDN(toOIDArray(oids), toValueArray(values));
            }
            else
            {
                builder.addRDN(oid, value);
            }
        }

        return builder.build().getRDNs();
    }

    private static String[] toValueArray(Vector values)
    {
        String[] tmp = new String[values.size()];

        for (int i = 0; i != tmp.length; i++)
        {
            tmp[i] = (String)values.elementAt(i);
        }

        return tmp;
    }

    private static ASN1ObjectIdentifier[] toOIDArray(Vector oids)
    {
        ASN1ObjectIdentifier[] tmp = new ASN1ObjectIdentifier[oids.size()];

        for (int i = 0; i != tmp.length; i++)
        {
            tmp[i] = (ASN1ObjectIdentifier)oids.elementAt(i);
        }

        return tmp;
    }

    public static ASN1ObjectIdentifier decodeAttrName(
        String      name,
        Hashtable   lookUp)
    {
        if (Strings.toUpperCase(name).startsWith("OID."))
        {
            return new ASN1ObjectIdentifier(name.substring(4));
        }
        else if (name.charAt(0) >= '0' && name.charAt(0) <= '9')
        {
            return new ASN1ObjectIdentifier(name);
        }

        ASN1ObjectIdentifier oid = (ASN1ObjectIdentifier)lookUp.get(Strings.toLowerCase(name));
        if (oid == null)
        {
            throw new IllegalArgumentException("Unknown object id - " + name + " - passed to distinguished name");
        }

        return oid;
    }

    public static ASN1Encodable valueFromHexString(
        String  str,
        int     off)
        throws IOException
    {
        str = Strings.toLowerCase(str);
        byte[] data = new byte[(str.length() - off) / 2];
        for (int index = 0; index != data.length; index++)
        {
            char left = str.charAt((index * 2) + off);
            char right = str.charAt((index * 2) + off + 1);

            if (left < 'a')
            {
                data[index] = (byte)((left - '0') << 4);
            }
            else
            {
                data[index] = (byte)((left - 'a' + 10) << 4);
            }
            if (right < 'a')
            {
                data[index] |= (byte)(right - '0');
            }
            else
            {
                data[index] |= (byte)(right - 'a' + 10);
            }
        }

        return ASN1Object.fromByteArray(data);
    }

    public static void appendTypeAndValue(
        StringBuffer          buf,
        AttributeTypeAndValue typeAndValue,
        Hashtable             oidSymbols)
    {
        String  sym = (String)oidSymbols.get(typeAndValue.getType());

        if (sym != null)
        {
            buf.append(sym);
        }
        else
        {
            buf.append(typeAndValue.getType().getId());
        }

        buf.append('=');

        buf.append(valueToString(typeAndValue.getValue()));
    }

    public static String valueToString(ASN1Encodable value)
    {
        StringBuffer vBuf = new StringBuffer();

        if (value instanceof ASN1String && !(value instanceof DERUniversalString))
        {
            String v = ((ASN1String)value).getString();
            if (v.length() > 0 && v.charAt(0) == '#')
            {
                vBuf.append("\\" + v);
            }
            else
            {
                vBuf.append(v);
            }
        }
        else
        {
            vBuf.append("#" + bytesToString(Hex.encode(value.getDERObject().getDEREncoded())));
        }

        int     end = vBuf.length();
        int     index = 0;

        if (vBuf.length() >= 2 && vBuf.charAt(0) == '\\' && vBuf.charAt(1) == '#')
        {
            index += 2;
        }

        while (index != end)
        {
            if ((vBuf.charAt(index) == ',')
               || (vBuf.charAt(index) == '"')
               || (vBuf.charAt(index) == '\\')
               || (vBuf.charAt(index) == '+')
               || (vBuf.charAt(index) == '=')
               || (vBuf.charAt(index) == '<')
               || (vBuf.charAt(index) == '>')
               || (vBuf.charAt(index) == ';'))
            {
                vBuf.insert(index, "\\");
                index++;
                end++;
            }

            index++;
        }

        return vBuf.toString();
    }

    private static String bytesToString(
        byte[] data)
    {
        char[]  cs = new char[data.length];

        for (int i = 0; i != cs.length; i++)
        {
            cs[i] = (char)(data[i] & 0xff);
        }

        return new String(cs);
    }

    public static String canonicalize(String s)
    {
        String value = Strings.toLowerCase(s.trim());

        if (value.length() > 0 && value.charAt(0) == '#')
        {
            DERObject obj = decodeObject(value);

            if (obj instanceof ASN1String)
            {
                value = Strings.toLowerCase(((ASN1String)obj).getString().trim());
            }
        }

        value = stripInternalSpaces(value);

        return value;
    }

    private static ASN1Object decodeObject(String oValue)
    {
        try
        {
            return ASN1Object.fromByteArray(Hex.decode(oValue.substring(1)));
        }
        catch (IOException e)
        {
            throw new IllegalStateException("unknown encoding in name: " + e);
        }
    }

    public static String stripInternalSpaces(
        String str)
    {
        StringBuffer res = new StringBuffer();

        if (str.length() != 0)
        {
            char c1 = str.charAt(0);

            res.append(c1);

            for (int k = 1; k < str.length(); k++)
            {
                char c2 = str.charAt(k);
                if (!(c1 == ' ' && c2 == ' '))
                {
                    res.append(c2);
                }
                c1 = c2;
            }
        }

        return res.toString();
    }
}
