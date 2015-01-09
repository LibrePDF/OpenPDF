package old.org.bouncycastle.asn1.x9;

import old.org.bouncycastle.asn1.ASN1ObjectIdentifier;

public interface X9ObjectIdentifiers
{
    //
    // X9.62
    //
    // ansi-X9-62 OBJECT IDENTIFIER ::= { iso(1) member-body(2)
    //            us(840) ansi-x962(10045) }
    //
    static final ASN1ObjectIdentifier ansi_X9_62 = new ASN1ObjectIdentifier("1.2.840.10045");
    static final ASN1ObjectIdentifier id_fieldType = ansi_X9_62.branch("1");

    static final ASN1ObjectIdentifier prime_field = id_fieldType.branch("1");

    static final ASN1ObjectIdentifier characteristic_two_field = id_fieldType.branch("2");

    static final ASN1ObjectIdentifier gnBasis = id_fieldType.branch("2.3.1");

    static final ASN1ObjectIdentifier tpBasis = id_fieldType.branch("2.3.2");

    static final ASN1ObjectIdentifier ppBasis = id_fieldType.branch("2.3.3");

    static final ASN1ObjectIdentifier id_ecSigType = ansi_X9_62.branch("4");

    static final ASN1ObjectIdentifier ecdsa_with_SHA1 = new ASN1ObjectIdentifier(id_ecSigType + ".1");

    static final ASN1ObjectIdentifier id_publicKeyType = ansi_X9_62.branch("2");

    static final ASN1ObjectIdentifier id_ecPublicKey = id_publicKeyType.branch("1");

    static final ASN1ObjectIdentifier ecdsa_with_SHA2 = id_ecSigType.branch("3");

    static final ASN1ObjectIdentifier ecdsa_with_SHA224 = ecdsa_with_SHA2.branch("1");

    static final ASN1ObjectIdentifier ecdsa_with_SHA256 = ecdsa_with_SHA2.branch("2");

    static final ASN1ObjectIdentifier ecdsa_with_SHA384 = ecdsa_with_SHA2.branch("3");

    static final ASN1ObjectIdentifier ecdsa_with_SHA512 = ecdsa_with_SHA2.branch("4");

    //
    // named curves
    //
    static final ASN1ObjectIdentifier ellipticCurve = ansi_X9_62.branch("3");

    //
    // Two Curves
    //
    static final ASN1ObjectIdentifier  cTwoCurve = ellipticCurve.branch("0");

    static final ASN1ObjectIdentifier c2pnb163v1 = cTwoCurve.branch("1");
    static final ASN1ObjectIdentifier c2pnb163v2 = cTwoCurve.branch("2");
    static final ASN1ObjectIdentifier c2pnb163v3 = cTwoCurve.branch("3");
    static final ASN1ObjectIdentifier c2pnb176w1 = cTwoCurve.branch("4");
    static final ASN1ObjectIdentifier c2tnb191v1 = cTwoCurve.branch("5");
    static final ASN1ObjectIdentifier c2tnb191v2 = cTwoCurve.branch("6");
    static final ASN1ObjectIdentifier c2tnb191v3 = cTwoCurve.branch("7");
    static final ASN1ObjectIdentifier c2onb191v4 = cTwoCurve.branch("8");
    static final ASN1ObjectIdentifier c2onb191v5 = cTwoCurve.branch("9");
    static final ASN1ObjectIdentifier c2pnb208w1 = cTwoCurve.branch("10");
    static final ASN1ObjectIdentifier c2tnb239v1 = cTwoCurve.branch("11");
    static final ASN1ObjectIdentifier c2tnb239v2 = cTwoCurve.branch("12");
    static final ASN1ObjectIdentifier c2tnb239v3 = cTwoCurve.branch("13");
    static final ASN1ObjectIdentifier c2onb239v4 = cTwoCurve.branch("14");
    static final ASN1ObjectIdentifier c2onb239v5 = cTwoCurve.branch("15");
    static final ASN1ObjectIdentifier c2pnb272w1 = cTwoCurve.branch("16");
    static final ASN1ObjectIdentifier c2pnb304w1 = cTwoCurve.branch("17");
    static final ASN1ObjectIdentifier c2tnb359v1 = cTwoCurve.branch("18");
    static final ASN1ObjectIdentifier c2pnb368w1 = cTwoCurve.branch("19");
    static final ASN1ObjectIdentifier c2tnb431r1 = cTwoCurve.branch("20");

