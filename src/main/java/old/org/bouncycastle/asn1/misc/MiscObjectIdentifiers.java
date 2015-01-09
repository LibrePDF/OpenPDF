package old.org.bouncycastle.asn1.misc;

import old.org.bouncycastle.asn1.ASN1ObjectIdentifier;

public interface MiscObjectIdentifiers
{
    //
    // Netscape
    //       iso/itu(2) joint-assign(16) us(840) uscompany(1) netscape(113730) cert-extensions(1) }
    //
    static final ASN1ObjectIdentifier    netscape                = new ASN1ObjectIdentifier("2.16.840.1.113730.1");
    static final ASN1ObjectIdentifier    netscapeCertType        = netscape.branch("1");
    static final ASN1ObjectIdentifier    netscapeBaseURL         = netscape.branch("2");
    static final ASN1ObjectIdentifier    netscapeRevocationURL   = netscape.branch("3");
    static final ASN1ObjectIdentifier    netscapeCARevocationURL = netscape.branch("4");
    static final ASN1ObjectIdentifier    netscapeRenewalURL      = netscape.branch("7");
    static final ASN1ObjectIdentifier    netscapeCApolicyURL     = netscape.branch("8");
    static final ASN1ObjectIdentifier    netscapeSSLServerName   = netscape.branch("12");
    static final ASN1ObjectIdentifier    netscapeCertComment     = netscape.branch("13");
    
    //
    // Verisign
    //       iso/itu(2) joint-assign(16) us(840) uscompany(1) verisign(113733) cert-extensions(1) }
    //
    static final ASN1ObjectIdentifier   verisign                = new ASN1ObjectIdentifier("2.16.840.1.113733.1");

    //
    // CZAG - country, zip, age, and gender
    //
    static final ASN1ObjectIdentifier    verisignCzagExtension   = verisign.branch("6.3");
    // D&B D-U-N-S number
    static final ASN1ObjectIdentifier    verisignDnbDunsNumber   = verisign.branch("6.15");

    //
    // Novell
    //       iso/itu(2) country(16) us(840) organization(1) novell(113719)
    //
    static final ASN1ObjectIdentifier    novell                  = new ASN1ObjectIdentifier("2.16.840.1.113719");
    static final ASN1ObjectIdentifier    novellSecurityAttribs   = novell.branch("1.9.4.1");

    //
    // Entrust
    //       iso(1) member-body(16) us(840) nortelnetworks(113533) entrust(7)
    //
    static final ASN1ObjectIdentifier    entrust                 = new ASN1ObjectIdentifier("1.2.840.113533.7");
    static final ASN1ObjectIdentifier    entrustVersionExtension = entrust.branch("65.0");
}
