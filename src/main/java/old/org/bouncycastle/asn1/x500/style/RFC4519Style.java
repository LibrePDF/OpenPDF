package old.org.bouncycastle.asn1.x500.style;

import java.io.IOException;
import java.util.Hashtable;

import old.org.bouncycastle.asn1.ASN1Encodable;
import old.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import old.org.bouncycastle.asn1.DERIA5String;
import old.org.bouncycastle.asn1.DERPrintableString;
import old.org.bouncycastle.asn1.DERUTF8String;
import old.org.bouncycastle.asn1.x500.AttributeTypeAndValue;
import old.org.bouncycastle.asn1.x500.RDN;
import old.org.bouncycastle.asn1.x500.X500Name;
import old.org.bouncycastle.asn1.x500.X500NameStyle;

public class RFC4519Style
    implements X500NameStyle
{
    public static final X500NameStyle INSTANCE = new RFC4519Style();

    public static final ASN1ObjectIdentifier businessCategory = new ASN1ObjectIdentifier("2.5.4.15");
    public static final ASN1ObjectIdentifier c = new ASN1ObjectIdentifier("2.5.4.6");
    public static final ASN1ObjectIdentifier cn = new ASN1ObjectIdentifier("2.5.4.3");
    public static final ASN1ObjectIdentifier dc = new ASN1ObjectIdentifier("0.9.2342.19200300.100.1.25");
    public static final ASN1ObjectIdentifier description = new ASN1ObjectIdentifier("2.5.4.13");
    public static final ASN1ObjectIdentifier destinationIndicator = new ASN1ObjectIdentifier("2.5.4.27");
    public static final ASN1ObjectIdentifier distinguishedName = new ASN1ObjectIdentifier("2.5.4.49");
    public static final ASN1ObjectIdentifier dnQualifier = new ASN1ObjectIdentifier("2.5.4.46");
    public static final ASN1ObjectIdentifier enhancedSearchGuide = new ASN1ObjectIdentifier("2.5.4.47");
    public static final ASN1ObjectIdentifier facsimileTelephoneNumber = new ASN1ObjectIdentifier("2.5.4.23");
    public static final ASN1ObjectIdentifier generationQualifier = new ASN1ObjectIdentifier("2.5.4.44");
    public static final ASN1ObjectIdentifier givenName = new ASN1ObjectIdentifier("2.5.4.42");
    public static final ASN1ObjectIdentifier houseIdentifier = new ASN1ObjectIdentifier("2.5.4.51");
    public static final ASN1ObjectIdentifier initials = new ASN1ObjectIdentifier("2.5.4.43");
    public static final ASN1ObjectIdentifier internationalISDNNumber = new ASN1ObjectIdentifier("2.5.4.25");
    public static final ASN1ObjectIdentifier l = new ASN1ObjectIdentifier("2.5.4.7");
    public static final ASN1ObjectIdentifier member = new ASN1ObjectIdentifier("2.5.4.31");
    public static final ASN1ObjectIdentifier name = new ASN1ObjectIdentifier("2.5.4.41");
    public static final ASN1ObjectIdentifier o = new ASN1ObjectIdentifier("2.5.4.10");
    public static final ASN1ObjectIdentifier ou = new ASN1ObjectIdentifier("2.5.4.11");
    public static final ASN1ObjectIdentifier owner = new ASN1ObjectIdentifier("2.5.4.32");
    public static final ASN1ObjectIdentifier physicalDeliveryOfficeName = new ASN1ObjectIdentifier("2.5.4.19");
    public static final ASN1ObjectIdentifier postalAddress = new ASN1ObjectIdentifier("2.5.4.16");
    public static final ASN1ObjectIdentifier postalCode = new ASN1ObjectIdentifier("2.5.4.17");
    public static final ASN1ObjectIdentifier postOfficeBox = new ASN1ObjectIdentifier("2.5.4.18");
    public static final ASN1ObjectIdentifier preferredDeliveryMethod = new ASN1ObjectIdentifier("2.5.4.28");
    public static final ASN1ObjectIdentifier registeredAddress = new ASN1ObjectIdentifier("2.5.4.26");
    public static final ASN1ObjectIdentifier roleOccupant = new ASN1ObjectIdentifier("2.5.4.33");
    public static final ASN1ObjectIdentifier searchGuide = new ASN1ObjectIdentifier("2.5.4.14");
    public static final ASN1ObjectIdentifier seeAlso = new ASN1ObjectIdentifier("2.5.4.34");
    public static final ASN1ObjectIdentifier serialNumber = new ASN1ObjectIdentifier("2.5.4.5");
    public static final ASN1ObjectIdentifier sn = new ASN1ObjectIdentifier("2.5.4.4");
    public static final ASN1ObjectIdentifier st = new ASN1ObjectIdentifier("2.5.4.8");
    public static final ASN1ObjectIdentifier street = new ASN1ObjectIdentifier("2.5.4.9");
    public static final ASN1ObjectIdentifier telephoneNumber = new ASN1ObjectIdentifier("2.5.4.20");
    public static final ASN1ObjectIdentifier teletexTerminalIdentifier = new ASN1ObjectIdentifier("2.5.4.22");
    public static final ASN1ObjectIdentifier telexNumber = new ASN1ObjectIdentifier("2.5.4.21");
    public static final ASN1ObjectIdentifier title = new ASN1ObjectIdentifier("2.5.4.12");
    public static final ASN1ObjectIdentifier uid = new ASN1ObjectIdentifier("0.9.2342.19200300.100.1.1");
    public static final ASN1ObjectIdentifier uniqueMember = new ASN1ObjectIdentifier("2.5.4.50");
    public static final ASN1ObjectIdentifier userPassword = new ASN1ObjectIdentifier("2.5.4.35");
    public static final ASN1ObjectIdentifier x121Address = new ASN1ObjectIdentifier("2.5.4.24");
    public static final ASN1ObjectIdentifier x500UniqueIdentifier = new ASN1ObjectIdentifier("2.5.4.45");

    /**
     * default look up table translating OID values into their common symbols following
     * the convention in RFC 2253 with a few extras
     */
    private static final Hashtable DefaultSymbols = new Hashtable();

    /**
     * look up table translating common symbols into their OIDS.
     */
    private static final Hashtable DefaultLookUp = new Hashtable();

    static
    {
        DefaultSymbols.put(businessCategory, "businessCategory");
        DefaultSymbols.put(c, "c");
        DefaultSymbols.put(cn, "cn");
        DefaultSymbols.put(dc, "dc");
        DefaultSymbols.put(description, "description");
        DefaultSymbols.put(destinationIndicator, "destinationIndicator");
        DefaultSymbols.put(distinguishedName, "distinguishedName");
        DefaultSymbols.put(dnQualifier, "dnQualifier");
        DefaultSymbols.put(enhancedSearchGuide, "enhancedSearchGuide");
        DefaultSymbols.put(facsimileTelephoneNumber, "facsimileTelephoneNumber");
        DefaultSymbols.put(generationQualifier, "generationQualifier");
        DefaultSymbols.put(givenName, "givenName");
        DefaultSymbols.put(houseIdentifier, "houseIdentifier");
        DefaultSymbols.put(initials, "initials");
        DefaultSymbols.put(internationalISDNNumber, "internationalISDNNumber");
        DefaultSymbols.put(l, "l");
        DefaultSymbols.put(member, "member");
        DefaultSymbols.put(name, "name");
        DefaultSymbols.put(o, "o");
        DefaultSymbols.put(ou, "ou");
        DefaultSymbols.put(owner, "owner");
        DefaultSymbols.put(physicalDeliveryOfficeName, "physicalDeliveryOfficeName");
        DefaultSymbols.put(postalAddress, "postalAddress");
        DefaultSymbols.put(postalCode, "postalCode");
        DefaultSymbols.put(postOfficeBox, "postOfficeBox");
        DefaultSymbols.put(preferredDeliveryMethod, "preferredDeliveryMethod");
        DefaultSymbols.put(registeredAddress, "registeredAddress");
        DefaultSymbols.put(roleOccupant, "roleOccupant");
        DefaultSymbols.put(searchGuide, "searchGuide");
        DefaultSymbols.put(seeAlso, "seeAlso");
        DefaultSymbols.put(serialNumber, "serialNumber");
        DefaultSymbols.put(sn, "sn");
        DefaultSymbols.put(st, "st");
        DefaultSymbols.put(street, "street");
        DefaultSymbols.put(telephoneNumber, "telephoneNumber");
        DefaultSymbols.put(teletexTerminalIdentifier, "teletexTerminalIdentifier");
        DefaultSymbols.put(telexNumber, "telexNumber");
        DefaultSymbols.put(title, "title");
        DefaultSymbols.put(uid, "uid");
        DefaultSymbols.put(uniqueMember, "uniqueMember");
        DefaultSymbols.put(userPassword, "userPassword");
        DefaultSymbols.put(x121Address, "x121Address");
        DefaultSymbols.put(x500UniqueIdentifier, "x500UniqueIdentifier");

        DefaultLookUp.put("businesscategory", businessCategory);
        DefaultLookUp.put("c", c);
        DefaultLookUp.put("cn", cn);
        DefaultLookUp.put("dc", dc);
        DefaultLookUp.put("description", description);
        DefaultLookUp.put("destinationindicator", destinationIndicator);
        DefaultLookUp.put("distinguishedname", distinguishedName);
        DefaultLookUp.put("dnqualifier", dnQualifier);
        DefaultLookUp.put("enhancedsearchguide", enhancedSearchGuide);
        DefaultLookUp.put("facsimiletelephonenumber", facsimileTelephoneNumber);
        DefaultLookUp.put("generationqualifier", generationQualifier);
        DefaultLookUp.put("givenname", givenName);
        DefaultLookUp.put("houseidentifier", houseIdentifier);
        DefaultLookUp.put("initials", initials);
        DefaultLookUp.put("internationalisdnnumber", internationalISDNNumber);
        DefaultLookUp.put("l", l);
        DefaultLookUp.put("member", member);
        DefaultLookUp.put("name", name);
        DefaultLookUp.put("o", o);
        DefaultLookUp.put("ou", ou);
        DefaultLookUp.put("owner", owner);
        DefaultLookUp.put("physicaldeliveryofficename", physicalDeliveryOfficeName);
        DefaultLookUp.put("postaladdress", postalAddress);
        DefaultLookUp.put("postalcode", postalCode);
        DefaultLookUp.put("postofficebox", postOfficeBox);
        DefaultLookUp.put("preferreddeliverymethod", preferredDeliveryMethod);
        DefaultLookUp.put("registeredaddress", registeredAddress);
        DefaultLookUp.put("roleoccupant", roleOccupant);
        DefaultLookUp.put("searchguide", searchGuide);
        DefaultLookUp.put("seealso", seeAlso);
        DefaultLookUp.put("serialnumber", serialNumber);
        DefaultLookUp.put("sn", sn);
        DefaultLookUp.put("st", st);
        DefaultLookUp.put("street", street);
        DefaultLookUp.put("telephonenumber", telephoneNumber);
        DefaultLookUp.put("teletexterminalidentifier", teletexTerminalIdentifier);
        DefaultLookUp.put("telexnumber", telexNumber);
        DefaultLookUp.put("title", title);
        DefaultLookUp.put("uid", uid);
        DefaultLookUp.put("uniquemember", uniqueMember);
        DefaultLookUp.put("userpassword", userPassword);
        DefaultLookUp.put("x121address", x121Address);
        DefaultLookUp.put("x500uniqueidentifier", x500UniqueIdentifier);

        // TODO: need to add correct matching for equality comparisons.
    }

    protected RFC4519Style()
    {

    }

    public ASN1Encodable stringToValue(ASN1ObjectIdentifier oid, String value)
    {
        if (value.length() != 0 && value.charAt(0) == '#')
        {
            try
            {
                return IETFUtils.valueFromHexString(value, 1);
            }
            catch (IOException e)
            {
                throw new RuntimeException("can't recode value for oid " + oid.getId());
            }
        }
        else
        {
            if (value.length() != 0 && value.charAt(0) == '\\')
            {
                value = value.substring(1);
            }
            if (oid.equals(dc))
            {
                return new DERIA5String(value);
            }
            else if (oid.equals(c) || oid.equals(serialNumber) || oid.equals(dnQualifier)
                || oid.equals(telephoneNumber))
            {
                return new DERPrintableString(value);
            }
        }

        return new DERUTF8String(value);
    }

    public ASN1ObjectIdentifier attrNameToOID(String attrName)
    {
        return IETFUtils.decodeAttrName(attrName, DefaultLookUp);
    }

    public boolean areEqual(X500Name name1, X500Name name2)
    {
        RDN[] rdns1 = name1.getRDNs();
        RDN[] rdns2 = name2.getRDNs();

        if (rdns1.length != rdns2.length)
        {
            return false;
        }

        boolean reverse = false;

        if (rdns1[0].getFirst() != null && rdns2[0].getFirst() != null)
        {
            reverse = !rdns1[0].getFirst().getType().equals(rdns2[0].getFirst().getType());  // guess forward
        }

        for (int i = 0; i != rdns1.length; i++)
        {
            if (!foundMatch(reverse, rdns1[i], rdns2))
            {
                return false;
            }
        }

        return true;
    }

    private boolean foundMatch(boolean reverse, RDN rdn, RDN[] possRDNs)
    {
        if (reverse)
        {
            for (int i = possRDNs.length - 1; i >= 0; i--)
            {
                if (possRDNs[i] != null && rdnAreEqual(rdn, possRDNs[i]))
                {
                    possRDNs[i] = null;
                    return true;
                }
            }
        }
        else
        {
            for (int i = 0; i != possRDNs.length; i++)
            {
                if (possRDNs[i] != null && rdnAreEqual(rdn, possRDNs[i]))
                {
                    possRDNs[i] = null;
                    return true;
                }
            }
        }

        return false;
    }

    protected boolean rdnAreEqual(RDN rdn1, RDN rdn2)
    {
        if (rdn1.isMultiValued())
        {
            if (rdn2.isMultiValued())
            {
                AttributeTypeAndValue[] atvs1 = rdn1.getTypesAndValues();
                AttributeTypeAndValue[] atvs2 = rdn2.getTypesAndValues();

                if (atvs1.length != atvs2.length)
                {
                    return false;
                }

                for (int i = 0; i != atvs1.length; i++)
                {
                    if (!atvAreEqual(atvs1[i], atvs2[i]))
                    {
                        return false;
                    }
                }
            }
            else
            {
                return false;
            }
        }
        else
        {
            if (!rdn2.isMultiValued())
            {
                return atvAreEqual(rdn1.getFirst(), rdn2.getFirst());
            }
            else
            {
                return false;
            }
        }

        return true;
    }

    private boolean atvAreEqual(AttributeTypeAndValue atv1, AttributeTypeAndValue atv2)
    {
        if (atv1 == atv2)
        {
            return true;
        }

        if (atv1 == null)
        {
            return false;
        }

        if (atv2 == null)
        {
            return false;
        }

        ASN1ObjectIdentifier o1 = atv1.getType();
        ASN1ObjectIdentifier o2 = atv2.getType();

        if (!o1.equals(o2))
        {
            return false;
        }

        String v1 = IETFUtils.canonicalize(IETFUtils.valueToString(atv1.getValue()));
        String v2 = IETFUtils.canonicalize(IETFUtils.valueToString(atv2.getValue()));

        if (!v1.equals(v2))
        {
            return false;
        }

        return true;
    }

    // parse backwards
    public RDN[] fromString(String dirName)
    {
        RDN[] tmp = IETFUtils.rDNsFromString(dirName, this);
        RDN[] res = new RDN[tmp.length];

        for (int i = 0; i != tmp.length; i++)
        {
            res[res.length - i - 1] = tmp[i];
        }

        return res;
    }

    public int calculateHashCode(X500Name name)
    {
        int hashCodeValue = 0;
        RDN[] rdns = name.getRDNs();

        // this needs to be order independent, like equals
        for (int i = 0; i != rdns.length; i++)
        {
            if (rdns[i].isMultiValued())
            {
                AttributeTypeAndValue[] atv = rdns[i].getTypesAndValues();

                for (int j = 0; j != atv.length; j++)
                {
                    hashCodeValue ^= atv[j].getType().hashCode();
                    hashCodeValue ^= calcHashCode(atv[j].getValue());
                }
            }
            else
            {
                hashCodeValue ^= rdns[i].getFirst().getType().hashCode();
                hashCodeValue ^= calcHashCode(rdns[i].getFirst().getValue());
            }
        }

        return hashCodeValue;
    }

    private int calcHashCode(ASN1Encodable enc)
    {
        String value = IETFUtils.valueToString(enc);

        value = IETFUtils.canonicalize(value);

        return value.hashCode();
    }

    // convert in reverse
    public String toString(X500Name name)
    {
        StringBuffer buf = new StringBuffer();
        boolean first = true;

        RDN[] rdns = name.getRDNs();

        for (int i = rdns.length - 1; i >= 0; i--)
        {
            if (first)
            {
                first = false;
            }
            else
            {
                buf.append(',');
            }

            if (rdns[i].isMultiValued())
            {
                AttributeTypeAndValue[] atv = rdns[i].getTypesAndValues();
                boolean firstAtv = true;

                for (int j = 0; j != atv.length; j++)
                {
                    if (firstAtv)
                    {
                        firstAtv = false;
                    }
                    else
                    {
                        buf.append('+');
                    }

                    IETFUtils.appendTypeAndValue(buf, atv[j], DefaultSymbols);
                }
            }
            else
            {
                IETFUtils.appendTypeAndValue(buf, rdns[i].getFirst(), DefaultSymbols);
            }
        }

        return buf.toString();
    }
}
