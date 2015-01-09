package old.org.bouncycastle.asn1.iana;

import old.org.bouncycastle.asn1.ASN1ObjectIdentifier;

public interface IANAObjectIdentifiers
{
    // id-SHA1 OBJECT IDENTIFIER ::=    
    // {iso(1) identified-organization(3) dod(6) internet(1) security(5) mechanisms(5) ipsec(8) isakmpOakley(1)}
    //

    static final ASN1ObjectIdentifier    isakmpOakley  = new ASN1ObjectIdentifier("1.3.6.1.5.5.8.1");

    static final ASN1ObjectIdentifier    hmacMD5       = new ASN1ObjectIdentifier(isakmpOakley + ".1");
    static final ASN1ObjectIdentifier    hmacSHA1     = new ASN1ObjectIdentifier(isakmpOakley + ".2");
    
    static final ASN1ObjectIdentifier    hmacTIGER     = new ASN1ObjectIdentifier(isakmpOakley + ".3");
    
    static final ASN1ObjectIdentifier    hmacRIPEMD160 = new ASN1ObjectIdentifier(isakmpOakley + ".4");

}
