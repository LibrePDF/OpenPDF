package old.org.bouncycastle.asn1.x509;

import old.org.bouncycastle.asn1.ASN1ObjectIdentifier;

public interface X509ObjectIdentifiers
{
    //
    // base id
    //
    static final String                 id                      = "2.5.4";

    static final ASN1ObjectIdentifier    commonName              = new ASN1ObjectIdentifier(id + ".3");
    static final ASN1ObjectIdentifier    countryName             = new ASN1ObjectIdentifier(id + ".6");
    static final ASN1ObjectIdentifier    localityName            = new ASN1ObjectIdentifier(id + ".7");
    static final ASN1ObjectIdentifier    stateOrProvinceName     = new ASN1ObjectIdentifier(id + ".8");
    static final ASN1ObjectIdentifier    organization            = new ASN1ObjectIdentifier(id + ".10");
    static final ASN1ObjectIdentifier    organizationalUnitName  = new ASN1ObjectIdentifier(id + ".11");

    static final ASN1ObjectIdentifier    id_at_telephoneNumber   = new ASN1ObjectIdentifier("2.5.4.20");
    static final ASN1ObjectIdentifier    id_at_name              = new ASN1ObjectIdentifier(id + ".41");

    // id-SHA1 OBJECT IDENTIFIER ::=    
    //   {iso(1) identified-organization(3) oiw(14) secsig(3) algorithms(2) 26 }    //
    static final ASN1ObjectIdentifier    id_SHA1                 = new ASN1ObjectIdentifier("1.3.14.3.2.26");

    //
    // ripemd160 OBJECT IDENTIFIER ::=
    //      {iso(1) identified-organization(3) TeleTrust(36) algorithm(3) hashAlgorithm(2) RIPEMD-160(1)}
    //
    static final ASN1ObjectIdentifier    ripemd160               = new ASN1ObjectIdentifier("1.3.36.3.2.1");

    //
    // ripemd160WithRSAEncryption OBJECT IDENTIFIER ::=
    //      {iso(1) identified-organization(3) TeleTrust(36) algorithm(3) signatureAlgorithm(3) rsaSignature(1) rsaSignatureWithripemd160(2) }
    //
    static final ASN1ObjectIdentifier    ripemd160WithRSAEncryption = new ASN1ObjectIdentifier("1.3.36.3.3.1.2");


    static final ASN1ObjectIdentifier    id_ea_rsa = new ASN1ObjectIdentifier("2.5.8.1.1");
    
    // id-pkix
    static final ASN1ObjectIdentifier id_pkix = new ASN1ObjectIdentifier("1.3.6.1.5.5.7");

    //
    // private internet extensions
    //
    static final ASN1ObjectIdentifier  id_pe = new ASN1ObjectIdentifier(id_pkix + ".1");

    //
    // authority information access
    //
    static final ASN1ObjectIdentifier  id_ad = new ASN1ObjectIdentifier(id_pkix + ".48");
    static final ASN1ObjectIdentifier  id_ad_caIssuers = new ASN1ObjectIdentifier(id_ad + ".2");
    static final ASN1ObjectIdentifier  id_ad_ocsp = new ASN1ObjectIdentifier(id_ad + ".1");

    //
    //    OID for ocsp and crl uri in AuthorityInformationAccess extension
    //
    static final ASN1ObjectIdentifier ocspAccessMethod = id_ad_ocsp;
    static final ASN1ObjectIdentifier crlAccessMethod = id_ad_caIssuers;
}