    //
    // Prime
    //
    static final ASN1ObjectIdentifier primeCurve = ellipticCurve.branch("1");

    static final ASN1ObjectIdentifier prime192v1 = primeCurve.branch("1");
    static final ASN1ObjectIdentifier prime192v2 = primeCurve.branch("2");
    static final ASN1ObjectIdentifier prime192v3 = primeCurve.branch("3");
    static final ASN1ObjectIdentifier prime239v1 = primeCurve.branch("4");
    static final ASN1ObjectIdentifier prime239v2 = primeCurve.branch("5");
    static final ASN1ObjectIdentifier prime239v3 = primeCurve.branch("6");
    static final ASN1ObjectIdentifier prime256v1 = primeCurve.branch("7");

    //
    // DSA
    //
    // dsapublicnumber OBJECT IDENTIFIER ::= { iso(1) member-body(2)
    //            us(840) ansi-x957(10040) number-type(4) 1 }
    static final ASN1ObjectIdentifier id_dsa = new ASN1ObjectIdentifier("1.2.840.10040.4.1");

    /**
     * id-dsa-with-sha1 OBJECT IDENTIFIER ::= { iso(1) member-body(2) us(840) x9-57
     * (10040) x9cm(4) 3 }
     */
    public static final ASN1ObjectIdentifier id_dsa_with_sha1 = new ASN1ObjectIdentifier("1.2.840.10040.4.3");

    /**
     * X9.63
     */
    public static final ASN1ObjectIdentifier x9_63_scheme = new ASN1ObjectIdentifier("1.3.133.16.840.63.0");
    public static final ASN1ObjectIdentifier dhSinglePass_stdDH_sha1kdf_scheme = x9_63_scheme.branch("2");
    public static final ASN1ObjectIdentifier dhSinglePass_cofactorDH_sha1kdf_scheme = x9_63_scheme.branch("3");
    public static final ASN1ObjectIdentifier mqvSinglePass_sha1kdf_scheme = x9_63_scheme.branch("16");

    /**
     * X9.42
     */

    static final ASN1ObjectIdentifier ansi_X9_42 = new ASN1ObjectIdentifier("1.2.840.10046");

    //
    // Diffie-Hellman
    //
    // dhpublicnumber OBJECT IDENTIFIER ::= { iso(1) member-body(2)
    //            us(840) ansi-x942(10046) number-type(2) 1 }
    //
    public static final ASN1ObjectIdentifier dhpublicnumber = ansi_X9_42.branch("2.1");

    public static final ASN1ObjectIdentifier x9_42_schemes = ansi_X9_42.branch("3");
    public static final ASN1ObjectIdentifier dhStatic = x9_42_schemes.branch("1");
    public static final ASN1ObjectIdentifier dhEphem = x9_42_schemes.branch("2");
    public static final ASN1ObjectIdentifier dhOneFlow = x9_42_schemes.branch("3");
    public static final ASN1ObjectIdentifier dhHybrid1 = x9_42_schemes.branch("4");
    public static final ASN1ObjectIdentifier dhHybrid2 = x9_42_schemes.branch("5");
    public static final ASN1ObjectIdentifier dhHybridOneFlow = x9_42_schemes.branch("6");
    public static final ASN1ObjectIdentifier mqv2 = x9_42_schemes.branch("7");
    public static final ASN1ObjectIdentifier mqv1 = x9_42_schemes.branch("8");
}
